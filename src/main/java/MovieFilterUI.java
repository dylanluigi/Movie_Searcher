
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MovieFilterUI {
    private static final JTextField scoreField = new JTextField(5);
    private static final JComboBox<String> genreBox;
    private static final JTextField yearField = new JTextField(5);
    private static final JEditorPane resultPane = new JEditorPane();
    private static final JPanel resultPanel = new JPanel();

    static {
        Map<String, Integer> genres;
        try {
            genres = GenreMapper.getGenres();
        } catch (IOException e) {
            throw new RuntimeException("Error fetching genres", e);
        }

        genreBox = new JComboBox<>(genres.keySet().toArray(new String[0]));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MovieFilterUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Movie Filter");
        frame.setBackground(Color.BLACK);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Min Score:"));
        inputPanel.add(scoreField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreBox);
        inputPanel.add(new JLabel("Year:"));
        inputPanel.add(yearField);
        JButton submitButton = new JButton("Submit");
        inputPanel.add(submitButton);
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // adjust this value to suit your needs
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        resultPane.setEditable(false);
        resultPane.setContentType("text/html");
        resultPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        submitButton.addActionListener(e -> submit());

        // KeyListener to submit when Enter is pressed
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submit();
                }
            }
        };

        // Add KeyListener to fields
        scoreField.addKeyListener(enterAdapter);
        genreBox.addKeyListener(enterAdapter);
        yearField.addKeyListener(enterAdapter);

        frame.pack();
        frame.setVisible(true);
    }

    private static void submit() {
        double minScore = Double.parseDouble(scoreField.getText());
        String genreName = (String) genreBox.getSelectedItem();
        int year = Integer.parseInt(yearField.getText());
        try {
            Map<String, Integer> genres = GenreMapper.getGenres();
            Integer genreId = genres.get(genreName);
            if (genreId == null) {
                resultPanel.removeAll();
                resultPanel.add(new JLabel("Unknown genre: " + genreName));
                resultPanel.revalidate();
                resultPanel.repaint();
                return;
            }
            List<MovieFilter.Movie> movies = MovieFilter.filterMovies(minScore, genreId, year);
            resultPanel.removeAll();
            for (MovieFilter.Movie movie : movies) {
                JLabel title = new JLabel();
                title.setText(String.format("<html><a href='https://www.themoviedb.org/movie/%s'>%s</a></html>", movie.getId(), movie.getTitle()));
                title.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JTextArea description = new JTextArea(movie.getDescription());
                description.setLineWrap(true);
                description.setWrapStyleWord(true);
                description.setEditable(false);
                description.setFocusable(false);
                description.setBackground(UIManager.getColor("Label.background"));
                description.setFont(UIManager.getFont("Label.font"));
                description.setBorder(UIManager.getBorder("Label.border"));

                ImageIcon icon = new ImageIcon(new URL(movie.getPosterPath()));
                JLabel poster = new JLabel();
                poster.setIcon(icon);

                JPanel moviePanel = new JPanel();
                moviePanel.setLayout(new BoxLayout(moviePanel, BoxLayout.Y_AXIS));
                moviePanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // add space between movies
                moviePanel.add(poster);
                moviePanel.add(title);
                moviePanel.add(description);

                JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // Changed GridBagLayout to FlowLayout with LEFT alignment
                outerPanel.add(moviePanel);
                resultPanel.add(outerPanel);
            }
            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (IOException e) {
            resultPanel.removeAll();
            resultPanel.add(new JLabel("Error: " + e.getMessage()));
            resultPanel.revalidate();
            resultPanel.repaint();
        }
    }






}
