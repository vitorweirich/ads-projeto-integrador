## TODOs

---

### Geral

- [ ] Rever todos os arquivos de config/envs (docker-compose, application.yml, .env, Dockerfile)
- [ ] Considerar usar https://zotregistry.dev/v2.1.15/ como Docker Registry

---

### App

- [ ] Ajustar readme (src\mobile-app\README.md) com links atualizados (documentacao)
- [ ] Tornar a funcionalidade de upload e visualização pra suportar varios formatos de arquivos
- [ ] Remover modulos do expo que não estao sendo usados
- [ ] Corrigir warning
  > [expo-av]: Video component from `expo-av` is deprecated in favor of `expo-video`. See the documentation at https://docs.expo.dev/versioons/latest/sdk/video/ for the new API reference.
- [x] Corrigir TODOs comentados no codigo
  > OBS: Ainda existem TODOs, mas devem ficar para uma evolução futura
- [ ] (Opcional) Configurar AppLinks para abrir algumas paginas do site automaticamente caso o APP esteja instalado
  > Tem como pré requisito que o site esteja deployado em PRD com https, de outra forma não será possível testar
- [ ] Disponibilizar APK do APP para download no no site
  > Possivelmente colocar no storage de arquivos para evitar aumentar o tamanho da imagem docker do site.
  > Para isso temos algumas opções:
  1. Simplesmente criar enviar o APK manualmente no storage
  2. Criar a funcionalidade de ter uploads permanetes e usar o proprio sistema pra enviar o APK, porém isso requer bastante trabalho, visto que seria necessário também implementar uma forma de compartilhar o video com todos os usuarios (deixar publico, acessível sem pre-signed url, possivelmente configurar um bucket separado devido as policies necessárias)
  3. Se tiver com muita vontade, criar uma pagina de listagem de arterfatos, com várias versões do APP (talvez um seção especial listando todos os arquivos publicos)??
     OBS: As opções 2 e 3, seria possívelmente uma v3 ou v4 do APP, pois são muito complexas/trabalhosas de serem implementadas da forma correta

---

### Web

- [x] Tornar interfaces de admin no web minimamente responsivas
  > Implementado layout responsivo com cards em mobile e tabela em desktop para AdminUsersView e AdminFilesView. Header também responsivo com 3 tiers (mobile/intermediário/desktop).
- [x] Corrigir TODOs comentados no codigo
  > OBS: Ainda existem TODOs, mas devem ficar para uma evolução futura
- [] Melhorar estilização do menu humburguer (espaçamento a esquerda, botão sair) e possivelmente move os dados sobre uso de armazenamento para fora, talvez de uma forma mais resumida? ou priorizar sobre outro link/seletor de tema?

---

### Backend

- [x] Adicionar @PreAuthorize nos endpoints de Admin
  > Já existe, nesse projeto o usuario é considerado administrador se o email esta na lista de emails de admin e não baseado em roles
- [x] Corrigir TODOs comentados no codigo
  > OBS: Ainda existem TODOs, mas devem ficar para uma evolução futura
