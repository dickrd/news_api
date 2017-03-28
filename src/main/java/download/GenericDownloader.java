package download;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Download search result from baidu news search.
 */
public class GenericDownloader {

    private static final String queryUrl = "http://news.baidu.com/ns?word=%s";
    private static final Charset charset = StandardCharsets.UTF_8;

    private HttpClient client;

    public GenericDownloader() {
        client = new HttpClient();
    }

    public String search(String keyword) throws IOException {
        String requestUrl = String.format(queryUrl, URLEncoder.encode(keyword, charset.name()));
        return client.getAsString(requestUrl);
    }

    public String download(String url) throws IOException {
        return client.getAsString(url);
    }
}
