import { WEB_URL } from "@/constants/api";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "expo-router";
import React, { useState } from "react";
import {
  Alert,
  Linking,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

export default function ProfileScreen() {
  const router = useRouter();
  const { user, logout, authFetch } = useAuth();

  const [loadingTarget, setLoadingTarget] = useState<string | null>(null);

  React.useEffect(() => {
    if (!user) {
      router.replace("/login");
    }
  }, [user, router]);

  if (!user) {
    return null;
  }

  const handleLogout = () => {
    Alert.alert("Sair", "Deseja realmente sair?", [
      { text: "Cancelar", style: "cancel" },
      {
        text: "Sair",
        style: "destructive",
        onPress: async () => {
          await logout();
          router.replace("/login");
        },
      },
    ]);
  };

  const formatStorage = () => {
    if (!user.storage) return "N/A";
    const usedMB = (user.storage.usedQuota / (1024 * 1024)).toFixed(2);
    const limitMB = (user.storage.totalQuota / (1024 * 1024)).toFixed(2);
    return `${usedMB} MB / ${limitMB} MB`;
  };

  const handleOpenWebPage = async (target: string, errorMessage: string) => {
    setLoadingTarget(target);
    try {
      const res = await authFetch("/v1/api/auth/session-transfer", {
        method: "POST",
        body: JSON.stringify({ target: "WEB" }),
      });

      if (!res.ok) {
        throw new Error("Falha ao gerar token de sessão");
      }

      const data = await res.json();
      const url = `${WEB_URL}/session-transfer/${data.transferToken}?destination=${encodeURIComponent(target)}`;

      await Linking.openURL(url);
    } catch {
      Alert.alert("Erro", errorMessage);
    } finally {
      setLoadingTarget(null);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <View style={styles.section}>
          <Text style={styles.label}>Nome</Text>
          <Text style={styles.value}>{user.name}</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>E-mail</Text>
          <Text style={styles.value}>{user.email}</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>MFA</Text>
          <Text style={styles.value}>
            {user.mfaEnabled ? "Ativado" : "Desativado"}
          </Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Privilégios de Admin</Text>
          <Text style={styles.value}>
            {user.hasAdminPrivileges ? "Sim" : "Não"}
          </Text>
        </View>

        {user.storage && (
          <View style={styles.section}>
            <Text style={styles.label}>Armazenamento</Text>
            <Text style={styles.value}>{formatStorage()}</Text>
          </View>
        )}

        <Pressable
          onPress={() =>
            handleOpenWebPage(
              "/profile",
              "Não foi possível abrir o gerenciamento de conta",
            )
          }
          style={styles.manageButton}
          disabled={loadingTarget !== null}
        >
          <Text style={styles.manageText}>
            {loadingTarget === "/profile" ? "Abrindo..." : "Gerenciar Conta"}
          </Text>
        </Pressable>

        {user.hasAdminPrivileges && (
          <>
            <Pressable
              onPress={() =>
                handleOpenWebPage(
                  "/admin/files",
                  "Não foi possível abrir a administração de arquivos",
                )
              }
              style={styles.adminButton}
              disabled={loadingTarget !== null}
            >
              <Text style={styles.adminText}>
                {loadingTarget === "/admin/files"
                  ? "Abrindo..."
                  : "Administrar Arquivos"}
              </Text>
            </Pressable>

            <Pressable
              onPress={() =>
                handleOpenWebPage(
                  "/admin/users",
                  "Não foi possível abrir a administração de usuários",
                )
              }
              style={styles.adminButton}
              disabled={loadingTarget !== null}
            >
              <Text style={styles.adminText}>
                {loadingTarget === "/admin/users"
                  ? "Abrindo..."
                  : "Administrar Usuários"}
              </Text>
            </Pressable>
          </>
        )}

        <Pressable onPress={handleLogout} style={styles.logoutButton}>
          <Text style={styles.logoutText}>Sair da Conta</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#1B0B26",
  },
  content: {
    padding: 16,
    gap: 16,
  },
  section: {
    backgroundColor: "#2D2828",
    padding: 16,
    borderRadius: 8,
  },
  label: {
    fontSize: 12,
    color: "#BFBFBF",
    marginBottom: 4,
  },
  value: {
    fontSize: 16,
    color: "#fff",
    fontWeight: "500",
  },
  manageButton: {
    backgroundColor: "#2D7856",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginTop: 16,
  },
  manageText: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
  adminButton: {
    backgroundColor: "#3B5998",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginTop: 8,
  },
  adminText: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
  logoutButton: {
    backgroundColor: "#C0392B",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginTop: 8,
  },
  logoutText: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
});
