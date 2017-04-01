package site;

import content.JsoupContent;
import content.Configuration;
import download.HttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Download result from baidu news and etc.
 */
public class SearchSites {

    private static final String queryUrl = "http://news.baidu.com/ns?word=%s";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final String name = "news.baidu.com";
    private static final String fieldResultLink = "resultLink";

    private SearchEngine engines[];
    private HttpClient client;

    public SearchSites(SearchEngine[] engines) {
        this.engines = engines;

        client = new HttpClient();
    }

    public String[] searchAll(String keyword) throws IOException {
        List<String> results = new ArrayList<>();
        for (SearchEngine engine: engines) {
            String requestUrl = String.format(engine.queryUrl, URLEncoder.encode(keyword, engine.charset));
            String aResult = client.getAsString(requestUrl, engine.charset);
            results.add(aResult);
        }
        return results.toArray(new String[0]);
    }

    public String[] parseResult(String htmlString) {
        JsoupContent jsoupContent = new JsoupContent(htmlString, queryUrl);
        return jsoupContent.parseLinks(Configuration.getSelector(name, fieldResultLink));
    }

    class SearchEngine {
        String name;
        String queryUrl;
        String charset;
    }
}
