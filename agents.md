---
name: NearNode
description: app chat descentralizado que enviar mensagens de nó a nó
---

- este é um projeto Nativo Android
- neste projeto é utilizado a arquitetura clean architecture
- Para cada feature crie o mínimo necessário de classes
- Utilize os princípios de SOLID e KISS
- Mantenha a legíbilidade de código o máximo possível, criando código fácil para leitura por humanos
- Sempre que criar uma nova tela, faça da seguinte maneira
  - crie a tela e injete a viewModel
  - deixe o conteúdo em um composable separado, recebendo o status por parâmetro
  - crie os previews para todos os estados
- ao Criar novas telas compose
  - mantenha todos os textos no arquivo strings.xml
  - todas as cores devem estar no arquivo Color.kt
  - sempre que determinado componente aparecer mais de uma vez, coloque-o dentro de presentation/components criando o compnente e o preview
  - mantenha sempre a árvore de componentes o menor possível evitando aninhar componentes desnecessariamente