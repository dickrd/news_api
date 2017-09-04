package webpages;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.bcel.internal.generic.DCMPG;
import content.Record;
import message.TaskAssignment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.INTERNAL;
import util.CommentInfo;
import util.ImageDownload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by first1hand on 2017/4/6.
 * 处理微博内容
 */
public class WeiboPage {
    private String taskId;
    private String url;
    private Map<String,String> headers = new HashMap<String,String>(){{
        put("Host","weibo.com");
        put("User-Agent","Mozilla/5.0 (X11; Linux x86_64; rv:51.0) Gecko/20100101 Firefox/51.0");
        put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        put("Accept-Encoding","gzip, deflate");
        put("Cookie","UOR=www.doc88.com,widget.weibo.com,blog.csdn.net; SINAGLOBAL=8017627439482.008.1481705083315; ULV=1491384746388:5:1:1:5984810241704.7.1491384746383:1490273590464; SUB=_2AkMvjrgpf8NhqwJRmP4Qy2LmZIl0zAvEieKZ0knyJRMxHRl-yT83qlZetRAiYTRq4YfJLFid8-swoGNnVen7ow..; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9WhFY4U.yxuwLJuwHSQ0zM6F; TC-V5-G0=52dad2141fc02c292fc30606953e43ef; YF-V5-G0=694581d81c495bd4b6d62b3ba4f9f1c8; _s_tentry=blog.csdn.net; TC-Page-G0=07e0932d682fda4e14f38fbcb20fac81; Apache=5984810241704.7.1491384746383; YF-Ugrow-G0=ad83bc19c1269e709f753b172bddb094; YF-Page-G0=b9004652c3bb1711215bacc0d9b6f2b5; TC-Ugrow-G0=370f21725a3b0b57d0baaf8dd6f16a18");
    }};

    public WeiboPage(String taskId,String url){
        try {
            this.taskId = taskId;
            this.url = java.net.URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.url = null;
        }
    }

    public  Record dealWeiboPage(){
        try {
            Document doc = Jsoup
                    .connect(url)
                    .headers(headers)
                    .timeout(6000)
                    .get();
            //System.out.print(doc);
            Record record = new Record();
            record.setTaskId(taskId);
            record.setUrl(url);
            String tempData = null;
            Elements elements = doc.getElementsByTag("script");
            for (Element element : elements
                    ) {
                tempData = element.data();
                if (tempData.contains("pl.content.weiboDetail.index")) {
                    //System.out.println(tempData);
                    String contentJson = tempData.substring(8, tempData.length()-1);
                   // System.out.println(contentJson);
                    JsonObject json = new JsonParser().parse(contentJson).getAsJsonObject();
                    String html;
                    if (json.has("html")) {
                        html = json.get("html").getAsString();
                    } else
                        return null;

                    Document docNew = Jsoup.parse(html);
                    String content = docNew.getElementsByClass("WB_text W_f14").text();
                    System.out.println(content);
                    record.setContent(content);

                    String commentAttr = docNew.getElementsByAttributeValue("action-type", "fl_comment").get(0).text();
                    String commentCount = commentAttr.split(String.valueOf(commentAttr.charAt(0)))[1];
                    if (commentCount.equals("评论"))
                        record.setCommentCount(0);
                    else
                        record.setCommentCount(Integer.parseInt(commentCount));


                    List<CommentInfo> comments = new ArrayList<>();

                    int pageNum = 2;
                    String weiboId = docNew.getElementsByClass("WB_cardwrap WB_feed_type S_bg2 ").get(0).attr("mid");
                    System.out.println(weiboId);
                    String commentsJsonUrl = "http://weibo.com/aj/v6/comment/big?ajwvr=6&id="+weiboId+"&from=singleWeiBo&page="+pageNum;
                    String commentsUrlJsonStr = Jsoup.connect(commentsJsonUrl)
                            .headers(headers)
                            .ignoreContentType(true)
                            .timeout(6000).execute().body();

                    String commentsUrl = new JsonParser().parse(commentsUrlJsonStr).getAsJsonObject().get("data").getAsJsonObject().get("html").getAsString();

                    Elements commentListElements = Jsoup.parse(commentsUrl).getElementsByAttributeValue("node-type","root_comment");
                    System.out.println(commentListElements.size());

                    for(Element commentElement: commentListElements){
                        CommentInfo tempComment = new CommentInfo();
                        List<String> commentImgList = new ArrayList<>();
                        Elements list_con = commentElement.getElementsByClass("list_con");
                        Element WB_text = list_con.get(0).getElementsByClass("WB_text").get(0);
                        Element userInfo = WB_text.getElementsByTag("a").get(0);
                        tempComment.setUserName(userInfo.text());
                        tempComment.setUserId(userInfo.attr("usercard").replaceAll("id=",""));
                        tempComment.setComment(WB_text.text());
                        System.out.println(userInfo.text()+":"+WB_text.text());
                        if(WB_text.getElementsByTag("img").size()!=0){
                            for(Element imginCommentElement: WB_text.getElementsByTag("img")){
                                String expressionSrc = imginCommentElement.attr("src");
                                if(!expressionSrc.contains("http:"))
                                    expressionSrc = "http:" + expressionSrc;
                                commentImgList.add(ImageDownload.imageDownload(expressionSrc));
                            }
                        }
                        Element comment_media_disp = list_con.get(0).getElementsByClass("WB_expand_media_box").get(0);
                        if(comment_media_disp.getElementsByTag("img").size() != 0){
                            for(Element imginCommentElement: WB_text.getElementsByTag("img")){
                                String expressionSrc = imginCommentElement.attr("src");
                                if(!expressionSrc.contains("http:"))
                                    expressionSrc = "http:" + expressionSrc;
                                commentImgList.add(ImageDownload.imageDownload(expressionSrc));
                            }
                        }
                        tempComment.setImages(commentImgList);
                        comments.add(tempComment);
                    }
                    record.setHotComments(comments);




                    List<String> imageUrls = new ArrayList<>();
                    List<String[]> images = new ArrayList<>();
                    if (docNew.getElementsByClass("media_box").size() != 0) {
                        Elements imageElements = docNew.getElementsByClass("media_box").get(0).children().get(0).children();
                        for (Element element0 : imageElements) {
                            imageUrls.add(element0.children().select("img").attr("src"));
                            //System.out.println(element0.children().attr("src"));
                        }
                    }
                    Elements imagesInContent = docNew.getElementsByClass("WB_text W_f14").get(0).children().select("img");
                    for (Element element0 : imagesInContent) {
                        imageUrls.add(element0.attr("src"));
                        System.out.println(element.attr("src"));
                    }

                    for (String imageUrl : imageUrls) {
                        String temp[] = new String[2];
                        temp[0] = "无描述";
                        temp[1] = ImageDownload.imageDownload(imageUrl);
                        images.add(temp);
                    }
                    record.setImages(images);
                    record.setParticipateCount(-1);
                    record.setReadCount(-1);
                    return record;
                }
            }
            System.out.println("页面不存在！");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
