package uestc.news.nlp;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Dick Zhou on 4/18/2017.
 *
 */
class NlpTest {

    Nlp nlp = new Nlp();

    @Test
    void testAnalyze() {
        try {
            nlp.analyze("i15b6109df77", 0, 1);
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}