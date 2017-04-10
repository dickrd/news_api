package content;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A record item.
 */
public class Record {
    String taskId;
    String url;
    String content;
    List<String> images;
    int readCount;
    int participateCount;
    int commentCount;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public void setParticipateCount(int participateCount) {
        this.participateCount = participateCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getCommentCount() {
        return commentCount;
    }

    @Override
    public String toString() {
        return String.format("%s, %d, %d, %d, %s, %s\n",
                url, readCount, participateCount, commentCount, Arrays.toString(images.toArray()), content);
    }
}
