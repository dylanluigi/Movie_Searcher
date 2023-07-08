
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
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
                resultPane.setText("Unknown genre: " + genreName);
                return;
            }

            List<MovieFilter.Movie> movies = MovieFilter.filterMovies(minScore, genreId, year);
            StringBuilder result = new StringBuilder("<html>");
            for (MovieFilter.Movie movie : movies) {
                result.append("<a href='https://www.themoviedb.org/movie/")
                        .append(movie.getId())
                        .append("'>")
                        .append(movie.getTitle())
                        .append("</a><br>");
            }
            resultPane.setText(result.toString());
            resultPanel.removeAll();
            for (MovieFilter.Movie movie : movies) {
                JLabel label = new JLabel();
                label.setText(String.format("<html><a href='https://www.themoviedb.org/movie/%s'>%s</a></html>", movie.getId(), movie.getTitle()));
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));

                ImageIcon icon = new ImageIcon(new URL(movie.getPosterPath()));
                JLabel poster = new JLabel();
                poster.setIcon(icon);

                JPanel moviePanel = new JPanel();
                moviePanel.add(poster);
                moviePanel.add(label);
                resultPanel.add(moviePanel);
            }
            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (IOException e) {
            resultPane.setText("Error: " + e.getMessage());
        }
    }

}
