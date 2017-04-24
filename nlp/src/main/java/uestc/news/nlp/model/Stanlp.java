package uestc.news.nlp.model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import uestc.news.nlp.NlpResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dick Zhou on 4/20/2017.
 *
 */
public class Stanlp {
    public NlpResult analyze(String content) {
        NlpResult result;

        StanfordCoreNLP nlp = new StanfordCoreNLP("StanfordCoreNLP-chinese.properties");
        Annotation annotation = new Annotation(content);
        nlp.annotate(annotation);
        List<CoreMap> maps = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence: maps) {
            String ner = sentence.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        }
        //String summary = HanLP.getSummary(content, lengthSummary);
        //List<String> keyword = HanLP.extractKeyword(content, sizeKeywords);
        result = new NlpResult(null, null, new NlpResult.Comment[0]);

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
