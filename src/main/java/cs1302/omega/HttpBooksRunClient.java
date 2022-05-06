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
 * This is a public HttpBooksRunClient class. Represents a response from
 * the Booksrun Search API. This is used by Gson to
 * create an object from the JSON response body.
 */
public class HttpBooksRunClient {

    private String uri = "";
    private String isbn = "";

    private static String Key = "re5vd2327ybnjb5ugnwd";

    private static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).build();
    // builds and returns an HttpClient

    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String BOOKSRUN_API = "https://booksrun.com/api/price/sell/";

    /**
     * This is a getResults method that returns values of book using isbnValue.
     * @return HttpResultBooksRun
     * @param isbnVal
     */
    public HttpResultBooksRun getResults(String isbnVal) throws IOException, InterruptedException {

        isbn = URLEncoder.encode(isbnVal, StandardCharsets.UTF_8);

        String queryIsbn = BOOKSRUN_API + isbn;
        String query = String.format("?key=%s", Key);
        uri = queryIsbn + query;
        // build request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

        // send request / receive response in the form of a String
        HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

        // ensure the request is okay
        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        } // if

        // get request body (the content we requested)
        String jsonString = response.body();

        HttpResultBooksRun resultObj = GSON.fromJson(jsonString, HttpResultBooksRun.class);

        return resultObj;
    } //getResults

    /**
     * This is a getter method that returns the String URI.
     * @return uri String
     */
    public String getURI() {
        return uri;
    } //getURI


}
