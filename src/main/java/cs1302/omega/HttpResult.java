package cs1302.omega;

/**
 * This class captures relevant information from openlib API and shares it among other classes.
 */
public class HttpResult {
    private String title;
    private String titleSuggest;
    private String isbn;
    private String picUrl;

    /**
     * This is a constructor method that contains paramenters.
     * @param titleVal
     * @param titlesuggestVal
     * @param isbnVal
     * @param picurlVal
     */
    public HttpResult(String titleVal, String titlesuggestVal, String isbnVal, String picurlVal) {
        title = titleVal;
        titleSuggest = titlesuggestVal;
        isbn = isbnVal;
        picUrl = picurlVal;
    }

    /**
     * This is a getter method that gets the title.
     * @return title;
     */
    public String getTitle() {
        return title;
    }

    /**
     * This is a getter method that gets the title.
     * @return title;
     */
    public String getTitleSuggest() {
        return titleSuggest;
    }

    /**
     * This is a getter method that gets the title.
     * @return title;
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * This is a getter method that gets the title.
     * @return title;
     */
    public String getPicUrl() {
        return picUrl;
    }
}
