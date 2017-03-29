package message;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class SearchRequest {
    private String keywords[];

    public SearchRequest(String[] keywords) {
        this.keywords = keywords;
    }

    public String[] getKeywords() {
        return keywords;
    }
}
