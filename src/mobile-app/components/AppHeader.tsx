import { useAuth } from "@/contexts/AuthContext";
import { useRouter, usePathname } from "expo-router";
import React from "react";
import { Alert, Pressable, StyleSheet, Text, View } from "react-native";

export default function AppHeader() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const handleProfilePress = () => {
    if (pathname !== "/profile") {
      router.push("/profile");
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>File Share</Text>
      {user ? (
        <View style={styles.right}>
          <Pressable
            onPress={handleProfilePress}
            style={styles.userPill}
            accessibilityRole="button"
          >
            <Text style={styles.buttonText}>{user.name}</Text>
          </Pressable>
          <Pressable
            onPress={() => {
              Alert.alert("Sair", "Deseja realmente sair?", [
                { text: "Cancelar", style: "cancel" },
                {
                  text: "Sair",
                  style: "destructive",
                  onPress: () => void logout(),
                },
              ]);
            }}
            style={styles.logoutButton}
            accessibilityRole="button"
          >
            <Text style={styles.logoutText}>Sair</Text>
          </Pressable>
        </View>
      ) : (
        <Pressable
          onPress={() => router.push({ pathname: "/login" as any })}
          style={styles.button}
          accessibilityRole="button"
        >
          <Text style={styles.buttonText}>Login</Text>
        </Pressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 16,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#1E1A21",
    backgroundColor: "#1E1A21",
    paddingBottom: 4,
  },
  right: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    color: "#2ECC71",
  },
  button: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: "#2D2828",
  },
  userPill: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: "#2D2828",
  },
  buttonText: {
    color: "#ffffff",
    fontWeight: "600",
  },
  logoutButton: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: "#C0392B",
  },
  logoutText: {
    color: "#ffffff",
    fontWeight: "600",
  },
});
