import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SerialCPU {
    public static class Resultado {
        public int ocorrencias;
        public long tempoMillis;

        public Resultado(int ocorrencias, long tempoMillis) {
            this.ocorrencias = ocorrencias;
            this.tempoMillis = tempoMillis;
        }
    }

    public static Resultado contarPalavra(String caminhoArquivo, String palavraBuscada) {
        int contador = 0;
        long inicio = System.currentTimeMillis();

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;

            while ((linha = leitor.readLine()) != null) {
                linha = linha.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");

                String[] palavras = linha.split("\\s+");

                for (String palavra : palavras) {
                    if (palavra.equals(palavraBuscada.toLowerCase())) {
                        contador++;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        long fim = System.currentTimeMillis();
        return new Resultado(contador, fim - inicio);
    }

    public static void main(String[] args) {
        String arquivo = "C:\\Users\\muril\\OneDrive\\Documentos\\GitHub\\busca_paralela_cpu-gpu\\base_palavras.txt";
        String palavra = "gutenberg";

        Resultado resultado = contarPalavra(arquivo, palavra);
        System.out.println("SerialCPU: " + resultado.ocorrencias + " ocorrências em " + resultado.tempoMillis + " ms");
    }
}
