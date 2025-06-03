import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame {
    private JButton executarButton;
    private JPanel graficoPanel;
    private final String caminhoArquivo = "base_palavras.txt";
    private final String palavraBuscada = "amor";

    public App() {
        setTitle("Comparativo Serial vs Paralelo");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        executarButton = new JButton("Executar Testes");
        graficoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharGrafico(g);
            }
        };

        add(executarButton, BorderLayout.NORTH);
        add(graficoPanel, BorderLayout.CENTER);

        executarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executarTestes();
                graficoPanel.repaint();
            }
        });
    }

    private static class ResultadoExecucao {
        String metodo;
        int threads;
        long tempo;

        ResultadoExecucao(String metodo, int threads, long tempo) {
            this.metodo = metodo;
            this.threads = threads;
            this.tempo = tempo;
        }
    }

    private final List<ResultadoExecucao> resultados = new ArrayList<>();

    private void executarTestes() {
        resultados.clear();

        // Execução serial (equivale a 1 thread)
        SerialCPU.Resultado resultadoSerial = SerialCPU.contarPalavra(caminhoArquivo, palavraBuscada);
        resultados.add(new ResultadoExecucao("Serial", 1, resultadoSerial.tempoMillis));

        // Execuções paralelas com diferentes números de threads
        for (int threads : new int[]{2, 4, 8}) {
            ParallelCPU.Resultado resultadoParalelo = ParallelCPU.contarPalavraParalelo(caminhoArquivo, palavraBuscada, threads);
            resultados.add(new ResultadoExecucao("ParaleloCPU", threads, resultadoParalelo.tempoMillis));
        }

        // Salva em CSV
        salvarResultadosCSV();
    }

    private void salvarResultadosCSV() {
        try (FileWriter writer = new FileWriter("C:\\Users\\muril\\OneDrive\\Documentos\\GitHub\\busca_paralela_cpu-gpu\\resultados.csv")) {
            writer.write("metodo,threads,tempo_ms\n");
            for (ResultadoExecucao r : resultados) {
                writer.write(r.metodo + "," + r.threads + "," + r.tempo + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void desenharGrafico(Graphics g) {
        if (resultados.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        int width = graficoPanel.getWidth();
        int height = graficoPanel.getHeight();

        // Eixos
        g2.drawLine(50, height - 50, width - 50, height - 50); // X
        g2.drawLine(50, height - 50, 50, 50); // Y
        g2.drawString("Threads", width / 2, height - 20);
        g2.drawString("Tempo (ms)", 10, 30);

        // Normalização
        long maxTempo = resultados.stream().mapToLong(r -> r.tempo).max().orElse(1);

        for (ResultadoExecucao r : resultados) {
            int x = 50 + r.threads * 50;
            int y = (int) (height - 50 - ((double) r.tempo / maxTempo * (height - 100)));
            g2.setColor(r.metodo.equals("Serial") ? Color.RED : Color.BLUE);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.drawString(r.tempo + "ms", x - 10, y - 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
