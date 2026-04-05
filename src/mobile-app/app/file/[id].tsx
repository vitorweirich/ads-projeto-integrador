import { useFiles } from "@/store/files";
import { ResizeMode, Video } from "expo-av";
import { useLocalSearchParams } from "expo-router";
import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Image,
  Linking,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

export default function FileViewScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { getById, getFileSignedUrl } = useFiles();
  const item = useMemo(
    () => (id ? getById(Number(id)) : undefined),
    [id, getById],
  );
  const [playUrl, setPlayUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      if (!id) return;
      try {
        setLoading(true);
        const url = await getFileSignedUrl(Number(id));
        if (mounted) setPlayUrl(url);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [getFileSignedUrl, id]);

  if (!item)
    return (
      <View style={styles.center}>
        <Text style={styles.notFound}>Arquivo não encontrado.</Text>
      </View>
    );

  const contentType = item.contentType || "";
  const isVideo = contentType.startsWith("video/");
  const isImage =
    contentType.startsWith("image/") && contentType !== "application/pdf";
  const isPdf = contentType === "application/pdf";

  const openInBrowser = () => {
    if (playUrl) Linking.openURL(playUrl);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Nome: {item.title}</Text>
      {loading && <ActivityIndicator color="#fff" style={{ marginTop: 16 }} />}

      {playUrl && isVideo && (
        <Video
          source={{
            uri: playUrl,
          }}
          style={styles.player}
          useNativeControls
          resizeMode={ResizeMode.CONTAIN}
        />
      )}

      {playUrl && isImage && (
        <Image
          source={{ uri: playUrl }}
          style={styles.image}
          resizeMode="contain"
        />
      )}

      {playUrl && isPdf && (
        <View style={styles.pdfFallback}>
          <Text style={styles.pdfText}>
            Visualização de PDF não disponível no app.
          </Text>
          <Pressable style={styles.openButton} onPress={openInBrowser}>
            <Text style={styles.openButtonText}>Abrir no navegador</Text>
          </Pressable>
        </View>
      )}

      {playUrl && !isVideo && !isImage && !isPdf && (
        <View style={styles.pdfFallback}>
          <Text style={styles.pdfText}>
            Visualização não disponível para este tipo de arquivo.
          </Text>
          <Pressable style={styles.openButton} onPress={openInBrowser}>
            <Text style={styles.openButtonText}>Abrir no navegador</Text>
          </Pressable>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#000" },
  title: { color: "#fff", fontSize: 18, fontWeight: "600", padding: 12 },
  player: { flex: 1 },
  image: { flex: 1, width: "100%" },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  notFound: { color: "#fff", fontSize: 16 },
  pdfFallback: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
  },
  pdfText: { color: "#BFBFBF", fontSize: 16, textAlign: "center" },
  openButton: {
    marginTop: 16,
    backgroundColor: "#2ECC71",
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  openButtonText: { color: "#fff", fontWeight: "600", fontSize: 16 },
});
