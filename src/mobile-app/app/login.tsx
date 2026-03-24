import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "expo-router";
import React, { useState } from "react";
import {
  Alert,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

export default function LoginScreen() {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async () => {
    if (!email) return Alert.alert("Informe seu e-mail");
    setLoading(true);
    try {
      const result = await login(email, password);
      if (result?.requiresMfa) {
        // Navegar para tela de verificação MFA
        router.push({
          pathname: "/mfa-verify",
          params: { token: result.mfaToken },
        });
      } else {
        router.replace("/(tabs)/files");
      }
    } catch (e: any) {
      Alert.alert("Erro ao entrar", e?.message || "Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Entrar</Text>
      <TextInput
        placeholder="E-mail"
        keyboardType="email-address"
        autoCapitalize="none"
        autoCorrect={false}
        placeholderTextColor="#BFBFBF"
        value={email}
        onChangeText={setEmail}
        style={styles.input}
      />
      <TextInput
        placeholder="Senha"
        placeholderTextColor="#BFBFBF"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        style={styles.input}
      />
      <Pressable onPress={onSubmit} style={styles.button} disabled={loading}>
        <Text style={styles.buttonText}>
          {loading ? "Entrando..." : "Entrar"}
        </Text>
      </Pressable>
      <Pressable onPress={() => router.replace("/cadastro")}>
        <Text style={styles.link}>Não tem conta? Cadastre-se</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, gap: 12, backgroundColor: "#1B0B26" },
  title: { fontSize: 24, fontWeight: "700", marginBottom: 8, color: "#fff" },
  input: {
    borderWidth: 1,
    borderColor: "#444",
    color: "#fff",
    borderRadius: 8,
    padding: 12,
  },
  button: {
    backgroundColor: "#2ECC71",
    padding: 12,
    borderRadius: 8,
    alignItems: "center",
  },
  buttonText: { color: "#ffffff", fontWeight: "600" },
  link: { color: "#2ECC71", marginTop: 8, textDecorationLine: "underline" },
});
