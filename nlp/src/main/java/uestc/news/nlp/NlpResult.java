package uestc.news.nlp;

/**
 * Created by Dick Zhou on 4/18/2017.
 *
 */
public class NlpResult {
    private String summary;
    private String keywords[];
    private Comment comments[];

    private String people[];
    private String places[];
    private String organizations[];

    public NlpResult(String summary, String[] keywords, Comment[] comments) {
        this.summary = summary;
        this.keywords = keywords;
        this.comments = comments;
    }

    public String getSummary() {
        return summary;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public Comment[] getComments() {
        return comments;
    }

    public String[] getPeople() {
        return people;
    }

    public void setPeople(String[] people) {
        this.people = people;
    }

    public String[] getPlaces() {
        return places;
    }

    public void setPlaces(String[] places) {
        this.places = places;
    }

    public String[] getOrganizations() {
        return organizations;
    }

    public void setOrganizations(String[] organizations) {
        this.organizations = organizations;
    }

    public class Comment {
        private String content;
        private float sentiment;
    }
}
