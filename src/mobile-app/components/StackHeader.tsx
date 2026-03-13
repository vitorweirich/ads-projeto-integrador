import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";
import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

type StackHeaderProps = {
  title: string;
  canGoBack?: boolean;
};

export default function StackHeader({ title, canGoBack }: StackHeaderProps) {
  const router = useRouter();

  return (
    <View style={styles.container}>
      {canGoBack && (
        <Pressable onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#2ECC71" />
        </Pressable>
      )}
      <Text style={styles.title}>{title}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    backgroundColor: "#1E1A21",
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#444",
  },
  backButton: {
    marginRight: 12,
    padding: 4,
  },
  backText: {
    fontSize: 24,
    color: "#2ECC71",
    fontWeight: "600",
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    color: "#fff",
  },
});
