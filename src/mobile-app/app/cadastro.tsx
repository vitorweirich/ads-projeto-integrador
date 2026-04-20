import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "expo-router";
import React, { useState, useMemo } from "react";
import {
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

export default function CadastroScreen() {
  const { register } = useAuth();
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [isRegistered, setIsRegistered] = useState(false);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState("");

  const hasMinLength = useMemo(() => password.length >= 6, [password]);
  const hasUppercase = useMemo(() => /[A-Z]/.test(password), [password]);
  const hasSpecialChar = useMemo(
    () => /[^A-Za-z0-9]/.test(password),
    [password],
  );
  const passwordValid = useMemo(
    () => hasMinLength && hasUppercase && hasSpecialChar,
    [hasMinLength, hasUppercase, hasSpecialChar],
  );

  const onSubmit = async () => {
    if (!name) return Alert.alert("Informe seu nome");
    if (!email) return Alert.alert("Informe seu e-mail");

    if (!passwordValid) {
      setError(
        "A senha deve ter ao menos 6 caracteres, incluir uma letra maiúscula e um caractere especial.",
      );
      return;
    }

    setLoading(true);
    setError("");
    try {
      await register(name, email, password);
      setIsRegistered(true);
    } catch (e: any) {
      if (e?.code === "PENDING_APPROVAL") {
        setIsPending(true);
      } else {
        setError(e?.message || "Ocorreu um erro durante o cadastro");
      }
    } finally {
      setLoading(false);
    }
  };

  if (isPending) {
    return (
      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.successContainer}
      >
        <Text style={styles.title}>Aguardando aprovação</Text>
        <View style={[styles.successBox, { borderColor: "#F39C12" }]}>
          <Text style={styles.successText}>
            Seu email está aguardando aprovação. Você será notificado quando for
            liberado.
          </Text>
        </View>
        <Pressable
          onPress={() => router.replace("/login")}
          style={styles.button}
        >
          <Text style={styles.buttonText}>Voltar para o Login</Text>
        </Pressable>
      </ScrollView>
    );
  }

  if (isRegistered) {
    return (
      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.successContainer}
      >
        <Text style={styles.title}>Cadastro realizado com sucesso!</Text>
        <View style={styles.successBox}>
          <Text style={styles.successText}>
            Enviamos um e-mail de confirmação para{" "}
            <Text style={styles.emailText}>{email}</Text>.
          </Text>
          <Text style={styles.successText}>
            Por favor, verifique sua caixa de entrada e siga as instruções para
            verificar sua conta.
          </Text>
        </View>
        <Text style={styles.readyText}>
          Pronto para começar? Verifique seu e-mail e depois faça login.
        </Text>
        <Pressable
          onPress={() => router.replace("/login")}
          style={styles.button}
        >
          <Text style={styles.buttonText}>Ir para o Login</Text>
        </Pressable>
      </ScrollView>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Crie sua conta</Text>
      <TextInput
        placeholder="Nome completo"
        placeholderTextColor="#BFBFBF"
        value={name}
        onChangeText={setName}
        style={styles.input}
      />
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

      {password.length > 0 && (
        <View style={styles.requirementsBox}>
          <Text style={styles.requirementsTitle}>Requisitos da senha</Text>
          <View style={styles.requirementsList}>
            <Text
              style={[
                styles.requirement,
                hasMinLength ? styles.valid : styles.invalid,
              ]}
            >
              {hasMinLength ? "✔" : "✖"} Mínimo 6 caracteres
            </Text>
            <Text
              style={[
                styles.requirement,
                hasUppercase ? styles.valid : styles.invalid,
              ]}
            >
              {hasUppercase ? "✔" : "✖"} Uma letra maiúscula
            </Text>
            <Text
              style={[
                styles.requirement,
                hasSpecialChar ? styles.valid : styles.invalid,
              ]}
            >
              {hasSpecialChar ? "✔" : "✖"} Um caractere especial
            </Text>
          </View>
        </View>
      )}

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable
        onPress={onSubmit}
        style={[
          styles.button,
          (!passwordValid || loading) && styles.buttonDisabled,
        ]}
        disabled={loading || !passwordValid}
      >
        <Text style={styles.buttonText}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </Text>
      </Pressable>

      <Pressable onPress={() => router.replace("/login")}>
        <Text style={styles.link}>Já tem uma conta? Faça login</Text>
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#1B0B26" },
  content: { padding: 16, gap: 12 },
  successContainer: { padding: 16, gap: 20, justifyContent: "center", flex: 1 },
  title: {
    fontSize: 24,
    fontWeight: "700",
    marginBottom: 8,
    color: "#fff",
    textAlign: "center",
  },
  input: {
    borderWidth: 1,
    borderColor: "#444",
    color: "#fff",
    borderRadius: 8,
    padding: 12,
  },
  requirementsBox: {
    backgroundColor: "#2D2828",
    borderRadius: 8,
    padding: 12,
    borderWidth: 1,
    borderColor: "#444",
  },
  requirementsTitle: {
    color: "#fff",
    fontSize: 14,
    fontWeight: "600",
    marginBottom: 8,
  },
  requirementsList: {
    gap: 4,
  },
  requirement: {
    fontSize: 13,
  },
  valid: {
    color: "#2ECC71",
  },
  invalid: {
    color: "#E74C3C",
  },
  error: {
    color: "#E74C3C",
    fontSize: 14,
  },
  button: {
    backgroundColor: "#2ECC71",
    padding: 12,
    borderRadius: 8,
    alignItems: "center",
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  buttonText: { color: "#ffffff", fontWeight: "600" },
  link: {
    color: "#2ECC71",
    marginTop: 8,
    textAlign: "center",
    textDecorationLine: "underline",
  },
  successBox: {
    backgroundColor: "#2D2828",
    borderRadius: 8,
    padding: 16,
    gap: 12,
  },
  successText: {
    color: "#fff",
    fontSize: 15,
    lineHeight: 22,
  },
  emailText: {
    fontWeight: "600",
    color: "#2ECC71",
  },
  readyText: {
    color: "#BFBFBF",
    fontSize: 15,
    textAlign: "center",
  },
});
