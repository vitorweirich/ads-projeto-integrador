import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "expo-router";
import React from "react";
import { Alert, Pressable, StyleSheet, Text, View } from "react-native";

export default function ProfileScreen() {
  const router = useRouter();
  const { user, logout } = useAuth();

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
  logoutButton: {
    backgroundColor: "#C0392B",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginTop: 16,
  },
  logoutText: {
    color: "#ffffff",
    fontWeight: "600",
    fontSize: 16,
  },
});
