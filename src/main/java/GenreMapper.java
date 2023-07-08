import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenreMapper {
    private static final OkHttpClient client = new OkHttpClient();

    public static Map<String, Integer> getGenres() throws IOException {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/genre/movie/list?api_key=" + API.API_KEY)
                .build();

        Map<String, Integer> genres = new HashMap<>();
        try (Response response = client.newCall(request).execute()) {
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray genresArray = jsonResponse.get("genres").getAsJsonArray();
            for (int i = 0; i < genresArray.size(); i++) {
                JsonObject genre = genresArray.get(i).getAsJsonObject();
                genres.put(genre.get("name").getAsString(), genre.get("id").getAsInt());
            }
        }

        return genres;
    }
}
