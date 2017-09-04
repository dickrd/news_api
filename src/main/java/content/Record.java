package content;

import util.CommentInfo;
import webpages.NeteaseNews;

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
    List<String[]> images;
    int readCount;
    int participateCount;
    int commentCount;
    List<CommentInfo> hotComments;
    List<CommentInfo> newComments;
    String postTime;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImages(List<String[]> images) {
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

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public List<String[]> getImages() {
        return images;
    }

    public int getReadCount() {
        return readCount;
    }

    public int getParticipateCount() {
        return participateCount;
    }

    public List<CommentInfo> getHotComments() {
        return hotComments;
    }

    public void setHotComments(List<CommentInfo> hotComments) {
        this.hotComments = hotComments;
    }

    public List<CommentInfo> getNewComments() {
        return newComments;
    }

    public void setNewComments(List<CommentInfo> newComments) {
        this.newComments = newComments;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    @Override
    public String toString() {
        return String.format("%s, %d, %d, %d, %s, %s\n",
                url, readCount, participateCount, commentCount, Arrays.toString(images.toArray()), content);
    }
}
