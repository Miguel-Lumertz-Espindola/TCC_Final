# Seleção de personagens

O guerreiro permanece como personagem inicial. O mago fica disponível ao atingir **30 pontos acumulados**, valor escolhido para a demonstração do TCC. Selecionar um personagem não consome pontos, não registra refeições e não altera o último check-in.

No Android, o botão no canto superior direito da moldura usa um quadro do personagem atual. Ao tocar, um menu ancorado oferece os ícones do Guerreiro e do Mago, indica a opção em uso e informa a meta e os pontos restantes quando há bloqueio. A animação principal só muda após a confirmação da API. A seleção fica salva por conta.

## Recursos visuais

- Guerreiro: GIF existente de 13 quadros; o ícone foi obtido por decodificação do primeiro quadro, preservando seus pixels e transparência.
- Mago: GIF original `mago_idle_64px.gif`, com 12 quadros de 100 ms, e `mago_frames/frame_000.png` como ícone, ambos fornecidos no projeto de recursos do usuário. Os arquivos foram copiados sem editar a arte.
- Os GIFs usam o Glide com o ciclo de vida do Fragment. Atualizar a pontuação não reinicia uma animação que já está exibindo o mesmo personagem.

## Backend

- `gamificacao.personagens.pontos-mago=30` em `application.properties` define a meta. Reinicie a API depois de alterar esse valor.
- O campo `status_gamificacao.personagem_codigo` guarda a escolha. Valores nulos de contas anteriores usam o Guerreiro como padrão.
- `GET /api/gamificacao/status` e as respostas detalhadas de check-in incluem `personagemSelecionado` e a lista `personagens`, com código, nome, meta, disponibilidade e indicação de uso.
- `POST /api/gamificacao/personagem?email=...`, com corpo `{"codigo":"MAGO"}` ou `{"codigo":"GUERREIRO"}`, valida a pontuação no servidor e retorna o status atualizado. Código inválido retorna HTTP 400; personagem bloqueado, HTTP 403.
- A troca utiliza o mesmo bloqueio de usuário do check-in, preservando a consistência entre a pontuação e a seleção.
- Contas com pontuação suficiente já têm acesso ao Mago; não precisam de um novo cadastro. O desbloqueio é calculado pela pontuação acumulada atual e pela meta vigente do catálogo.

## Validação

78 testes de backend passaram. Os novos casos cobrem a fronteira de 29/30 pontos, o desbloqueio por check-in, a preservação da escolha em consultas e novos check-ins, a volta ao Guerreiro sem custo e a rejeição de personagem inválido.

A integração HTTP com PostgreSQL confirmou a coluna nova, a recusa de seleção abaixo da meta e de tentativa de forjar pontos no corpo da requisição, a persistência da escolha e a ausência de novos registros de refeição durante a troca. A conta temporária usada na integração foi removida.

O APK do aplicativo e o de instrumentação compilaram. A conferência visual no emulador ficou pendente: não havia dispositivo ADB conectado e a autorização de acesso ao Android Studio expirou durante a tentativa de abertura.

Para demonstrar: inicie o backend atualizado e execute o app pelo Android Studio. Em uma conta com pelo menos 30 pontos, toque no ícone no canto da moldura e escolha o Mago. Saia e volte à aba para conferir a persistência. Em uma conta abaixo de 30 pontos, o menu mostra a meta e quantos pontos faltam.
