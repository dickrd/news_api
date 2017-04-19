package uestc.news.nlp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hehehey.ghost.message.Response;
import com.hehehey.ghost.record.PageData;
import com.hehehey.ghost.util.HttpClient;

import java.io.IOException;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 4/18/2017.
 *
 */
public class Nlp {

    private static final Logger logger = Logger.getLogger(Nlp.class.getName());

    private static final String baseUrl = "http://127.0.0.1:666/";
    private static final String dataUrl = "data/%s?page=%d&size=%d";
    private static final String dataSubmitUrl = "data/%s";

    private static final int LENGTH_SUMMARY = 50;
    private static final int SIZE_KEYWORDS = 5;

    private Gson gson;
    private HttpClient client;

    public Nlp() {
        gson = new Gson();
        client = new HttpClient();
    }

    public static void main(String[] args) throws IOException {
        Logger.getGlobal().addHandler(new ConsoleHandler());
        new Nlp().analyze("i15b6109df77", 0, 5);
    }

    public void analyze(String taskId, int page, int size) throws IOException {
        String string = client.getAsString(baseUrl + String.format(dataUrl, taskId, page, size));
        Response response = gson.fromJson(string, Response.class);
        if (response.getStatus() != Response.Status.ok)
            throw new IOException("Master info: " + response.getData());

        response = gson.fromJson(string,
                new TypeToken<Response<PageData[]>>() {
                }.getType());
        PageData[] dataArray = ((PageData[]) response.getData());
        for (PageData data : dataArray) {
            try {
                String content = data.getData().get("content").toString();
                NlpResult nlpResult = analyze(content);
                PageData theData = new PageData(data.getUrl(),
                        System.currentTimeMillis(),
                        gson.fromJson(gson.toJson(nlpResult), new TypeToken<HashMap<String, Object>>() {
                        }.getType()));

                putResult(taskId, theData);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Put data failed for " + data.getUrl() + " : " + e.toString());
            }
        }
    }

    private void putResult(String id, PageData data) throws IOException {
        String string = client.putString(baseUrl + String.format(dataSubmitUrl, "nlp" + id), gson.toJson(data));
        logger.log(Level.FINE, string);
    }

    private NlpResult analyze(String content) {
        NlpResult result;

        String summary = HanLP.getSummary(content, LENGTH_SUMMARY);
        List<String> keyword = HanLP.extractKeyword(content, SIZE_KEYWORDS);
        result = new NlpResult(summary, keyword.toArray(new String[0]), new NlpResult.Comment[0]);

        List<Term> terms = HanLP.newSegment().enableAllNamedEntityRecognize(true).seg(content);
        Set<String> people = new HashSet<>();
        Set<String> places = new HashSet<>();
        Set<String> organizations = new HashSet<>();
        for (Term term : terms) {
            switch (term.nature) {
                case nr:
                case nr1:
                case nr2:
                case nrj:
                case nrf:
                    people.add(term.word);
                    break;
                case ns:
                case nsf:
                    places.add(term.word);
                    break;
                case nt:
                case ntc:
                case ntcb:
                case ntcf:
                case ntch:
                case nth:
                case nto:
                case nts:
                case ntu:
                    organizations.add(term.word);
                    break;
            }
        }

        String[] empty = new String[0];
        result.setPeople(people.toArray(empty));
        result.setPlaces(places.toArray(empty));
        result.setOrganizations(organizations.toArray(empty));

        return result;
    }
}
