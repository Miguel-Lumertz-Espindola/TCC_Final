# Seleção inicial de dieta por perfil

## Escopo

O cadastro escolhe entre opções completas previamente registradas. O seletor não gera receitas, troca ingredientes, calcula calorias ou preenche metas nutricionais. A versão inicial contém seis perfis e dois cardápios demonstrativos por perfil, totalizando doze opções completas. Novos perfis e variantes podem ser acrescentados ao JSON sem aumentar os casos do algoritmo.

Catálogo: `src/main/resources/nutricao/perfis-dietas-v1.json`.

Os cardápios, os valores físicos dos perfis e os pesos do comparador são dados experimentais do protótipo. Eles não foram extraídos como prescrições da literatura nem validados clinicamente. Os nomes dos objetivos organizam o catálogo; eles não demonstram que o cardápio produz emagrecimento, hipertrofia ou manutenção. A interface identifica as opções como exemplos. Para uso nutricional individual, é necessário cadastrar planos e critérios revisados por profissional habilitado.

## Dados utilizados

- Objetivo: filtro obrigatório. Um objetivo diferente nunca é utilizado como alternativa.
- Tags alimentares: todas devem ser atendidas pelo mesmo cardápio completo.
- Atividade física, praticidade, rigor da rotina e orçamento: critérios de comparação entre candidatos elegíveis.
- Peso e altura: comparados separadamente aos valores de referência do perfil.
- Nascimento/idade e sexo biológico: não participam da seleção nesta etapa. A validação de uma data de nascimento válida continua existindo no cadastro.
- Identificação e credenciais: não participam da seleção.

Não há diagnóstico pelo IMC ou estimativa de necessidade energética.

## Seis perfis iniciais

| Objetivo | Perfil | Atividade de referência | Preparo | Rotina | Orçamento | Peso de referência | Altura de referência |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Manutenção | Prático e econômico | Sedentário / leve | Rápido | Flexível | Econômico | 80 kg | 175 cm |
| Manutenção | Variado | Moderado / intenso | Elaborado | Estruturada | Flexível | 60 kg | 160 cm |
| Emagrecimento | Prático e econômico | Sedentário / leve | Rápido | Flexível | Econômico | 80 kg | 175 cm |
| Emagrecimento | Variado | Moderado / intenso | Elaborado | Estruturada | Flexível | 60 kg | 160 cm |
| Hipertrofia | Prático e econômico | Sedentário / leve | Rápido | Flexível | Econômico | 80 kg | 175 cm |
| Hipertrofia | Variado | Moderado / intenso | Elaborado | Estruturada | Flexível | 60 kg | 160 cm |

Esses valores físicos são pontos de referência simulados para demonstrar a comparação, não limites de indicação de dietas. Cada resposta influencia o cálculo, mas alterar uma resposta não necessariamente altera o vencedor: isso depende dos demais critérios e do tamanho do catálogo.

## Comparação determinística

Depois dos filtros de objetivo e tags:

```
pontos = atividade + praticidade + orçamento + rotina + peso + altura

atividade:   20 se compatível; 0 se diferente
praticidade: 20 se compatível; 0 se diferente
orçamento:   20 se compatível; 0 se diferente
rotina:      10 se compatível; 0 se diferente
peso:       15 / (1 + abs(pesoInformado - pesoReferência) / 20)
altura:     15 / (1 + abs(alturaInformada - alturaReferência) / 15)
```

Peso em kg; altura em cm. Os divisores 20 e 15 definem a escala matemática da comparação e são decisões experimentais de engenharia. A pontuação é um índice de semelhança entre registros do catálogo, não uma porcentagem de adequação nutricional ou de precisão do check-in. A ordenação utiliza os valores decimais; só o valor armazenado para apresentação é arredondado. Empates são resolvidos pelos códigos estáveis do perfil e da variante.

## Tags e variantes

Tags iniciais: `SEM_LACTOSE`, `SEM_GLUTEN`, `VEGETARIANO`, `VEGANO`. Seleção múltipla e seleção vazia são válidas. Combinações redundantes, como vegetariano + vegano, são aceitas. Valores desconhecidos são rejeitados.

Cada perfil contém um cardápio tradicional e outro vegetal sem lactose e sem glúten. O segundo cobre as combinações iniciais de tags. Com apenas duas variantes, uma única tag pode resultar na opção vegetal com outras características adicionais; a interface mostra qual opção foi escolhida. Não são feitas substituições dinâmicas de alimentos.

