## 📦 Configuração do Garage

Antes de tudo, este setup **depende do WSL (Windows Subsystem for Linux)**.

> ⚠️ **Importante:**  
> O binário do Garage (`linux/amd64`) **NÃO roda no Windows diretamente**.  
> Ele deve ser executado **dentro do WSL**.

---

## ✅ Pré-requisitos

- WSL instalado (ex: Ubuntu)
- Node.js instalado no Windows
- Garage rodando dentro do WSL

---

## 📥 Baixar o binário

1. Acesse a página de releases do Garage
2. Baixe o binário: `linux/amd64` para essa mesma pasta
3. Renomeie o arquivo para:

```bash
garage
```

## 🚀 Iniciar o servidor

Execute dentro do WSL:

```bash
./garage -c ./garage.toml server
```

> O servidor precisa estar rodando antes do setup

## 🔧 Rodar o setup

Agora, no Windows (ou no próprio WSL), execute:

> Instalar dependencias

```bash
yarn
```

> Executar script de setup

```bash
node setup-garage
```

Esse script irá:

1. Criar o bucket files (se não existir)
2. Criar/atualizar a app key
3. Aplicar permissões
4. Gerar o arquivo .env
5. Configurar CORS automaticamente

## Utilizar API KEY gerada

Substitua no arquivo `src\backend\src\main\resources\application.yml` as envs `S3_ACCESS_KEY`, `S3_SECRET_KEY` e `S3_ENDPOINT` pelos valores no `.env` gerado.
