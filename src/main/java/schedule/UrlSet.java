package schedule;

/**
 * Created by Dick Zhou on 3/29/2017.
 * A set for unique urls.
 */
public interface UrlSet {

    /**
     * Add a url to the set.
     * @param url url to add.
     * @throws Exception Throws if an error happened.
     */
    void addUrl(String url) throws Exception;

    /**
     * Retrieve a url that never have been downloaded.
     * @return A unique url.
     * @throws Exception Throws if an error happened.
     */
    String getUrl() throws Exception;
}
