## 🚀 Setup Neovim (VPS) — rápido e confiável

### 1. Remover versão antiga (evita conflito)

sudo apt remove neovim -y

### 2. Instalar versão moderna (via snap)

sudo apt update
sudo apt install snapd -y
sudo snap install nvim --classic

### 3. Corrigir PATH (importante)

echo 'export PATH=$PATH:/snap/bin' >> ~/.bashrc
source ~/.bashrc
hash -r

### 4. Validar instalação

nvim --version

👉 Esperado:

NVIM v0.9+ (ou superior)

Se não funcionar:

/snap/bin/nvim --version

### 5. Criar estrutura de config

mkdir -p ~/.config/nvim
nvim ~/.config/nvim/init.lua

### 6. Colar sua configuração

vim.opt.number = true
vim.opt.relativenumber = false
vim.opt.tabstop = 2
vim.opt.shiftwidth = 2
vim.opt.expandtab = true
vim.opt.mouse = "a"
