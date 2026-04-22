
# Setup Android + React Native no WSL (passo a passo)

## 1. Atualizar sistema

```bash
sudo apt update
sudo apt upgrade -y
```

## 2. Instalar dependências básicas

```bash
sudo apt install -y \
  curl \
  unzip \
  git \
  build-essential \
  cmake \
  ninja-build \
  pkg-config \
  openjdk-17-jdk
```

## 3. Configurar JAVA_HOME (verificar se paths estão corretos)

```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

**Verificar:**

```bash
java -version
echo $JAVA_HOME
```

## 4. Instalar NVM + Node.js 22 (buscar link atualizado no repo oficial do nvm)

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc

nvm install 22
nvm use 22
nvm alias default 22
```

**Verificar:**

```bash
node -v
npm -v
```

## 5. Criar estrutura do Android SDK

```bash
mkdir -p ~/Android/Sdk
cd ~/Android
```

## 6. Baixar command line tools (ir no site oficial https://developer.android.com/studio?hl=pt-br e baixar a versão correta)
> Scrole até o final da pagina, tem uma sessão 'Somente ferramentas de linha de comando', baixe a versão linux (wsl)

```bash
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip

mkdir -p ~/Android/Sdk/cmdline-tools/latest
mv cmdline-tools/* ~/Android/Sdk/cmdline-tools/latest/
```

## 7. Configurar variáveis de ambiente (Android)

```bash
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export ANDROID_SDK_ROOT=$ANDROID_HOME' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.bashrc

source ~/.bashrc
```

**Verificar:**

```bash
echo $ANDROID_HOME
```

## 8. Aceitar licenças do SDK

```bash
yes | sdkmanager --licenses
```

## 9. Instalar componentes do Android SDK
> IMPORTANTE: isso só deve ser feito como fallback, ao rodar o build, as dependencias devem ser baixadas automaticamente

```bash
sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "cmake;3.22.1" \
  "ndk;26.1.10909125"
```

**Verificar:**

```bash
sdkmanager --list | grep installed
```
