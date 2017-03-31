package content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Parse content of page using Jsoup.
 */
public class JsoupContentParser {

    public static Record parseRecord(String htmlString, String baseUrl, Selectors selectors) {
        Record record = new Record();
        Document document = Jsoup.parse(htmlString, baseUrl);

        record.setUrl(baseUrl);
        record.setContent(document.select(selectors.contentSelector).text());

        if (selectors.commentCountSelector != null && !selectors.commentCountSelector.contentEquals(""))
            record.setCommentCount(Integer.parseInt(document.select(selectors.commentCountSelector).get(0).text()));
        if (selectors.participateCountSelector != null && !selectors.participateCountSelector.contentEquals(""))
            record.setParticipateCount(Integer.parseInt(document.select(selectors.participateCountSelector).get(0).text()));
        if (selectors.readCountSelector != null && !selectors.readCountSelector.contentEquals(""))
            record.setReadCount(Integer.parseInt(document.select(selectors.readCountSelector).text()));

        return record;
    }

    public static List<String> parseLinks(String htmlString, String baseUrl, Selectors selectors) {
        List<String> resultUrls = new ArrayList<>();

        if (selectors.linkSelector == null || selectors.linkSelector.contentEquals(""))
            return resultUrls;

        Document document = Jsoup.parse(htmlString, baseUrl);
        for (Element element: document.select(selectors.linkSelector)) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (href.contains("#"))
                    resultUrls.add(href.substring(0, href.lastIndexOf("#")));
                else
                    resultUrls.add(href);
            }
        }

        return resultUrls;
    }

    /**
     * Created by Dick Zhou on 3/28/2017.
     * Selectors used when parse record.
     */
    static class Selectors {
        String name;

        String contentSelector;
        String readCountSelector;
        String participateCountSelector;
        String commentCountSelector;

        String linkSelector;
    }
}
