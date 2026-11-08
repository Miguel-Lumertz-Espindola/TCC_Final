# USO DE TÉCNICAS DE GAMIFICAÇÃO COMO ESTRATÉGIA DE APOIO AO GERENCIAMENTO DE DIETAS

Aplicativo Android para acompanhamento da rotina alimentar, com check-ins, diário alimentar, metas e recursos de gamificação. Projeto de Trabalho de Conclusão de Curso na UNESC.

## Organização

```
android/   Aplicativo Android nativo em Java e XML
backend/   API Spring Boot e mapeamentos JPA para PostgreSQL
```

## Tecnologias

- Android: Java, XML, Retrofit, Navigation Component e Glide.
- API: Java 21, Spring Boot 4.1.1 e JPA/Hibernate.
- Banco: PostgreSQL.

## Funcionalidades

- Cadastro em etapas, com objetivo, atividade física, peso, altura, preferências da rotina e tags alimentares combináveis.
- Seleção inicial entre seis perfis e cardápios demonstrativos previamente cadastrados.
- Diário alimentar, itens alimentares e valores nutricionais opcionais.
- Metas nutricionais com histórico de alterações.
- Check-ins com pontuação por horário e sequência de dias com pontos.
- Conquistas, ranking e seleção de personagem.
- Guerreiro inicial e mago disponível com 30 pontos acumulados para demonstração, sem gastar pontos na troca.

Os cardápios, valores de referência dos perfis e critérios de comparação do catálogo inicial são demonstrativos. Eles não constituem prescrição nutricional individualizada. Os detalhes estão em `backend/docs`.

## Executar o backend

1. Instale/configure um JDK 21 e o PostgreSQL.
2. Crie o banco `Database_TCC`.
3. Abra `backend` no IntelliJ como projeto Maven.
4. Configure as variáveis de ambiente da execução:

| Variável | Valor |
| --- | --- |
| `DB_USER` | Seu usuário do PostgreSQL |
| `DB_PASSWORD` | Sua senha do PostgreSQL |
| `DB_URL` | Opcional; padrão `jdbc:postgresql://localhost:5432/Database_TCC` |

No IntelliJ, essas variáveis podem ser definidas na configuração de execução da aplicação. Não grave credenciais reais nos arquivos versionados.

Execute `DatabaseTccApplication`. A API usa a porta 8080 e o Hibernate está configurado com `ddl-auto=update`. O banco de dados, seus usuários e registros não são distribuídos neste repositório; os dados existentes no ambiente de desenvolvimento não são publicados junto ao código.

Os horários dos check-ins e a pontuação de desbloqueio do mago são configuráveis em `backend/src/main/resources/application.properties`. O fuso padrão dos check-ins é `America/Sao_Paulo`.

## Executar o Android

1. Abra a pasta `android` no Android Studio.
2. Deixe o Android Studio configurar o SDK local e sincronizar o Gradle.
3. Inicie a API e conecte um emulador ou celular com depuração USB autorizada.
4. Execute o aplicativo.

No Windows, o projeto configura `adb reverse` ao compilar a versão de debug para encaminhar a porta 8080 do dispositivo ao computador. O script `android/conectar-api.ps1` também pode ser executado manualmente. A URL de desenvolvimento do aplicativo é `http://127.0.0.1:8080/`.

## Testes

Os testes unitários do backend ficam em `backend/src/test`. A carga completa inclui um teste de inicialização do Spring que requer as variáveis do banco configuradas e PostgreSQL disponível.

Os testes Android ficam em `android/app/src/test` e `android/app/src/androidTest`. Testes de instrumentação precisam de emulador/dispositivo; alguns cenários de integração são condicionados a servidores isolados de teste.

## Publicação e continuidade do desenvolvimento

Esta pasta foi preparada como cópia do código em 8 de novembro de 2026. Para que mudanças futuras apareçam neste repositório, abra e edite os projetos `android` e `backend` desta pasta e faça novos commits.

As pastas de build, caches, configurações locais de SDK, chaves privadas e credenciais locais são excluídas pelo `.gitignore`. Os wrappers do Gradle e Maven e os recursos visuais usados no app fazem parte do código publicado.
