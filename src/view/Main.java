package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import model.Cidade;
import model.ListaCidades;
import model.Previsao;
import model.PrevisaoCidade;
import model.parse.XStreamParser;
import model.service.WeatherForecastService;

public class Main {

    private static final Map<String, String> DESCRICOES_TEMPO = new HashMap<>();
    static {
        DESCRICOES_TEMPO.put("ec", "Encoberto com Chuvas Isoladas");
        DESCRICOES_TEMPO.put("ci", "Chuvas Isoladas");
        DESCRICOES_TEMPO.put("c", "Chuva");
        DESCRICOES_TEMPO.put("in", "Instável");
        DESCRICOES_TEMPO.put("pp", "Poss. de Pancadas de Chuva");
        DESCRICOES_TEMPO.put("cm", "Chuva pela Manhã");
        DESCRICOES_TEMPO.put("cn", "Chuva a Noite");
        DESCRICOES_TEMPO.put("pt", "Pancadas de Chuva a Tarde");
        DESCRICOES_TEMPO.put("pm", "Pancadas de Chuva pela Manhã");
        DESCRICOES_TEMPO.put("np", "Nublado e Pancadas de Chuva");
        DESCRICOES_TEMPO.put("pc", "Pancadas de Chuva");
        DESCRICOES_TEMPO.put("pn", "Parcialmente Nublado");
        DESCRICOES_TEMPO.put("cv", "Chuvisco");
        DESCRICOES_TEMPO.put("ch", "Chuvoso");
        DESCRICOES_TEMPO.put("t", "Tempestade");
        DESCRICOES_TEMPO.put("ps", "Predomínio de Sol");
        DESCRICOES_TEMPO.put("e", "Encoberto");
        DESCRICOES_TEMPO.put("n", "Nublado");
        DESCRICOES_TEMPO.put("cl", "Céu Claro");
        DESCRICOES_TEMPO.put("nv", "Nevoeiro");
        DESCRICOES_TEMPO.put("g", "Geada");
        DESCRICOES_TEMPO.put("ne", "Neve");
        DESCRICOES_TEMPO.put("nd", "Não Definido");
        DESCRICOES_TEMPO.put("pnt", "Pancadas de Chuva a Noite");
        DESCRICOES_TEMPO.put("psc", "Possibilidade de Chuva");
        DESCRICOES_TEMPO.put("pcm", "Possibilidade de Chuva pela Manhã");
        DESCRICOES_TEMPO.put("pct", "Possibilidade de Chuva a Tarde");
        DESCRICOES_TEMPO.put("pcn", "Possibilidade de Chuva a Noite");
        DESCRICOES_TEMPO.put("npt", "Nublado com Pancadas a Tarde");
        DESCRICOES_TEMPO.put("npn", "Nublado com Pancadas a Noite");
        DESCRICOES_TEMPO.put("ncn", "Nublado com Poss. de Chuva a Noite");
        DESCRICOES_TEMPO.put("nct", "Nublado com Poss. de Chuva a Tarde");
        DESCRICOES_TEMPO.put("ncm", "Nublado com Poss. de Chuva pela Manhã");
        DESCRICOES_TEMPO.put("npm", "Nublado com Pancadas pela Manhã");
        DESCRICOES_TEMPO.put("npp", "Nublado com Possibilidade de Chuva");
        DESCRICOES_TEMPO.put("vn", "Variação de Nebulosidade");
        DESCRICOES_TEMPO.put("ct", "Chuva a Tarde");
        DESCRICOES_TEMPO.put("ppn", "Poss. de Panc. de Chuva a Noite");
        DESCRICOES_TEMPO.put("ppt", "Poss. de Panc. de Chuva a Tarde");
        DESCRICOES_TEMPO.put("ppm", "Poss. de Panc. de Chuva pela Manhã");
    }

