package content;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A record item.
 */
public class Record {
    String url;
    String content;
    String images[];
    int readCount;
    int participateCount;
    int commentCount;

    void setUrl(String url) {
        this.url = url;
    }

    void setContent(String content) {
        this.content = content;
    }

    void setImages(List<String> images) {
        this.images = (String[]) images.toArray();
    }

    void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    void setParticipateCount(int participateCount) {
        this.participateCount = participateCount;
    }

    void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %d, %d, %d\n",
                url, content, Arrays.toString(images), readCount, participateCount, commentCount);
    }
}
