import { execSync } from "child_process";
import fs from "fs";
import dotenv from "dotenv";
import { S3Client, HeadBucketCommand } from "@aws-sdk/client-s3";

// ===== CONFIG =====
const BUCKET = "files";
const ENV_FILE = ".env";

// ===== HELPERS =====
function runWsl(cmd) {
  return execSync(`wsl ${cmd}`).toString().trim();
}

function safeRun(cmd) {
  try {
    return runWsl(cmd);
  } catch {
    return null;
  }
}

// =========================
// VALIDAÇÃO DO .env
// =========================
async function validateEnv() {
  if (!fs.existsSync(ENV_FILE)) {
    return false;
  }

  console.log("🔍 .env encontrado, validando...");

  dotenv.config({ quiet: true });

  const { S3_ENDPOINT, S3_ACCESS_KEY, S3_SECRET_KEY, S3_BUCKET } = process.env;

  if (!S3_ENDPOINT || !S3_ACCESS_KEY || !S3_SECRET_KEY || !S3_BUCKET) {
    console.log("❌ .env incompleto");
    return false;
  }

  try {
    const client = new S3Client({
      endpoint: S3_ENDPOINT,
      region: "garage",
      credentials: {
        accessKeyId: S3_ACCESS_KEY,
        secretAccessKey: S3_SECRET_KEY,
      },
      forcePathStyle: true,
    });

    await client.send(
      new HeadBucketCommand({
        Bucket: S3_BUCKET,
      }),
    );

    console.log("✅ .env válido, conexão OK");
    return true;
  } catch (err) {
    console.error("❌ Erro de conexão:", err.message);
    console.log("❌ Credenciais inválidas ou bucket não acessível");
    return false;
  }
}

// =========================
// SETUP
// =========================
function setupGarage() {
  console.log("🚀 Executando setup...");

  // BUCKET
  console.log("📦 Verificando bucket...");
  const bucketList = runWsl("./garage -c ./garage.toml bucket list");

  if (!bucketList.includes(BUCKET)) {
    console.log("➕ Criando bucket...");
    runWsl(`./garage -c ./garage.toml bucket create ${BUCKET}`);
  } else {
    console.log("✔ Bucket já existe");
  }

  // KEY
  console.log("🔑 Verificando keys...");
  const keyList = runWsl("./garage -c ./garage.toml key list");

  let existingKeyId = null;

  for (const line of keyList.split("\n")) {
    if (line.includes("app-files-key")) {
      const match = line.match(/^(\S+)/);
      if (match) {
        existingKeyId = match[1];
        break;
      }
    }
  }

  let accessKeyId;
  let secretAccessKey;

  if (existingKeyId) {
    console.log("✔ Key já existe, recriando (não temos secret)...");

    // remove e recria (pra garantir secret)
    runWsl(`./garage -c ./garage.toml key delete ${existingKeyId} --yes`);

    const output = runWsl(`./garage -c ./garage.toml key create app-files-key`);

    accessKeyId = output.match(/Key ID:\s*(\S+)/)?.[1];
    secretAccessKey = output.match(/Secret key:\s*(\S+)/)?.[1];
  } else {
    console.log("➕ Criando nova key...");

    const output = runWsl(`./garage -c ./garage.toml key create app-files-key`);

    accessKeyId = output.match(/Key ID:\s*(\S+)/)?.[1];
    secretAccessKey = output.match(/Secret key:\s*(\S+)/)?.[1];
  }

  // PERMISSÕES
  console.log("🔗 Aplicando permissões...");
  safeRun(
    `./garage -c ./garage.toml bucket allow --read --write --owner ${BUCKET} --key ${accessKeyId}`,
  );

  // ENV
  console.log("💾 Salvando .env...");

  const env = `
S3_ENDPOINT=http://localhost:3900
S3_BUCKET=${BUCKET}
S3_ACCESS_KEY=${accessKeyId}
S3_SECRET_KEY=${secretAccessKey}
`;

  fs.writeFileSync(ENV_FILE, env.trim());

  console.log("✅ Setup concluído");
}

// =========================
// MAIN
// =========================
async function main() {
  const valid = await validateEnv();

  if (valid) {
    console.log("🚫 Setup ignorado (já está tudo OK)");
    return;
  }

  setupGarage();
}

async function validateGarageRunning() {
  console.log("🔍 Verificando se o Garage está rodando...");

  try {
    await fetch("http://localhost:3900", {
      method: "GET",
    });

    // qualquer resposta já indica que o servidor está vivo
    console.log("✅ Garage está rodando");
    return true;
  } catch (err) {
    console.error("❌ Garage não está acessível em http://localhost:3900");
    console.error(
      "👉 Certifique-se de que o servidor está rodando!! (rode './garage -c ./garage.toml server' em outro terminal)",
    );
    return false;
  }
}

async function mainWrapper() {
  console.log("🔧 Iniciando setup do Garage...");

  const isRunning = await validateGarageRunning();

  if (!isRunning) {
    console.log("🚫 Abortando setup");
    process.exit(1);
  }

  await main();

  console.log("🎉 Setup do Garage finalizado!");
  console.log("🔧 Configurando CORS...");

  execSync(`node configure-cors.js`, { stdio: "ignore" });

  console.log("🔧 Configuração de CORS finalizada!...");
}

mainWrapper().catch((err) => {
  console.error("❌ Erro:", err);
});
