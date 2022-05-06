package cs1302.omega;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is a public HttpOpenLibClient class. This class represents a response from
 * the iTunes Search API. This is used by Gson to create an object from the JSON response body.
 */
public class HttpOpenLibClient {

    private String limit = "";
    private String uri = "";
    private HttpResult[] httpResult;

    /**
     * This is method for the HTTPCLIENT builder.
     * @return HttpClient
     */
    private static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).build();
    // builds and returns an HttpClient

    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String ITUNES_API = "http://openlibrary.org/search.json";

    /**
     * This method is an encoder for the limit.
     */
    public HttpOpenLibClient() {
        limit = URLEncoder.encode("15", StandardCharsets.UTF_8);
    }

    /**
     * The getResults method returns an array of type HttpResult based on API search
     * on book title, author, etc..
     * @param title String
     * @return HttpResult array
     */
    public HttpResult[] getResults(String title) throws IOException, InterruptedException {

        title = URLEncoder.encode(title, StandardCharsets.UTF_8);

        String query = String.format("?title=%s&limit=%s", title, limit);
        uri = ITUNES_API + query;

        // build request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
        // send request / receive response in the form of a String
        HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
        // ensure the request is okay
        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        } //if
        // get request body (the content we requested)
        String jsonString = response.body();

        System.out.println(jsonString);

        // use GSON to extract docs array
        ILibResponse ilibResponse = GSON.fromJson(jsonString, ILibResponse.class);

        httpResult = new HttpResult[ilibResponse.docs.length];

        if (ilibResponse == null || ilibResponse.docs.length == 0 ||
            ilibResponse.docs[0].cover == null) {
            return null;
        }

        // populate httpResult with information extracted from docs array
        // creates cover page url associated with cover information
        for (int i = 0; i < ilibResponse.docs.length; i++) {

            ILibResult result = ilibResponse.docs[i];
            String cover = "";
            cover = "https://covers.openlibrary.org/b/ID/" +
                String.valueOf(result.cover) + "-M.jpg";

            if (!(result.isbn == null || result.isbn.length == 0)) {
                httpResult[i] = new HttpResult(result.title, result.titleSuggest,
                result.isbn[0], cover);
            }

        } // for
        return httpResult;
    }

    /**
     * The getURI method returns the current url used in the API call.
     * @return String uri
     */
    public String getURI() {
        return uri;
    }

    /**
     * The readCoverImg method downloads the image url in a byte array to be used
     * for the book cover display.
     * @param url String
     * @return data array
     */
    public byte[] readCoverImg(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] data = response.body();
            return data;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

}
