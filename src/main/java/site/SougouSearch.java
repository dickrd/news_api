package site;

import download.HttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dick Zhou on 4/1/2017.
 *
 */
public class SougouSearch {

    public static final String queryUrlBaidu = "http://news.sogou.com/news?query=%s";

    private static final Charset charset = StandardCharsets.UTF_8;

    private HttpClient client;

    public SougouSearch() {
        client = new HttpClient();
    }

    public String search(String keyword) throws IOException {
        String requestUrl = String.format(queryUrlBaidu, URLEncoder.encode(keyword, charset.name()));
        return client.getAsString(requestUrl, charset);
    }
}
