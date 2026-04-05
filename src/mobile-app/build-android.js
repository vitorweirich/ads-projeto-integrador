const { execSync } = require("child_process");
const path = require("path");
const os = require("os");

function run(command, options = {}) {
  console.log(`\n> ${command}\n`);
  execSync(command, { stdio: "inherit", ...options });
}

/*
  Pegar fingerprint da chave de assinatura (a pasta android/app): 
  keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
*/

try {
  console.log("🚀 Iniciando build Android...");

  // Caminho padrão do SDK no Windows
  const androidHome =
    process.env.ANDROID_HOME ||
    path.join(os.homedir(), "AppData", "Local", "Android", "Sdk");

  console.log("ANDROID_HOME:", androidHome);

  const env = {
    ...process.env,
    ANDROID_HOME: androidHome,
    PATH: `${androidHome}\\platform-tools;${androidHome}\\emulator;${process.env.PATH}`,
    // Usado pra especificar uma versão mais recente do cmake
    CMAKE_VERSION: "4.1.2",
    EXPO_PUBLIC_API_URL:
      process.env.EXPO_PUBLIC_API_URL ||
      "https://native.files.vitorweirich.com",
    EXPO_PUBLIC_WEB_URL:
      process.env.EXPO_PUBLIC_WEB_URL || "https://files.vitorweirich.com",
  };

  // Gerar pasta android se necessário
  run("npx expo prebuild");

  const androidPath = path.join(process.cwd(), "android");

  if (process.platform === "win32") {
    run("gradlew assembleRelease", {
      cwd: androidPath,
      env,
      shell: "cmd.exe",
    });
  } else {
    run("./gradlew assembleRelease", {
      cwd: androidPath,
      env,
    });
  }

  console.log("\n✅ Build finalizado!");
} catch (error) {
  console.error("\n❌ Erro no build.");
  process.exit(1);
}