    private JFrame frame;
    private JTextField cidadeTextField;
    private JButton buscarButton;
    private JList<Cidade> listaCidades;
    private DefaultListModel<Cidade> listModel;
    private JPanel previsaoPanel;
    private JLabel statusLabel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Previsão do Tempo - INPE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));

        initComponents();
        layoutComponents();
        addListeners();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initComponents() {
        cidadeTextField = new JTextField(25);
        buscarButton = new JButton("Buscar");
        listModel = new DefaultListModel<>();
        listaCidades = new JList<>(listModel);
        listaCidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCidades.setCellRenderer(new CidadeListCellRenderer());
        previsaoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        statusLabel = new JLabel("Digite o nome de uma cidade e clique em buscar.");
    }

    private void layoutComponents() {
        JPanel buscaPanel = new JPanel(new BorderLayout(10, 10));
        buscaPanel.setBorder(BorderFactory.createTitledBorder("Buscar Cidade"));
        buscaPanel.add(cidadeTextField, BorderLayout.CENTER);
        buscaPanel.add(buscarButton, BorderLayout.EAST);

        JPanel resultadosPanel = new JPanel(new BorderLayout());
        resultadosPanel.setBorder(BorderFactory.createTitledBorder("Resultados da Busca"));
        resultadosPanel.add(new JScrollPane(listaCidades), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(buscaPanel, BorderLayout.NORTH);
        topPanel.add(resultadosPanel, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel previsaoWrapperPanel = new JPanel(new BorderLayout());
        previsaoWrapperPanel.setBorder(BorderFactory.createTitledBorder("Previsão para os Próximos Dias"));
        previsaoWrapperPanel.add(new JScrollPane(previsaoPanel), BorderLayout.CENTER);
        previsaoWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);

        frame.setLayout(new BorderLayout(10, 10));
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(previsaoWrapperPanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        buscarButton.addActionListener(e -> buscarCidades());
        cidadeTextField.addActionListener(e -> buscarButton.doClick());

        listaCidades.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaCidades.getSelectedValue() != null) {
                buscarPrevisao(listaCidades.getSelectedValue());
            }
        });
    }

    private void buscarCidades() {
        String nomeCidade = cidadeTextField.getText().trim();
        if (nomeCidade.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Por favor, digite o nome de uma cidade.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setText("Buscando cidades...");
        buscarButton.setEnabled(false);
        listModel.clear();
        previsaoPanel.removeAll();
        previsaoPanel.revalidate();
        previsaoPanel.repaint();

        SwingWorker<ListaCidades, Void> worker = new SwingWorker<>() {
            @Override
            protected ListaCidades doInBackground() throws Exception {
                String cidadesXML = WeatherForecastService.cidades(nomeCidade);
                XStreamParser<PrevisaoCidade, ListaCidades> xspCidades = new XStreamParser<>();
                return xspCidades.cidades(cidadesXML);
            }

            @Override
            protected void done() {
                try {
                    ListaCidades lista = get();
                    if (lista.getCidades() == null || lista.getCidades().isEmpty()) {
                        statusLabel.setText("Nenhuma cidade encontrada com o nome '" + nomeCidade + "'.");
                    } else {
                        lista.getCidades().forEach(listModel::addElement);
                        statusLabel.setText(lista.getCidades().size() + " cidade(s) encontrada(s). Selecione uma da lista.");
                    }
                } catch (ExecutionException e) {
                    handleError("Erro de rede ao buscar cidades", e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleError("Busca de cidades interrompida", e);
                } finally {
                    buscarButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void buscarPrevisao(Cidade cidade) {
        statusLabel.setText("Buscando previsão para " + cidade.getNome() + "...");
        previsaoPanel.removeAll();
        buscarButton.setEnabled(false);

        SwingWorker<PrevisaoCidade, Void> worker = new SwingWorker<>() {
            @Override
            protected PrevisaoCidade doInBackground() throws Exception {
                String previsaoXML = WeatherForecastService.previsoesParaSeteDias(cidade.getId());
                XStreamParser<PrevisaoCidade, ListaCidades> xspPrevisoes = new XStreamParser<>();
                return xspPrevisoes.previsao(previsaoXML);
            }

            @Override
            protected void done() {
                try {
                    PrevisaoCidade pc = get();
                    exibirPrevisoes(pc);
                } catch (ExecutionException e) {
                    handleError("Erro de rede ao buscar previsão", e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleError("Busca de previsão interrompida", e);
                } finally {
                    buscarButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void exibirPrevisoes(PrevisaoCidade previsaoCidade) {
        previsaoPanel.removeAll();
        statusLabel.setText("Previsão para " + previsaoCidade.getNome() + " - " + previsaoCidade.getUf());

        SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfSaida = new SimpleDateFormat("dd/MM (EEE)");
        String hoje = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        for (Previsao p : previsaoCidade.getPrevisoes()) {
            JPanel diaPanel = new JPanel(new BorderLayout(5, 5));
            diaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            diaPanel.setPreferredSize(new Dimension(150, 220));

            String dataFormatada;
            try {
                Date data = sdfEntrada.parse(p.getDia());
                dataFormatada = sdfSaida.format(data).toLowerCase();
            } catch (ParseException e) {
                dataFormatada = p.getDia();
            }

            JLabel dataLabel = new JLabel(dataFormatada, SwingConstants.CENTER);
            dataLabel.setFont(new Font("Arial", Font.BOLD, 14));

            ImageIcon icon = getIconeTempo(p.getTempo());
            JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);

            String descricao = DESCRICOES_TEMPO.getOrDefault(p.getTempo(), "Não definido");
            JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + descricao + "</div></html>", SwingConstants.CENTER);
            descLabel.setPreferredSize(new Dimension(120, 40));

            JLabel tempLabel = new JLabel(p.getMinima() + "°C - " + p.getMaxima() + "°C", SwingConstants.CENTER);
            tempLabel.setFont(new Font("Arial", Font.BOLD, 12));

            JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
            infoPanel.add(descLabel, BorderLayout.CENTER);
            infoPanel.add(tempLabel, BorderLayout.SOUTH);

            diaPanel.add(dataLabel, BorderLayout.NORTH);
            diaPanel.add(iconLabel, BorderLayout.CENTER);
            diaPanel.add(infoPanel, BorderLayout.SOUTH);

            Color panelColor = p.getDia().equals(hoje) ? new Color(225, 240, 255) : Color.WHITE;
            diaPanel.setBackground(panelColor);
            infoPanel.setBackground(panelColor);

            previsaoPanel.add(diaPanel);
        }
        previsaoPanel.revalidate();
        previsaoPanel.repaint();
    }
    
    private ImageIcon getIconeTempo(String sigla) {
        try {
            URL url = getClass().getResource("/icons/" + sigla + ".png");
            if (url == null) {
                System.err.println("Ícone local não encontrado: /icons/" + sigla + ".png");
                return new ImageIcon();
            }
            return new ImageIcon(url);
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone local: " + sigla + ".png");
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    private void handleError(String message, Throwable cause) {
        String errorMessage = message + ": " + cause.getMessage();
        JOptionPane.showMessageDialog(frame, errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Ocorreu um erro. Por favor, tente novamente.");
    }
    
    private static class CidadeListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Cidade) {
                Cidade c = (Cidade) value;
                label.setText(c.getNome() + " - " + c.getUf());
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
            return label;
        }
    }
}