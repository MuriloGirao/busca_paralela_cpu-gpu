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
    private final String caminhoArquivo = "C:\\Users\\muril\\OneDrive\\Documentos\\GitHub\\busca_paralela_cpu-gpu\\base_palavras.txt";
    private final String palavraBuscada = "gutenberg";

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

        SerialCPU.Resultado resultadoSerial = SerialCPU.contarPalavra(caminhoArquivo, palavraBuscada);
        resultados.add(new ResultadoExecucao("Serial", 1, resultadoSerial.tempoMillis));

        for (int threads : new int[]{2, 4, 6}) {
            ParallelCPU.Resultado resultadoParalelo = ParallelCPU.contarPalavraParalelo(caminhoArquivo, palavraBuscada, threads);
            resultados.add(new ResultadoExecucao("ParaleloCPU", threads, resultadoParalelo.tempoMillis));
        }

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

        g2.drawLine(50, height - 50, width - 50, height - 50); // X
        g2.drawLine(50, height - 50, 50, 50); // Y
        g2.drawString("Threads", width / 2, height - 20);
        g2.drawString("Tempo (ms)", 10, 30);

        long maxTempo = resultados.stream().mapToLong(r -> r.tempo).max().orElse(1);

        List<Point> pontosSerial = new ArrayList<>();
        List<Point> pontosParalelo = new ArrayList<>();

        for (ResultadoExecucao r : resultados) {
            int x = 50 + r.threads * 50;
            int y = (int) (height - 50 - ((double) r.tempo / maxTempo * (height - 100)));
            g2.setColor(r.metodo.equals("Serial") ? Color.RED : Color.BLUE);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.drawString(r.tempo + "ms", x - 10, y - 10);

            if (r.metodo.equals("Serial")) {
                pontosSerial.add(new Point(x, y));
            } else if (r.metodo.equals("ParaleloCPU")) {
                pontosParalelo.add(new Point(x, y));
            }
        }

        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.RED);
        for (int i = 1; i < pontosSerial.size(); i++) {
            g2.drawLine(pontosSerial.get(i - 1).x, pontosSerial.get(i - 1).y, pontosSerial.get(i).x, pontosSerial.get(i).y);
        }

        g2.setColor(Color.BLUE);
        for (int i = 1; i < pontosParalelo.size(); i++) {
            g2.drawLine(pontosParalelo.get(i - 1).x, pontosParalelo.get(i - 1).y, pontosParalelo.get(i).x, pontosParalelo.get(i).y);
        }

        if (!pontosSerial.isEmpty() && !pontosParalelo.isEmpty()) {
            Point pontoSerial = pontosSerial.get(0);
            Point primeiroParalelo = pontosParalelo.get(0);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            g2.drawLine(pontoSerial.x, pontoSerial.y, primeiroParalelo.x, primeiroParalelo.y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
