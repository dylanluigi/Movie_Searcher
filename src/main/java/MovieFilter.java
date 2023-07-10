import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MovieFilter {

    private static final String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String POSTER_URL = "https://image.tmdb.org/t/p/w200";

    public static class Movie {
        private final String id;
        private final String title;
        private final String posterPath;
        private final String description;

        public Movie(String id, String title, String posterPath, String description) {
            this.id = id;
            this.title = title;
            this.posterPath = posterPath;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getPosterPath() {
            return posterPath;
        }

        public String getDescription() {
            return description;
        }
    }

    public static List<Movie> filterMovies(double minScore, int genreId, int year) throws IOException {
        String urlString = DISCOVER_URL + "?api_key=" + API.API_KEY + "&with_genres=" + genreId + "&primary_release_year=" + year;

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (Reader reader = new InputStreamReader(connection.getInputStream())) {
            JsonObject response = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray moviesArray = response.get("results").getAsJsonArray();

            return StreamSupport.stream(moviesArray.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(movie -> movie.get("vote_average").getAsDouble() >= minScore)
                    .map(movie -> new Movie(
                            movie.get("id").getAsString(),
                            movie.get("title").getAsString(),
                            POSTER_URL + movie.get("poster_path").getAsString(),
                            movie.get("overview").getAsString()))
                    .collect(Collectors.toList());
        }
    }
}
