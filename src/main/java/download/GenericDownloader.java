package download;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Download search result from baidu news search.
 */
public class GenericDownloader {

    private static final String queryUrlBaidu = "http://news.baidu.com/ns?word=%s";
    public static final String apiUrlNeteaseCount = "http://sdk.comment.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/%s";
    private static final Charset charset = StandardCharsets.UTF_8;

    private HttpClient client;

    public GenericDownloader() {
        client = new HttpClient();
    }

    public String search(String keyword) throws IOException {
        String requestUrl = String.format(queryUrlBaidu, URLEncoder.encode(keyword, charset.name()));
        return client.getAsString(requestUrl);
    }

    public String getNeteaseCount(String url) throws IOException {
        String[] split = url.split("/");
        String last = split[split.length - 1];
        String id = last.substring(0, last.indexOf("."));
        return client.getAsString(String.format(apiUrlNeteaseCount, id));
    }

    public String download(String url, Charset charset) throws IOException {
        return client.getAsString(url, charset);
    }
}