As compatibilidades são declarações do catálogo. A inicialização rejeita contradições entre as tags e os componentes cadastrados, mas essa verificação não examina rótulos, preparo ou contaminação cruzada. "Sem lactose" não equivale a adequação para alergia ao leite. O catálogo não afirma atender alergias que não estão representadas nas tags.

Sem opção elegível, o cadastro é concluído e a sugestão recebe `SEM_COMPATIVEL`. Nenhuma tag é descartada para produzir uma resposta.

## Persistência e API

- `usuarios`: recebe praticidade, rigor e orçamento.
- `usuario_tags_alimentares`: conjunto de tags por usuário, com unicidade por usuário/tag.
- `dietas_usuario`: snapshot da escolha, variante, versão do catálogo, critérios e refeições.

Usuário, mascote e snapshot são criados na mesma transação. A consulta retorna o snapshot, sem recalcular a sugestão quando o catálogo é ampliado.

- `POST /api/usuarios/cadastrar`: preserva o retorno textual anterior e aceita os novos campos de perfil e tags.
- `GET /api/usuarios/dieta?email=...`: consulta a sugestão da conta.

Contas anteriores recebem `CADASTRO_ANTERIOR`, sem atribuição automática baseada em respostas ausentes. Clientes antigos que ainda enviam observações alimentares em texto recebem `OBSERVACOES_PENDENTES`: o texto é preservado e não é ignorado para sugerir uma dieta potencialmente incompatível.

A estratégia de identificação por e-mail acompanha o restante da API existente; autenticação e autorização não foram reformuladas nesta implementação.

## Fundamentação e limites das referências

O TCC fornecido, páginas 9 e 15-16, descreve seleção de planos previamente cadastrados usando o perfil, com acompanhamento por check-ins e diário opcional. A implementação mantém essa distinção: confirmar uma refeição não registra automaticamente alimentos no diário.

[Amiri, Li e Hasan (2023)](https://formative.jmir.org/2023/1/e46434), Tabela 1, apresenta dois dias exemplificativos: um para um caso de hipertensão e outro para um caso de diabetes. Em tradução resumida, os pratos são:

| Caso do artigo | Café da manhã | Almoço | Jantar |
| --- | --- | --- | --- |
| Hipertensão | Omelete com vegetais | Espaguete com feijão-preto | Macarrão com queijo ao estilo mexicano |
| Diabetes | Cuscuz no café da manhã | Frango Dum Ka | Sopa de vegetais de inverno com curry |

São exemplos de outros casos e não seis dietas para os objetivos deste app. Os cardápios demonstrativos do JSON são exemplos próprios; não foram apresentados como transcrição, adaptação validada ou prescrição proveniente dessa tabela. O artigo fundamenta considerar restrições obrigatórias antes de comparar preferências; sua otimização nutricional não foi reproduzida.

[Orte et al. (2023)](https://pmc.ncbi.nlm.nih.gov/articles/PMC10142076/), seções 2.2 e 2.4.1, descreve recomendações classificadas por compatibilidade com preferências, incluindo vegano e sem glúten. Isso apoia o uso de tags estruturadas, mas não valida os seis cardápios deste protótipo.

A referência FitEat foi localizada na bibliografia do TCC, mas o arquivo no endereço informado não respondeu à consulta. Nenhum cardápio foi atribuído a esse trabalho sem leitura do documento.

## Validação realizada

- 60 testes de backend passaram, incluindo 19 novos casos de seleção e cadastro.
- Cobertura das 15 combinações não vazias das quatro tags.
- Mudança isolada de peso e mudança isolada de altura podem mudar o perfil; alteração de nascimento não muda a classificação.
- Cadastro e consulta reais dos seis perfis testados via HTTP com PostgreSQL; confirmação de tags, mascote e snapshot; rejeição de tag desconhecida e de cadastro duplicado.
- Contas temporárias de integração removidas ao final.
- Android e APK de instrumentação compilados. A execução do novo teste de interface depende de um emulador/dispositivo acessível; o emulador não iniciou no ambiente de execução por restrição de aceleração/virtualização.

Para testar manualmente: reinicie o backend na porta 8080, execute o Android e realize um novo cadastro. Selecione os campos do perfil e várias tags; após concluir, consulte o motivo da sugestão e as três refeições. O botão "Minha dieta" na Home permite voltar à sugestão depois.
