import { DefaultTheme, ThemeProvider } from "@react-navigation/native";
import { useFonts } from "expo-font";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { View } from "react-native";
import "react-native-reanimated";
import { SafeAreaView } from "react-native-safe-area-context";

import AppHeader from "@/components/AppHeader";
import StackHeader from "@/components/StackHeader";
import { AuthProvider } from "@/contexts/AuthContext";
import { VideosProvider } from "@/store/videos";

if (__DEV__) {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  require("../ReactotronConfig");
}

export default function RootLayout() {
  const [loaded] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
  });

  if (!loaded) {
    // Async font loading only occurs in development.
    return null;
  }

  return (
    <ThemeProvider value={DefaultTheme}>
      <AuthProvider>
        <VideosProvider>
          <StatusBar style="light" />

          <SafeAreaView style={{ flex: 1, backgroundColor: "#1E1A21" }}>
            <AppHeader />

            <Stack
              initialRouteName="(tabs)"
              screenOptions={{
                header: (props) => (
                  <StackHeader title={props.options.title || ""} canGoBack />
                ),
              }}
            >
              <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
              <Stack.Screen name="video/[id]" options={{ title: "Vídeo" }} />
              <Stack.Screen name="login" options={{ title: "Login" }} />
              <Stack.Screen name="+not-found" />
              <Stack.Screen name="cadastro" options={{ title: "Cadastro" }} />
              <Stack.Screen name="profile" options={{ title: "Perfil" }} />
              <Stack.Screen
                name="mfa-verify"
                options={{ title: "Verificação MFA", headerShown: false }}
              />
            </Stack>
          </SafeAreaView>
        </VideosProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}
