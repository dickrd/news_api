package uestc.news.nlp.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import uestc.news.nlp.NlpResult;
import uestc.news.nlp.NlpWorker;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Dick Zhou on 4/19/2017.
 *
 */
class HanlpTest {

    @Test
    void testAnalyze() {
        NlpResult nlpResult = new Hanlp(NlpWorker.LENGTH_SUMMARY, NlpWorker.SIZE_KEYWORDS).analyze("");

        System.out.println(new Gson().toJson(nlpResult));
        assertTrue(nlpResult.getSummary().length() > 0);
    }
}