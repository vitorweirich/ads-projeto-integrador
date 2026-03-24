import { jsonHeaders } from "@/constants/api";
import { useAuth } from "@/contexts/AuthContext";
import * as FileSystem from "expo-file-system";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

export type FileItem = {
  id: number;
  title: string;
  shareUrl?: string;
  expiresIn?: string; // ISO datetime
};

type FilesContextType = {
  files: FileItem[];
  refresh: () => Promise<void>;
  upload: (
    pickedFile: { uri: string; name: string; mimeType: string; size: number },
    onProgress?: (progress: number) => void,
    onStage?: (
      stage:
        | "preparing"
        | "requesting"
        | "uploading"
        | "registering"
        | "refreshing"
        | "done",
    ) => void,
  ) => Promise<FileItem>;
  getFileSignedUrl: (fileId: number) => Promise<string>;
  getById: (id: number) => FileItem | undefined;
};

const FilesContext = createContext<FilesContextType | undefined>(undefined);

export function FilesProvider({ children }: { children: React.ReactNode }) {
  const [files, setFiles] = useState<FileItem[]>([]);
  const { authFetch, user } = useAuth();

  const refresh = useCallback(async () => {
    const res = await authFetch("/v1/files/me?rows=20");
    if (!res.ok) throw new Error("Falha ao listar arquivos");
    const page = await res.json();
    const mapped: FileItem[] = (page?.content || []).map((v: any) => ({
      id: v.id,
      title: v.name,
      shareUrl: v.shareUrl,
      expiresIn: v.expiresIn,
    }));
    setFiles(mapped);
  }, [authFetch]);

  // Keep files in sync with auth state: clear on logout, refresh on login
  useEffect(() => {
    if (!user) {
      setFiles([]);
      return;
    }
    refresh().catch(() => {});
  }, [user, refresh]);

  const requestSignedPut = useCallback(
    async (name: string, size: number, mimeType: string) => {
      const res = await authFetch("/v1/files/upload", {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify({
          fileName: name,
          fileSize: size,
          contentType: mimeType,
        }),
      });
      if (!res.ok) {
        const body = await res.text().catch(() => "");
        throw new Error(
          `Falha ao requisitar URL de upload (${res.status})${body ? `: ${body}` : ""}`,
        );
      }
      const data = await res.json();
      return data as {
        signedUrl: string;
        fileId: number;
        expirationDate: string;
      };
    },
    [authFetch],
  );

  const doUploadToSignedUrl = useCallback(
    async (
      signedUrl: string,
      file: { uri: string; mimeType: string },
      onProgress?: (progress: number) => void,
      knownSize?: number,
    ) => {
      // Ensure we have a file:// path. If it's a content:// URI, copy to cache first.
      let uploadUri = file.uri;
      if (!uploadUri.startsWith("file://")) {
        const target = `${FileSystem.cacheDirectory}upload-${Date.now()}`;
        await FileSystem.copyAsync({ from: uploadUri, to: target });
        uploadUri = target;
      }
      onProgress?.(0);
      // Use a progress-capable upload task to emit real-time progress updates
      const uploadTask = FileSystem.createUploadTask(
        signedUrl,
        uploadUri,
        {
          httpMethod: "PUT",
          headers: { "Content-Type": file.mimeType },
          uploadType: FileSystem.FileSystemUploadType.BINARY_CONTENT,
        },
        (evt) => {
          try {
            const sent = evt.totalBytesSent ?? 0;
            const expectedFromEvent = evt.totalBytesExpectedToSend ?? 0;
            const expected =
              expectedFromEvent > 0
                ? expectedFromEvent
                : knownSize && knownSize > 0
                  ? knownSize
                  : 0;
            if (expected > 0) {
              // Clamp between 0 and <1; final 1 will be emitted after success
              const ratio = Math.max(0, Math.min(0.999, sent / expected));
              onProgress?.(ratio);
            }
          } catch {
            // ignore progress errors
          }
        },
      );
      const result = await uploadTask.uploadAsync();
      if (!result || result.status < 200 || result.status >= 300) {
        const response = result?.body ? ` - ${result.body}` : "";
        throw new Error(
          `Falha ao enviar arquivo (HTTP ${result?.status ?? "desconhecido"})${response}`,
        );
      }
      onProgress?.(1);
    },
    [],
  );

  const registerUploaded = useCallback(
    async (fileId: number) => {
      const res = await authFetch(
        `/v1/files/upload/${fileId}/register-uploaded`,
        { method: "PATCH" },
      );
      if (!res.ok && res.status !== 204) {
        const body = await res.text().catch(() => "");
        throw new Error(
          `Falha ao registrar upload (${res.status})${body ? `: ${body}` : ""}`,
        );
      }
    },
    [authFetch],
  );

  const upload = useCallback(
    async (
      pickedFile: { uri: string; name: string; mimeType: string; size: number },
      onProgress?: (progress: number) => void,
      onStage?: (
        stage:
          | "preparing"
          | "requesting"
          | "uploading"
          | "registering"
          | "refreshing"
          | "done",
      ) => void,
    ) => {
      try {
        onStage?.("preparing");
        // Ensure local file path and determine size reliably
        let activeUri = pickedFile.uri;
        if (!activeUri.startsWith("file://")) {
          const extFromName = pickedFile.name.split(".").pop() || "tmp";
          const target = `${FileSystem.cacheDirectory}upload-${Date.now()}.${extFromName}`;
          await FileSystem.copyAsync({ from: activeUri, to: target });
          activeUri = target;
        }
        let effectiveSize = pickedFile.size;
        if (!effectiveSize || effectiveSize <= 0) {
          try {
            const info = await FileSystem.getInfoAsync(activeUri);
            // @ts-ignore size is available on native platforms
            effectiveSize = (info as any)?.size ?? 0;
          } catch {}
          if (!effectiveSize) {
            throw new Error(
              "Não foi possível determinar o tamanho do arquivo selecionado.",
            );
          }
        }

        onStage?.("requesting");
        const req = await requestSignedPut(
          pickedFile.name,
          effectiveSize,
          pickedFile.mimeType,
        );

        onStage?.("uploading");
        try {
          await doUploadToSignedUrl(
            req.signedUrl,
            { uri: activeUri, mimeType: pickedFile.mimeType },
            onProgress,
            effectiveSize,
          );
        } catch (e: any) {
          // Upload falhou após criar o registro do arquivo no backend
          throw new Error(`Falha no envio do arquivo: ${e?.message || e}`);
        }

        onStage?.("registering");
        await registerUploaded(req.fileId);

        onStage?.("refreshing");
        await refresh();

        onStage?.("done");
        const item =
          files.find((v) => v.id === req.fileId) ||
          ({ id: req.fileId, title: pickedFile.name } as FileItem);
        return item;
      } catch (err) {
        // Propaga erro detalhado para a UI
        throw err;
      }
    },
    [doUploadToSignedUrl, registerUploaded, refresh, requestSignedPut, files],
  );

  const getFileSignedUrl = useCallback(
    async (fileId: number) => {
      const res = await authFetch(`/v1/files/${fileId}`);
      if (!res.ok) throw new Error("Falha ao obter URL do arquivo");
      const data = await res.json();
      // Backend returns SignedUrlDTO: either short link or direct signed URL
      return (data?.signedUrl as string) || "";
    },
    [authFetch],
  );

  const getById = useCallback(
    (id: number) => files.find((v) => v.id === id),
    [files],
  );

  const value = useMemo(
    () => ({ files, refresh, upload, getFileSignedUrl, getById }),
    [files, refresh, upload, getFileSignedUrl, getById],
  );
  return (
    <FilesContext.Provider value={value}>{children}</FilesContext.Provider>
  );
}

export function useFiles() {
  const ctx = useContext(FilesContext);
  if (!ctx) throw new Error("useFiles must be used within FilesProvider");
  return ctx;
}
