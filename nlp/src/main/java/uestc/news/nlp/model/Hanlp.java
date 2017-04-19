package uestc.news.nlp.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import uestc.news.nlp.NlpResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dick Zhou on 4/19/2017.
 *
 */
public class Hanlp {

    private int lengthSummary;
    private int sizeKeywords;

    public Hanlp(int lengthSummary, int sizeKeywords) {
        this.lengthSummary = lengthSummary;
        this.sizeKeywords = sizeKeywords;
    }

    public NlpResult analyze(String content) {
        NlpResult result;

        String summary = HanLP.getSummary(content, lengthSummary);
        List<String> keyword = HanLP.extractKeyword(content, sizeKeywords);
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
