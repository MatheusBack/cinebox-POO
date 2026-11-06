# Roteiro de validação no seu ambiente

**Não executado nesta entrega.** Não foram iniciados Java, Maven, banco, navegador ou servidor, nem houve download de dependências. O que segue é o roteiro para você validar o código.

## Sete cenários principais

1. **Cadastro/login:** execute o projeto, abra `/cadastro` e crie a conta A. Entre com e-mail/senha. Deve ir ao perfil com nome e apelido de A. Senha errada deve voltar ao login com mensagem genérica.
2. **Acesso anônimo:** em janela anônima, abra `/filmes`, `/avaliacoes`, `/listas` e `/perfil`. Todas devem exigir login. Login, cadastro e CSS/imagens devem abrir normalmente.
3. **Avaliação sem autor manual:** com A, abra Registrar filme. Só há filme, nota, data e comentário; não há pessoa/autor/ID oculto de autor. Salve nota 4,5 para um filme.
4. **Autoria correta:** confira o diário e o detalhe desse filme. A avaliação deve pertencer a A. Tentar enviar `usuarioId` de B manualmente não pode mudar o autor.
5. **Perfil individual:** Meu perfil deve mostrar somente os filmes de A, com capa, título e sua nota. O link do filme deve abrir o detalhe. Conta nova sem avaliação deve mostrar estado vazio.
6. **Logout:** no perfil, clique Sair da conta. Deve voltar ao login e `/perfil` deve passar a exigir autenticação. Logout é POST com CSRF, não GET.
7. **Outra conta:** crie B, entre e avalie outro filme. B vê apenas suas avaliações no perfil/diário. Saia, entre novamente como A e confira que as sessões e os filmes não se misturaram.

## Permissões e compatibilidade

- Com B, tente abrir ou enviar edição/exclusão de uma avaliação de A pelo ID. Deve negar (404) e preservar nota/autor.
- Faça o mesmo em lista/item de A. B pode ler a lista, mas não alterar ou transferir autoria.
- `/pessoas` deve listar nomes/bios sem e-mails. B não pode editar ou excluir A.
- Banco antigo: faça backup, copie `data` para esta versão e inicie. Confira avaliações e listas antigas nos detalhes. Conta sem senha não deve entrar. Pare o servidor e use a ativação local descrita no README, depois entre e confira os mesmos filmes no perfil antigo.
- Não tente redefinir uma conta ativa com o comando legado: ele deve rejeitar.

## Perfil e foto

- Edite nome, bio, apelido e e-mail em Meu perfil → Editar. Reinicie e confira persistência. Se mudar o e-mail, o próximo login usa o novo; a sessão atual continua associada ao mesmo ID.
- Envie PNG/JPEG de até 2 MB e 2048 × 2048. A foto deve aparecer no perfil e no botão do topo, inclusive após reiniciar.
- Teste remover foto. Deve voltar ao avatar genérico local.
- Tente texto renomeado como JPEG, SVG, imagem maior que 2 MB e dimensões acima do limite. Deve rejeitar, sem substituir a foto anterior.
- Excluir conta exige senha correta e ausência de avaliações/listas. Se houver vínculos, deve orientar a removê-los. Exclusão concluída deve encerrar a sessão e preservar outras pessoas/filmes.

## Regressão do restante

- Crie, edite, busque e exclua um filme sem vínculos; cadastre/edite/exclua um gênero sem uso.
- Notas fora de meia estrela, data futura, e-mail/apelido repetidos e filme sem gêneros devem continuar rejeitados.
- Excluir filme avaliado ou gênero em uso deve continuar bloqueado.
- Crie uma lista, adicione filmes, edite posição/observação e remova itens. Excluir a lista preserva filmes/avaliações.
- Comentários com spoiler devem ficar recolhidos e texto HTML deve ser exibido como texto, sem executar scripts.
- Teste em largura de celular: menu, perfil, botões, formulários e grade de filmes devem se adaptar.
- Pare/inicie o programa na mesma pasta. Contas, hashes, fotos, avaliações e listas devem persistir; as sessões devem exigir novo login após parar o processo.

## Suíte automatizada opcional

`mvn test` executa as suítes em H2 em memória. `AutenticacaoIntegrationTests` testa filtros/sessões reais via MockMvc (sem servidor HTTP externo). `CompatibilidadeLegadoTests` cria o esquema antigo em outro H2 em memória e abre o contexto sem modo web para testar a atualização. Nenhum teste usa o arquivo `data/cinebox.mv.db`.

## Problemas comuns

| Sintoma | Verificação |
|---|---|
| Java incompatível | `mvn -version` precisa mostrar JDK 17/21; Java 8 não serve |
| `mvn` não encontrado | Configure Maven e PATH no seu ambiente |
| Dependência indisponível | Maven precisa do cache ou de acesso aos repositórios públicos quando você executar |
| Porta 8080 ocupada | Use porta 8081 conforme README |
| Banco bloqueado | Pare a outra instância antes de abrir o mesmo arquivo |
| Conta fictícia/antiga não entra | Não há senha padrão; crie conta própria ou use ativação local para a conta antiga |
| Formulário antigo retorna 403 | Recarregue a página para obter token CSRF da sessão atual |
| Protótipo não autentica | É HTML ilustrativo; execute a aplicação Spring para autenticação real |
