package download;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Download result from baidu news and etc.
 */
public class BaiduSearch {

    private static final String queryUrlBaidu = "http://news.baidu.com/ns?word=%s";
    private static final Charset charset = StandardCharsets.UTF_8;

    private HttpClient client;

    public BaiduSearch() {
        client = new HttpClient();
    }

    public String searchNews(String keyword) throws IOException {
        String requestUrl = String.format(queryUrlBaidu, URLEncoder.encode(keyword, charset.name()));
        return client.getAsString(requestUrl);
    }
}
