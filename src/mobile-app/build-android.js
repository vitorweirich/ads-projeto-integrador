const { execSync } = require("child_process");
const path = require("path");

function run(command, options = {}) {
  console.log(`\n> ${command}\n`);
  execSync(command, { stdio: "inherit", ...options });
}

try {
  console.log("🚀 Iniciando build Android...");

  // Só herda o ambiente atual (WSL ou Windows)
  const env = {
    ...process.env,
    EXPO_PUBLIC_API_URL:
      process.env.EXPO_PUBLIC_API_URL ||
      "https://native.files.vitorweirich.com",
    EXPO_PUBLIC_WEB_URL:
      process.env.EXPO_PUBLIC_WEB_URL ||
      "https://files.vitorweirich.com",
  };

  // Gera pasta android
  run("npx expo prebuild", { env });

  const androidPath = path.join(process.cwd(), "android");

  // Detecta plataforma corretamente
  const isWindows = process.platform === "win32";

  run(isWindows ? "gradlew assembleRelease" : "./gradlew assembleRelease", {
    cwd: androidPath,
    env,
    ...(isWindows && { shell: "cmd.exe" }),
  });

  console.log("\n✅ Build finalizado!");
} catch (error) {
  console.error("\n❌ Erro no build.");
  process.exit(1);
}