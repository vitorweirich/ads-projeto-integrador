import { useAuth } from "@/contexts/AuthContext";
import { useLocalSearchParams, useRouter } from "expo-router";
import React, { useState } from "react";
import {
  Alert,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

export default function MfaVerifyScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const mfaToken = params.token as string;
  const { verifyMfa } = useAuth();

  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async () => {
    if (!code || code.length !== 6) {
      return Alert.alert("Erro", "Informe o código de 6 dígitos");
    }

    setLoading(true);
    try {
      await verifyMfa(mfaToken, code);
      Alert.alert("Sucesso", "Login realizado com sucesso!", [
        {
          text: "OK",
          onPress: () => router.replace("/(tabs)/videos"),
        },
      ]);
    } catch (e: any) {
      Alert.alert("Erro", e?.message || "Código inválido. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Verificação MFA</Text>
      <Text style={styles.subtitle}>
        Digite o código de 6 dígitos do seu aplicativo autenticador
      </Text>
      <TextInput
        placeholder="000000"
        keyboardType="number-pad"
        maxLength={6}
        placeholderTextColor="#BFBFBF"
        value={code}
        onChangeText={setCode}
        style={styles.input}
      />
      <Pressable onPress={onSubmit} style={styles.button} disabled={loading}>
        <Text style={styles.buttonText}>
          {loading ? "Verificando..." : "Verificar"}
        </Text>
      </Pressable>
      <Pressable
        onPress={() => router.back()}
        style={styles.cancelButton}
        disabled={loading}
      >
        <Text style={styles.cancelText}>Cancelar</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    gap: 12,
    backgroundColor: "#1B0B26",
    justifyContent: "center",
  },
  title: {
    fontSize: 24,
    fontWeight: "700",
    marginBottom: 8,
    color: "#fff",
    textAlign: "center",
  },
  subtitle: {
    fontSize: 14,
    color: "#BFBFBF",
    textAlign: "center",
    marginBottom: 16,
  },
  input: {
    borderWidth: 1,
    borderColor: "#444",
    color: "#fff",
    borderRadius: 8,
    padding: 12,
    fontSize: 24,
    textAlign: "center",
    letterSpacing: 8,
  },
  button: {
    backgroundColor: "#2ECC71",
    padding: 12,
    borderRadius: 8,
    alignItems: "center",
  },
  buttonText: { color: "#ffffff", fontWeight: "600" },
  cancelButton: {
    padding: 12,
    alignItems: "center",
  },
  cancelText: { color: "#BFBFBF" },
});
