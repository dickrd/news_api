package content;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class PageData {

    /**
     * 网页数据爬取的URL
     */
    private String url;

    /**
     * 爬取时间戳
     */
    private long createdAt;

    /**
     * 爬取的内容
     */
    private Record data;

    public PageData(String url, long createdAt, Record data) {
        this.url = url;
        this.createdAt = createdAt;
        this.data = data;
    }
}
