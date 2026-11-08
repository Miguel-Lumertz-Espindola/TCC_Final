# Publicar no GitHub

## Pela pasta local preparada

1. Abra o GitHub Desktop e entre na sua conta GitHub.
2. Use **File > Add local repository** e selecione esta pasta `TCC_GitHub`.
3. Confira os arquivos na aba **Changes**.
4. Em **Summary**, escreva `Versão inicial do TCC` e clique em **Commit to main**.
5. Clique em **Publish repository**.
6. Escolha o nome do repositório, por exemplo `tcc-nutricao-gamificado`.
7. Mantenha **Keep this code private** marcado para um repositório privado, ou desmarque para publicar um repositório público.
8. Confirme em **Publish Repository**.

Esta cópia local já foi inicializada como repositório Git; a publicação e o primeiro commit ficam a cargo da sua conta. Nenhuma conta GitHub ou repositório remoto foi usado na preparação.

Se estiver usando somente o ZIP e não a pasta local preparada, extraia o ZIP. No GitHub Desktop, ao adicionar a pasta sem metadados Git, crie um repositório local nessa pasta antes de fazer o primeiro commit. O ZIP não deve ser enviado como um único arquivo ao GitHub: publique as pastas e os arquivos para que o código fique navegável.

## Atualizações futuras

Abra os projetos que estão dentro desta pasta para continuar o desenvolvimento. Depois das alterações, use **Commit to main** e **Push origin** no GitHub Desktop.

## Configuração do banco

O arquivo `backend/src/main/resources/application.properties` usa `DB_USER`, `DB_PASSWORD` e `DB_URL`. As credenciais da cópia original não estão presentes nesta cópia para publicação. Configure essas variáveis no IntelliJ ou no ambiente antes de executar o backend desta pasta.

## Referências

- https://docs.github.com/en/desktop/adding-and-cloning-repositories/adding-a-repository-from-your-local-computer-to-github-desktop
- https://docs.github.com/en/desktop/adding-and-cloning-repositories/adding-an-existing-project-to-github-using-github-desktop
