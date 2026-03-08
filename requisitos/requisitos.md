# Projeto Aplicativo Mobile: FileShare

## 1. Requisitos do Aplicativo

### 1.1. Requisitos Funcionais

- **Autenticação de Usuário:**
  - **Login:** Permitir que usuários existentes façam login com e-mail e senha.
  - **Cadastro:** Permitir que novos usuários se cadastrem fornecendo e-mail e senha (com confirmação de senha).

- **Gerenciamento de Arquivos:**
  - **Listagem de Arquivos:** Exibir uma lista dos Arquivos do usuário (Meus Arquivos).
  - **Visualização de Arquivos:** Permitir a visualização de um arquivo selecionado, exibindo título e visualização se existir (nem todos os tipos de arquivos terão como serem visualizados).
  - **Envio de Arquivo:** Permitir que o usuário selecione um arquivo do dispositivo, forneça um título e o envie para a plataforma.

### 1.2. Requisitos Não Funcionais

- **Usabilidade:** A interface deve ser intuitiva e fácil de usar.
- **Desempenho:** O aplicativo deve ser responsivo e carregar informações rapidamente.
- **Segurança:** Credenciais e dados dos usuários devem ser protegidos.
- **Compatibilidade:** O aplicativo deve ser compatível com iOS e Android.

## 2.Escolha da Plataforma de Desenvolvimento e Tecnologias

---

### 2.1. Plataforma do Aplicativo Mobile: React Native

- **Desenvolvimento Multiplataforma:** Permite criar aplicativos para iOS e Android a partir de uma única base de código, reduzindo tempo e custos de desenvolvimento. Como o objetivo é atingir o maior número de usuários, essa é a melhor escolha.
- **Familiaridade com JavaScript/TypeScript:** A equipe (no caso, eu) já possui experiência com essas linguagens, diminuindo a curva de aprendizado.
- **Desempenho Próximo ao Nativo:** Embora seja uma estrutura híbrida, o React Native utiliza componentes nativos. Apesar de não alcançar a performance de um desenvolvimento totalmente nativo, considerando a falta de experiência em iOS e Android nativos, essa abordagem tende a resultar em um desempenho melhor no projeto.
- **Ecossistema Rico e Comunidade Ativa:** A vasta quantidade de bibliotecas e a comunidade ativa facilitam o desenvolvimento e a resolução de problemas.
- **Componentização:** A arquitetura baseada em componentes favorece modularidade, reuso de código e manutenção mais simples, além de facilitar a evolução do aplicativo.

### 2.2. Plataforma do Site (Frontend Web): Vue.js

Para o desenvolvimento do site, foi escolhida a plataforma **Vue.js**:

- **Desenvolvimento Rápido e Produtivo:** Vue.js é conhecido por sua simplicidade e curva de aprendizado baixa, permitindo prototipação e desenvolvimento ágil.
- **Componentização:** Assim como React Native, Vue.js favorece modularidade e reuso de código.
- **Ecossistema e Comunidade:** Possui uma comunidade ativa e diversas bibliotecas para facilitar integrações e funcionalidades.
- **Performance:** Vue.js oferece boa performance para aplicações SPA (Single Page Application).

  > E claro, além de tudo isso, o Vue.js ajuda a evitar aquela dor de cabeça de lidar com o React (quem nunca se perdeu nos hooks que atire a primeira pedra!).

### 2.3. Plataforma do Backend (API): Spring Boot

Para a API backend, foi escolhido o **Spring Boot**:

- **Robustez e Escalabilidade:** Spring Boot é uma plataforma madura para desenvolvimento de APIs REST, com suporte a escalabilidade e segurança.
- **Integração com Banco de Dados:** Facilita integração com bancos relacionais como PostgreSQL.
- **Documentação e Comunidade:** Ampla documentação e comunidade ativa.
- **Produtividade:** Permite desenvolvimento rápido de APIs com configuração simplificada.
  - **Gosto pessoal e conhecimento:** A escolha do Spring Boot também foi motivada pelo gosto pessoal e familiaridade com o ecossistema Spring, o que facilita o desenvolvimento e manutenção da API.

### 2.4. Plataforma de Armazenamento de Arquivos: Object Storage S3

Para o armazenamento de arquivos, será utilizado um serviço compatível com a API S3 (Amazon S3, Cloudflare R2 ou MinIO):

- **Escalabilidade:** Permite armazenar grandes volumes de arquivos de forma eficiente.
- **Compatibilidade:** A API S3 é padrão de mercado, facilitando integrações.
- **Segurança:** Oferece recursos de controle de acesso e proteção de dados.
- **Custo:** Opções como MinIO permitem implantação local, reduzindo custos.

### 2.5. Banco de Dados: PostgreSQL

Para o armazenamento dos dados estruturados, será utilizado o **PostgreSQL**:

- **Confiabilidade e Robustez:** PostgreSQL é um dos bancos de dados relacionais mais confiáveis e utilizados no mercado.
- **Recursos Avançados:** Suporte a transações, integridade referencial, extensões e tipos de dados avançados.
- **Open Source:** Sem custos de licença, com grande comunidade e documentação.
- **Integração:** Fácil integração com Spring Boot e compatível com serviços de armazenamento em nuvem.
