<template>
  <main class="constainer mx-auto flex-col px-4 py-8">
    <section class="mb-12 flex-col text-center">
      <h1 class="mb-4 text-4xl font-bold">Compartilhe Arquivos Temporários com Facilidade</h1>
      <p class="mb-8 text-xl">
        Envie e compartilhe arquivos que expiram após um período, de forma rápida e segura. Os
        arquivos serão acessíveis apenas para as pessoas que você conceder acesso ou através do link
        compartilhado.
      </p>
      <router-link to="/upload" class="btn rounded-lg px-6 py-3 text-lg font-semibold">
        Compartilhar Agora
      </router-link>
    </section>

    <section class="mb-12 grid grid-cols-1 gap-8 md:grid-cols-2">
      <div class="rounded-lg p-6">
        <h2 class="mb-4 text-xl font-bold">Envie Arquivos Temporários</h2>
        <p class="">
          Compartilhe arquivos que ficarão disponíveis apenas por tempo limitado. Ideal para
          conteúdos rápidos, privados e acessíveis somente para quem você escolher ou quem receber o
          link.
        </p>
      </div>

      <div class="rounded-lg p-6">
        <h2 class="mb-4 text-xl font-bold">Compartilhamento Seguro</h2>
        <p class="">
          Seus arquivos são protegidos e excluídos automaticamente após o prazo definido.
          Compartilhe com tranquilidade.
        </p>
      </div>
    </section>
  </main>
</template>
