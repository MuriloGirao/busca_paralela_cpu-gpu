# Comparação de Algoritmos de Busca: CPU vs GPU

## ✅ Resumo

Este projeto visa comparar o desempenho de algoritmos de busca implementados em Java utilizando diferentes meios de processamento: CPU sequencial, CPU paralela e GPU. Os testes foram realizados em variados volumes de dados com armazenamento e análise dos tempos de execução, gerando gráficos que ilustram a eficiência de cada abordagem.

---

## 🔍 Introdução

O projeto tem como objetivo explorar o comportamento de algoritmos de busca sob diferentes arquiteturas de processamento. Para isso, foram desenvolvidos algoritmos em Java com suporte a execução sequencial e paralela (multi-threading e GPU via OpenCL). O foco principal é observar como o desempenho varia de acordo com o meio de execução, visando identificar padrões e vantagens em cada abordagem.

---

## 🧪 Metodologia

A metodologia aplicada para o desenvolvimento e análise dos testes envolveu as seguintes etapas:

- **Implementação de Algoritmos**: Foram desenvolvidas versões dos algoritmos de busca para execução sequencial e paralelizada, incluindo uma versão para execução em GPU utilizando OpenCL.
- **Framework de Testes**: Um framework de testes foi criado para padronizar as execuções, medições de tempo e coleta de dados.
- **Ambientes de Execução**: Os testes foram executados em diferentes ambientes para captar variações de desempenho.
- **Registro de Dados**: Os tempos de execução foram armazenados em arquivos `.csv` para posterior análise.
- **Análise Estatística**: Com base nos dados coletados, foram construídos gráficos e avaliados padrões estatísticos que demonstram os pontos fortes e fracos de cada abordagem.

---

## 📊 Resultados e Discussão

Os testes mostraram variações significativas de desempenho entre as versões dos algoritmos:

- **CPU Sequencial**: Apresenta desempenho consistente em pequenos volumes, mas não escala bem.
- **CPU Paralela**: Oferece ganhos consideráveis com aumento de threads, especialmente em datasets médios a grandes.
- **GPU**: Demonstrou desempenho superior em grandes volumes, com baixa latência após inicialização.

Gráficos demonstrativos foram gerados para ilustrar a performance dos algoritmos, evidenciando a vantagem do uso da GPU para datasets maiores, enquanto a CPU paralela mostrou-se mais eficiente em cenários intermediários. A linha pontilhada de tendência nos gráficos evidencia esse comportamento. Também foi traçada uma linha ligando os pontos de desempenho GPU ao ponto mais à direita da série CPU, destacando a vantagem da GPU nos cenários de maior carga.

---

## ✅ Conclusão

A análise comparativa entre os algoritmos de busca mostrou que:

- A **CPU sequencial** é adequada para pequenas tarefas.
- A **CPU paralela** representa um bom equilíbrio entre complexidade e performance.
- A **GPU** é mais vantajosa para grandes volumes, embora demande maior complexidade na configuração e inicialização.

Essas descobertas fornecem diretrizes para a escolha adequada do meio de execução conforme o problema enfrentado.

---

## 📚 Referências

- OpenCL API Documentation: https://registry.khronos.org/OpenCL/
- Java Parallel Streams: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html
- Artigos de benchmark de algoritmos paralelos e CUDA/OpenCL.

---

## 📎 Anexos – Códigos das Implementações

### Implementações

- `BuscaSequencial.java`: Implementação de busca linear simples.
- `BuscaParalela.java`: Implementação paralela com múltiplas threads.
- `BuscaGPU.java`: Implementação com suporte à execução via GPU (OpenCL).

### Framework de Teste

- `Main.java`: Controla a execução dos testes e coleta os tempos.
- `GeradorDados.java`: Gera os arrays de dados aleatórios.
- `EscritorCSV.java`: Escreve os tempos de execução no formato `.csv`.

### Dados e Resultados

- `dados.csv`: Arquivo com os dados de entrada para os testes.
- `tempos_execucao.csv`: Registra os tempos de execução para cada algoritmo.
- `grafico_desempenho.png`: Gráfico comparativo dos resultados.

---

O projeto completo com os códigos de busca sequencial, paralela e GPU, bem como o framework de teste e arquivos de entrada/saída está disponível no repositório abaixo:

🔗 **Link do Projeto no GitHub**: [(https://github.com/MuriloGirao/busca_paralela_cpu-gpu.git)](https://github.com/MuriloGirao/busca_paralela_cpu-gpu.git)



