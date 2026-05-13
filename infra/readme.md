## Infra

Essa pasta destina-se a documentar como provisionar a infra necessária para rodar esse projeto localmente.

## 1. A forma mais fácil é via Docker (porém consome mais recursos da máquina)

Para tal, tendo o Docker instalado, basta rodar o docker compose do projeto backend `src\backend\docker-compose.yml`

## 2. A segunda forma é instalar as 3 dependências diretamente na máquina

É a forma mais trabalhosa no primeiro setup, mas consome menos recursos.
Cada uma das dependências tem uma sub-pasta com seu próprio readme, abaixo um resumo de cada uma:

1. **garage**: Serviço de storage compatível com a API do S3
2. **mailpit**: Mock de envio de e-mails
3. **postgres**: Banco de dados PostgreSQL
