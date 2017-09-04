package main.java.webpages;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import main.java.content.Record;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import main.java.util.CommentInfo;
import main.java.util.ImageDownload;
import main.java.util.JsonToArrayList;

import java.io.IOException;
import java.util.*;

/**
 * Created by first1hand on 2017/4/9.
 * 网易新闻
 */
public class NeteaseNews {
    private String taskId;
    private String url;
    private Map<String,String> headers = new HashMap<String,String>(){{
        put("Host","news.163.com");
        put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
        put("Accept","text/html,application/xhtml+xml,image/jxr,*/*");
        put("Accept-Encoding","gzip,deflate");
        put("Cookie","_ntes_nnid=6d16c4d9bfd5df3493550fe223093175,1479728169915; _ntes_nuid=6d16c4d9bfd5df3493550fe223093175; vinfo_n_f_l_n3=369a3e05fd0a92f7.1.34.1480053863984.1491786355492.1491793097195; __oc_uuid=03b376d0-b2d5-11e6-bc58-0344ff28a079; __utma=187553192.678762976.1480053865.1481201461.1481447954.17; __utmz=187553192.1481447954.17.17.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; P_INFO=m13540653368@163.com|1484530159|0|other|00&99|sic&1483925190&other#sic&510100#10#0#0|135368&1||13540653368@163.com; NTES_CMT_USER_INFO=105031559%7Cm135****3368%7C%7Cfalse%7CbTEzNTQwNjUzMzY4QDE2My5jb20%3D; usertrack=c+5+hVhFZRIJ7TLvBd8uAg==; mail_psc_fingerprint=e7b2d3f7b095411bda3b22d25b4ed5b4; Province=028; City=028; vjuids=751f67f47.15b51d47361.0.19dc027329f558; vjlast=1491726529.1491791637.13; ne_analysis_trace_id=1491791636834; s_n_f_l_n3=369a3e05fd0a92f71491791636844; afpCT=1" );
    }};

    public NeteaseNews(String taskId,String url){
            this.taskId = taskId;
            this.url = url;
    }

