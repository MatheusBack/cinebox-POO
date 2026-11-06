# Autenticação, autoria e compatibilidade

## Identidade e credenciais

`AutenticacaoService` implementa `UserDetailsService` e consulta o e-mail normalizado. `DaoAuthenticationProvider` do Spring Security usa o `PasswordEncoder` BCrypt configurado (custo 12). O login não compara texto com texto nem autentica por um ID enviado pelo navegador.

`UsuarioAutenticado` carrega o ID imutável do banco como principal da sessão, além do e-mail e das autoridades. Não guarda uma entidade JPA na sessão. O hash é apagado do principal por `CredentialsContainer` após autenticar; o hash persistido no banco permanece.

`SessaoUsuarioService.exigirId()` lê o principal autenticado. Avaliações e listas usam esse ID no Service. `AvaliacaoForm` e `ListaForm` não possuem `usuarioId`. Parâmetros extras enviados manualmente não alteram a identidade nem a autoria.

Não há senha padrão. Cadastro exige confirmação e mínimo de 8 caracteres. O limite de 72 bytes UTF-8 é checado no Service para respeitar a entrada máxima do BCrypt sem truncamento silencioso. Senhas não são devolvidas no HTML após validação, nem registradas em logs pelo código.

## Autorização

- Toda rota de negócio exige autenticação pelo filtro; cadastro, login e assets são públicos.
- A consulta de edição/exclusão de avaliação usa simultaneamente `id` e `usuario_id` da sessão. Um registro alheio retorna 404, sem escrita.
- O perfil e o diário consultam avaliações por `usuario_id`. Não aceitam um ID de pessoa como parâmetro.
- Listas são lidas por todos os usuários autenticados; edição, exclusão e itens verificam o dono no Service.
- A seção Pessoas expõe DTOs públicos sem e-mail/hash/foto binária. Gestão de conta é apenas do titular em `/perfil`.
- A foto só pode ser lida por `/perfil/foto` para a própria sessão. Não há caminho de arquivo ou ID de terceiro como entrada.
- CSRF continua ativo. Todos os formulários de escrita usam `th:action`, permitindo ao Spring/Thymeleaf inserir o token.
- Logout é exclusivamente POST. A sessão é invalidada e o cookie JSESSIONID é removido. Não existe remember-me.

## Dados da primeira versão

Não foi alterada a chave de `usuarios` nem a associação `avaliacoes.usuario_id`/`listas.usuario_id`. Foram adicionados:

| Coluna | Tipo | Uso |
|---|---|---|
| `usuarios.senha_hash` | VARCHAR(100), nullable | BCrypt; null significa conta antiga ainda bloqueada para login |
| `usuarios.foto` | BLOB, nullable | PNG/JPEG processado |
| `usuarios.foto_tipo` | VARCHAR(20), nullable | MIME determinado pelo servidor |

Com `ddl-auto=update`, Hibernate adiciona essas colunas no primeiro início. Faça backup antes de abrir um arquivo existente com uma versão nova. A compatibilidade foi prevista no código e coberta por teste de migração em memória, mas não executada nesta preparação.

Contas sem hash não podem autenticar, inclusive as fictícias dos exemplos. A operação de ativação legada é um comando interativo local: `java -jar target/cinebox-1.0.0.jar --ativar-conta-legada`. Ela abre o contexto Spring sem servidor HTTP e permite definir apenas a primeira senha de uma conta sem hash. Não redefine senha de contas ativas. O responsável pelo arquivo local do banco é quem deve executar essa operação; não existe link público de reivindicação de perfil.

Os exemplos são importados por um Service separado e condicionado a `app.dados-exemplo=true`. Os Controllers nunca o usam. Isso preserva os exemplos sem abrir uma API que aceite autoria arbitrária.

## Foto

Upload facultativo em `/perfil/editar`. O Service inspeciona o formato real com ImageIO, aceita apenas PNG/JPEG de até 2 MB e 2048 × 2048, regrava a imagem sem depender do nome/extensão/MIME do cliente e armazena os bytes no H2. SVG enviado pelo cliente não é aceito. Imagem genérica vem de um SVG estático do projeto. Respostas da foto usam `Cache-Control: no-store`.

## Limites

Não há envio de e-mail, recuperação online de senha, OAuth, integração com identidade corporativa ou infraestrutura externa. Não foram adicionados papéis de administrador: o catálogo compartilhado permanece gerenciável por qualquer conta autenticada. Sessões são locais ao processo, com 30 minutos de inatividade. As regras de bloqueio de exclusão por vínculos permanecem.
