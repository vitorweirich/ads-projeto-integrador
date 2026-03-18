## Infra

Essa pasta destina-se a documentar como provisionar a infra necessaria para rodar esse projeto localmente.

## 1. A forma mais fácil é via docker (porém consome mais recursos da maquina)

Para tal, tendo o docker instalado, basta rodar o docker compose do projeto backend `src\backend\docker-compose.yml`

## 2. A segunda forma é instalar as 3 dependecias diratamente na maquina

É a forma mais trabalhosa no primeiro setup, mas consome menos recursos.
Cada uma das dependencias tem uma sub-pasta com seu próprio readme, abaixa um resumo de cada uma:

1. **garage**: Serviço de storage compativel com a API do s3
2. **mailpit**: Mock de envio de emails
3. **postgres**: Banco de dados postgres
