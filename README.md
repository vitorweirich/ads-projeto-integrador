# Projeto Integrador

- Aluno(a): Vitor Mateus Weirich (weirichvitor@gmail.com)
- Código: 369881
- Turma: EAD54-12
- Professor(a): Alysson Borges
- Disciplina: 30062 - PROJETO INTEGRADOR - EAD54-12

**Durante o período de avaliação do projeto, ficara disponível uma [réplica das documentações .md desse repositório de forma agrupada.](https://wiki.vitorweirich.com/share/f9698ur6i5/p/projeto-integrador-UngfEM3xJ7)**

Este repositório contém o desenvolvimento completo de uma [**plataforma de compartilhamento temporário de arquivos**](https://files.vitorweirich.com/), criada como Projeto Integrador do curso de Análise e Desenvolvimento de Sistemas.

> Nessa versão inicial o sistema suporta os seguintes tipos de arquivo:
>
> - "image/png"
> - "image/jpeg"
> - "video/mp4"
> - "video/avi"
> - "video/mkv"
> - "video/x-matroska"
> - "video/mov"
> - "video/quicktime"
> - "video/wmv"
> - "video/ts"
> - "application/pdf"

O projeto abrange todas as etapas do desenvolvimento de um sistema, incluindo levantamento de requisitos, modelagem do negócio, documentação técnica e implementação da aplicação.

A solução é composta por um frontend web responsivo desenvolvido em Vue.js, um aplicativo mobile desenvolvido com React Native (Expo) e uma API backend em Spring Boot, utilizando PostgreSQL como banco de dados. Para o armazenamento de arquivos é utilizado um object storage compatível com a API S3 (como Amazon S3, Cloudflare R2 ou MinIO).

Além do código-fonte, o repositório também inclui documentação do projeto e diagramas de modelagem, como diagramas UML e diagramas de banco de dados, que descrevem a arquitetura e o funcionamento do sistema.

## FileShare

Projeto acadêmico para planejar e construir uma plataforma de upload, listagem e compartilhamento de arquivos com acesso autenticado.

## Desenvolvimento Local

Para instruções detalhadas sobre configuração do ambiente, dependências, execução dos serviços e fluxo de desenvolvimento, consulte:

- [Guia de Desenvolvimento](./DEVELOPMENT.md)

## Documentacao Organizada

### Requisitos e decisões de plataforma e tecnologias

Documento com escopo funcional e não funcional do sistema, juntamente das plataformas e tecnologias escolhidas.

- [Requisitos do sistema](./docs/requisitos/requisitos.md)

### API (Swagger)

[API Docs (auto hospedado)](https://files-api.vitorweirich.com/swagger/index.html)

[API Docs (Swagger Editor)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/vitorweirich/ads-projeto-integrador/refs/heads/master/src/backend/src/main/resources/static/swagger/openapi.yaml)

### Protótipo (APP Mobile)

Wireframes e fluxo visual simplificado das telas principais do aplicativo.

- [Protótipo de layout mobile](./docs/prototipo-layout-mobile-simplificado/prototipo-layout-mobile.md)

### Canvas do modelo de negócio

![Canvas do modelo de negócio](./docs/canvas-do-modelo-de-negocio.png)

### Diagramas

Centralização dos diagramas do projeto.

- [Ver README de diagramas](./docs/diagramas/README.md)

## Demonstrações

### App mobile

Link para apresentação do projeto no [YouTube](https://www.youtube.com/watch?v=enwT61He6Dw)

> OBS: Essa apresentação trata-se do projeto base, onde a funcionalidade de upload estava restrita a vídeos, porém todos os conceitos apresentados se mantêm.

### Testes automatizados (Cypress)

Link de apresentação dos testes automatizados com Cypress: https://www.youtube.com/watch?v=XLV8zDDAS74

### Site (Sistema no geral)

Link apresentação geral do projeto https://www.youtube.com/watch?v=-_H_0ihZt8I
