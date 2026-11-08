# Horários e pontuação dos check-ins

O horário considerado é o da API no fuso `America/Sao_Paulo`. O relógio do celular e o fuso padrão da JVM não definem a pontuação. Consulta diária, liberação das refeições e gravação do histórico usam o mesmo fuso.

| Refeição | 30 pontos | 15 pontos |
| --- | --- | --- |
| Café da manhã | 07:00 até antes de 08:00 | 08:00 até antes de 08:30 |
| Almoço | 12:00 até antes de 13:00 | 13:00 até antes de 13:30 |
| Jantar | 19:00 até antes de 20:00 | 20:00 até antes de 20:30 |

Fora da faixa correspondente, o registro vale 0 pontos. O tipo `Outros` também vale 0, pois não tem faixa definida. Um check-in sem pontos continua registrado como refeição realizada naquele dia, mas não mantém a sequência de dias. A regra de uma confirmação por refeição por dia continua em vigor.

As faixas ficam em `application.properties`, nas propriedades `gamificacao.checkin`. A inicialização valida o fuso e a ordem início < fim exato < fim parcial. Depois de alterar uma configuração, reinicie a API.

`GET /api/gamificacao/horarios` retorna o fuso, o horário consultado e as faixas usadas no cálculo. O botão de informação da Home consome essa rota, sem manter uma segunda cópia dos horários no Android. Se a consulta falhar, a janela informa a falha em vez de exibir horários presumidos.

A confirmação do check-in informa o horário considerado com o deslocamento UTC e os pontos ganhos. Os endpoints existentes de check-in mantêm o mesmo formato de resposta.

O Relógio Suíço passa a considerar os check-ins exatos das três refeições. O Mestre do Almoço continua contando somente almoços exatos. Registros antigos com precisão `Registrado` não passam a ser exatos retroativamente. Pontos e registros já acumulados não são recalculados nesta correção.

Validação: 69 testes de backend passaram, incluindo limites com precisão de nanossegundos, horários em UTC, virada do dia em São Paulo, repetição e sequência de dias. Uma instância de teste iniciada com `-Duser.timezone=UTC` foi verificada via HTTP com PostgreSQL: consulta dos horários, pontuação real fora das faixas, horário persistido e repetição. As contas temporárias foram removidas. O Android compilou com a nova consulta de horários.
