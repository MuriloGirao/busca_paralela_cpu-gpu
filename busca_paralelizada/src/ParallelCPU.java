import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ParallelCPU {

    public static class Resultado {
        public int ocorrencias;
        public long tempoMillis;

        public Resultado(int ocorrencias, long tempoMillis) {
            this.ocorrencias = ocorrencias;
            this.tempoMillis = tempoMillis;
        }
    }

    public static class ContagemTask extends RecursiveTask<Integer> {
        private static final int LIMIAR = 10000;
        private final String[] palavras;
        private final int inicio, fim;
        private final String palavraBuscada;

        public ContagemTask(String[] palavras, int inicio, int fim, String palavraBuscada) {
            this.palavras = palavras;
            this.inicio = inicio;
            this.fim = fim;
            this.palavraBuscada = palavraBuscada.toLowerCase();
        }

        @Override
        protected Integer compute() {
            if (fim - inicio <= LIMIAR) {
                int contador = 0;
                for (int i = inicio; i < fim; i++) {
                    if (palavras[i].equalsIgnoreCase(palavraBuscada)) {
                        contador++;
                    }
                }
                return contador;
            } else {
                int meio = (inicio + fim) / 2;
                ContagemTask esquerda = new ContagemTask(palavras, inicio, meio, palavraBuscada);
                ContagemTask direita = new ContagemTask(palavras, meio, fim, palavraBuscada);
                esquerda.fork(); 
                direita.fork();
                int direitaResultado = direita.join(); 
                int esquerdaResultado = esquerda.join();
                return esquerdaResultado + direitaResultado;
            }
        }
    }

    public static Resultado contarPalavraParalelo(String caminhoArquivo, String palavraBuscada, int threads) {
        long inicio = System.currentTimeMillis();
        StringBuilder textoCompleto = new StringBuilder();

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                textoCompleto.append(linha).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String texto = textoCompleto.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] palavras = texto.split("\\s+");

        ForkJoinPool pool = new ForkJoinPool(threads); 
        ContagemTask tarefaPrincipal = new ContagemTask(palavras, 0, palavras.length, palavraBuscada);
        int total = pool.invoke(tarefaPrincipal);

        long fim = System.currentTimeMillis();
        return new Resultado(total, fim - inicio);
    }

    public static void main(String[] args) {
        String arquivo = "C:\\Users\\muril\\OneDrive\\Documentos\\GitHub\\busca_paralela_cpu-gpu\\base_palavras.txt";
        String palavra = "gutenberg";

        Resultado resultado = contarPalavraParalelo(arquivo, palavra,6);
        System.out.println("ParallelCPU: " + resultado.ocorrencias + " ocorrências em " + resultado.tempoMillis + " ms");
    }
}
