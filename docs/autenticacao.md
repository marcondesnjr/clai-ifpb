# SF-04 — Autenticação e controle de acesso

| Perfil | Permissões previstas |
| --- | --- |
| EDICAO | Consultar, cadastrar, editar e excluir registros nas operações disponíveis. |
| CONSULTA | Listar registros, pesquisar e visualizar detalhes. |

O MongoDB armazena as contas na coleção `usuarios`.
O serviço deverá guardar em `senhaHash` apenas o hash BCrypt e usar
`PasswordEncoder.matches` para validar a senha no login.
O índice único de `login` deve ser criado no banco antes do cadastro das contas.

Nesta etapa, a configuração automática de segurança web e do usuário padrão
está temporariamente desativada para preservar o acesso às telas existentes.
Na integração do login, essas exclusões serão removidas e as permissões
serão aplicadas nas rotas.

## US27 — Login e Logout

A autenticação utiliza Spring Security e usuários
armazenados no MongoDB.

### Funcionalidades

- Login por nome de usuário e senha.
- Senhas armazenadas com hash seguro.
- Bloqueio de usuários desativados.
- Redirecionamento para login quando não autenticado.
- Logout com invalidação da sessão HTTP.
- Proteção CSRF nos formulários.

### Rotas

- GET /login: exibe o formulário.
- POST /login: processa a autenticação.
- POST /logout: encerra a sessão.
- /usuarios/**: permanece bloqueada nesta etapa.

### Segurança

A configuração usa @EnableWebSecurity e
SecurityFilterChain.

Os perfis EDICAO e CONSULTA já estão definidos.
As permissões específicas serão implementadas
na US28.

O cadastro e o gerenciamento administrativo
permanecem desativados por padrão.

### Testes

Foram implementados testes de integração para
login, senha incorreta, contas desativadas,
logout, sessão HTTP e proteção CSRF.

Os testes usam bancos MongoDB específicos,
separados dos dados normais da aplicação.

## US28 — Controle de acesso por perfis

O sistema utiliza dois perfis de acesso:
EDICAO e CONSULTA.

### Perfil EDICAO

- Consultar registros.
- Cadastrar alunos e estudos de caso.
- Editar registros.
- Excluir alunos nas operações disponíveis.

### Perfil CONSULTA

- Visualizar registros.
- Pesquisar alunos e estudos de caso.
- Não pode cadastrar, editar ou excluir registros.

### Segurança

As permissões são verificadas no backend
pelo Spring Security.

O Thymeleaf oculta os botões de alteração
quando o usuário não possui autorização.

As rotas /usuarios/** continuam bloqueadas
até a definição de quem poderá administrar
as contas.

### Testes

Foram implementados testes automatizados
para validar as permissões dos dois perfis,
incluindo operações permitidas e negadas.