    public Record dealNeteasenews(){
        try {
            Record record = new Record();
            record.setUrl(url);
            record.setTaskId(taskId);
            Document doc = Jsoup
                            .connect(url)
                            .headers(headers)
                            .timeout(6000)
                            .get();

            if(!url.contains("photoview")) {

                String postOriginalTime[] = doc.getElementsByClass("post_time_source").get(0).text().split("来源");
                String postTime = postOriginalTime[0];
                System.out.println(postTime);
                record.setPostTime(postTime);

                //System.out.println(doc);
                Elements paragraphs = doc.getElementsByClass("post_text").get(0).children();
                String content = "";
                for (Element element : paragraphs) {
                    if (element.tagName().equals("p") && !element.hasClass("f_center"))
                        content = content + element.text();
                }
                System.out.println(content);
                record.setContent(content);
                //System.out.println(content);

                String urlSplit[] = url.split("/");
                String newsId = urlSplit[urlSplit.length - 1].split("\\.")[0].split("_")[0];
                //System.out.println(newsId);
                String commentInfoStr = Jsoup
                        .connect("http://comment.news.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/" + newsId)
                        .ignoreContentType(true)
                        .timeout(6000)
                        .get().getElementsByTag("body").text();
                JsonObject commentInfoJson = new JsonParser().parse(commentInfoStr).getAsJsonObject();
                //System.out.print(commentInfoJson.get("against").getAsInt());
                int participateCount = Integer.parseInt(commentInfoJson.get("cmtAgainst").toString())
                        + Integer.parseInt(commentInfoJson.get("cmtVote").toString())
                        + Integer.parseInt(commentInfoJson.get("rcount").toString());
                record.setParticipateCount(participateCount);
                //System.out.println(commentInfoStr);


                //hotComment
                HashSet<String> hotCommentIdSet = new HashSet<>();
                String hotCommentsStr = Jsoup.connect("http://comment.news.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+newsId+"/comments/hotList?offset=0&limit=40&showLevelThreshold=72&headLimit=1&tailLimit=2&callback=getData&ibc=newspc")
                                             .ignoreContentType(true)
                                             .timeout(6000)
                                             .get().getElementsByTag("body").get(0).text();
                String hotCommentsJsonStr = hotCommentsStr.replaceFirst("getData\\(","").substring(0,hotCommentsStr.replaceFirst("getData\\(","").length()-2);
                JsonObject hotCommentsInfo = new JsonParser().parse(hotCommentsJsonStr).getAsJsonObject();
                String hotCommentsStrReal = "{\"comments\":"+hotCommentsInfo.get("comments").getAsJsonObject().toString()+"}";
                List<CommentInfo> hotCommentInfos = new ArrayList<>();
                produceCommentInfoList(hotCommentsStrReal,hotCommentInfos, hotCommentIdSet);
                record.setHotComments(hotCommentInfos);
                //System.out.println(hotCommentInfos.get(1).getComment());

                //newComment
                Set<String> newCommentIdSet = new HashSet<>();
                int offset = 0;
                int newListSize = 1;

                List<CommentInfo> newCommentInfos = new ArrayList<>();
                while(offset<newListSize&&newCommentInfos.size()<=10000){
                    String newCommentsStr = Jsoup.connect("http://comment.news.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+newsId+"/comments/newList?offset="+offset+"&limit=30&showLevelThreshold=72&headLimit=1&tailLimit=2&callback=getData&ibc=newspc")
                            .ignoreContentType(true)
                            .timeout(6000)
                            .get().getElementsByTag("body").get(0).text();
                    String newCommentsJsonStr = newCommentsStr.replaceFirst("getData\\(","").substring(0,newCommentsStr.replaceFirst("getData\\(","").length()-2);
                    JsonObject newCommentsInfo = new JsonParser().parse(newCommentsJsonStr).getAsJsonObject();
                    String newCommentsStrReal = "{\"comments\":"+newCommentsInfo.get("comments").getAsJsonObject().toString()+"}";
                    produceCommentInfoList(newCommentsStrReal, newCommentInfos, newCommentIdSet);
                    newListSize = newCommentsInfo.get("newListSize").getAsInt();
                    offset = offset+30;
                }
                record.setNewComments(newCommentInfos);

                int commentCount = Integer.parseInt(commentInfoJson.get("tcount").toString());
                record.setCommentCount(commentCount);

                List<String[]> images = new ArrayList<>();
                Elements paragraphsContainImgs = doc.getElementsByClass("post_text").get(0).children();
                int imgsCount = 0;
                for (Element element : paragraphsContainImgs) {
                    //System.out.println("img:"+element.select("img").size());
                    if(element.tagName().equals("p")&&element.select("img").size()!=0){
                        //System.out.println(element);
                        String tempImg[] = new String[2];
                        if(!element.text().equals("")){
                            //System.out.println(element.text());
                            tempImg[0] = element.text();
                        }else {
                            //System.out.println("无描述！");s
                            tempImg[0] = "无描述！";
                        }
                        tempImg[1] = ImageDownload.imageDownload((element.children().attr("src")));
                        images.add(tempImg);
                        imgsCount++;
                    }
                }

                System.out.println("图片数量：" + imgsCount);
                System.out.println(record.getNewComments().size());
                System.out.println(newCommentIdSet.size());
                for(CommentInfo comment:record.getNewComments()){
                    System.out.println(comment.getComment());
                }
                /*
                for(String[] img:images){
                    System.out.println(img[0]+":"+img[1]);
                }
                */
                record.setImages(images);

            }else {
                String postTime = doc.getElementsByClass("headline").get(0).getElementsByTag("span").text();
                System.out.println(postTime);
                record.setPostTime(postTime);

                String content = doc.getElementsByClass("overview").text();
                System.out.println(content);
                record.setContent(content);

                String urlWithNewsId = doc.getElementsByClass("comment js-tielink").get(0).attr("href");
                String urlSplit[] = urlWithNewsId.split("/");
                String newsId = urlSplit[urlSplit.length-1].split("\\.")[0];
                System.out.println(newsId);
                String commentInfoStr = Jsoup
                                    .connect("http://comment.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+newsId)
                                    .ignoreContentType(true)
                                    .timeout(6000)
                                    .get().getElementsByTag("body").text();
                JsonObject commentInfoJson = new JsonParser().parse(commentInfoStr).getAsJsonObject();
                int participateCount = Integer.parseInt(commentInfoJson.get("cmtAgainst").toString())
                        + Integer.parseInt(commentInfoJson.get("cmtVote").toString())
                        + Integer.parseInt(commentInfoJson.get("rcount").toString());
                record.setParticipateCount(participateCount);
                System.out.println(participateCount);

                HashSet<String> hotCommentIdSet = new HashSet<>();
                String hotCommentsStr = Jsoup.connect("http://comment.news.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+newsId+"/comments/hotList?offset=0&limit=40&showLevelThreshold=72&headLimit=1&tailLimit=2&callback=getData&ibc=newspc")
                        .ignoreContentType(true)
                        .timeout(6000)
                        .get().getElementsByTag("body").get(0).text();
                String hotCommentsJsonStr = hotCommentsStr.replaceFirst("getData\\(","").substring(0,hotCommentsStr.replaceFirst("getData\\(","").length()-2);
                JsonObject hotCommentsInfo = new JsonParser().parse(hotCommentsJsonStr).getAsJsonObject();
                String hotCommentsStrReal = "{\"comments\":"+hotCommentsInfo.get("comments").getAsJsonObject().toString()+"}";
                List<CommentInfo> hotCommentInfos = new ArrayList<>();
                produceCommentInfoList(hotCommentsStrReal,hotCommentInfos,hotCommentIdSet);
                //System.out.println(hotCommentInfos.get(0).getUserName());
                record.setHotComments(hotCommentInfos);

                //newComment
                int offset = 0;
                int newListSize = 1;
                HashSet<String> newCommentIdSet = new HashSet<>();
                List<CommentInfo> newCommentInfos = new ArrayList<>();

                while(offset<newListSize&&newCommentInfos.size()<=100000){
                    String newCommentsStr = Jsoup.connect("http://comment.news.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/"+newsId+"/comments/newList?offset="+offset+"&limit=30&showLevelThreshold=72&headLimit=1&tailLimit=2&callback=getData&ibc=newspc")
                            .ignoreContentType(true)
                            .timeout(6000)
                            .get().getElementsByTag("body").get(0).text();
                    String newCommentsJsonStr = newCommentsStr.replaceFirst("getData\\(","").substring(0,newCommentsStr.replaceFirst("getData\\(","").length()-2);
                    JsonObject newCommentsInfo = new JsonParser().parse(newCommentsJsonStr).getAsJsonObject();
                    String newtCommentsStrReal = "{\"comments\":"+newCommentsInfo.get("comments").getAsJsonObject().toString()+"}";
                    produceCommentInfoList(newtCommentsStrReal, newCommentInfos, newCommentIdSet);
                    newListSize = newCommentsInfo.get("newListSize").getAsInt();
                    offset = offset+30;
                }
                record.setNewComments(newCommentInfos);

                int commentCount = Integer.parseInt(commentInfoJson.get("tcount").toString());
                record.setCommentCount(commentCount);
                System.out.println(commentCount);

                List<String[]> images = new ArrayList<>();
                String imagesInfoStr = doc.getElementsByTag("textarea").text();
                String imagesInfoStrList = new JsonParser()
                                                .parse(imagesInfoStr)
                                                .getAsJsonObject().get("list")
                                                .toString();
                List<JsonObject> imagesInfoJsonList = JsonToArrayList.jsonToArrayList(imagesInfoStrList);
                System.out.println("图片数量："+imagesInfoJsonList.size());
                for(JsonObject object:imagesInfoJsonList){
                   String imageTemp[] = new String[2];
                   imageTemp[0] = object.get("title").toString();
                   imageTemp[1] = ImageDownload.imageDownload(object.get("img").getAsString());
                   images.add(imageTemp);
                }
                record.setImages(images);

                record.setReadCount(-1);

            }
            record.setReadCount(-1);
            return record;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class Comment{
        private int against;
        private boolean anonymous;
        private int buildLevel;
        private String commentId;
        private String content;
        private String createTime;
        private String favCount;
        private String ip;
        private boolean isDel;
        private String postId;
        private String productKey;
        private int shareCount;
        private String siteName;
        private String source;
        private boolean unionState;
        private JsonObject user;
        private int vote;

        public String getContent() {
            return content;
        }

        JsonObject getUser() {
            return user;
        }

        String getCommentId() {
            return commentId;
        }
    }

    private class Comments{
        private Map<String,Comment> comments;

        Map<String, Comment> getComments() {
            return comments;
        }
    }

    private void produceCommentInfoList(String commentsStr,List<CommentInfo> commentInfos,Set<String> commentsIdSet){
        Comments commentsObj = new Gson().fromJson(commentsStr,Comments.class);
        Map<String,Comment> comments = commentsObj.getComments();
        for (Map.Entry<String,Comment> pair:comments.entrySet()){
            if(!commentsIdSet.contains(pair.getValue().getContent())) {
                //System.out.println(pair.getValue().getCommentId());
                CommentInfo commentInfo = new CommentInfo();
                commentInfo.setComment(pair.getValue().getContent());
                commentInfo.setCommentId(pair.getValue().getCommentId());
                if (!pair.getValue().getUser().get("userId").getAsString().equals("0")) {
                    commentInfo.setUserId(pair.getValue().getUser().get("userId").getAsString());
                } else
                    commentInfo.setUserId(null);
                if (pair.getValue().getUser().has("nickname")) {
                    commentInfo.setUserName(pair.getValue().getUser().get("nickname").getAsString());
                } else
                    commentInfo.setUserName(null);
                commentInfo.setLocation(pair.getValue().getUser().get("location").getAsString());
                commentInfos.add(commentInfo);
                commentsIdSet.add(pair.getValue().getContent());
            }
        }
//        System.out.println(commentInfos.size());
    }
}
