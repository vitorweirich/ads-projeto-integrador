
# Como rodar o projeto localmente

## Requisitos

- **Docker** (recomendado: 24.x ou superior)
- **Java 17** (ou superior)
- **Node.js** (recomendado: 20.x ou superior)
- **Yarn** (recomendado: 1.22.x ou superior)
- **Maven** (opcional, recomendado: 3.9.x ou superior; pode usar uma IDE como alternativa)

> Certifique-se de que todas as dependências estejam instaladas e acessíveis no terminal.

## 1. Subir a infraestrutura (Postgres, Garage, Mailpit)

```sh
cd src/backend

docker compose up -d
```

## 2. Rodar o backend

```sh
cd src/backend

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> Alternativamente, use sua IDE favorita para rodar a aplicação principal (`FileShareApplication.java`).

## 3. Rodar o frontend web

```sh
cd src/frontend-web

yarn

yarn dev
```

Acesse em http://localhost:5173

