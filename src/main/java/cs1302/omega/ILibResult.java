package cs1302.omega;

import com.google.gson.annotations.SerializedName;

/**
 * This class contains variables that are used in othre classes.
 */
public class ILibResult {

    public String title;
    public String titleSuggest;
    public String[] isbn;
    @SerializedName("cover_i")
    public Integer cover;
    //public Integer cover_i;
} // ILibResult
