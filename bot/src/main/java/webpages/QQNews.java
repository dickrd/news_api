package main.java.webpages;

import com.google.gson.*;
import main.java.content.Record;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import main.java.util.ImageDownload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by first1hand on 2017/4/10.
 */
public class QQNews {
    private String url;
    private String taskId;
    private Map<String,String> headers = new HashMap<String,String>(){{
        put("Host","news.qq.com");
        put("User-Agent","Mozilla/5.0 (X11; FreeBSD amd64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36");
        put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        put("Accept-Encoding","gzip,deflate,sdch");
        put("Cookie","pgv_pvid=2421808768; pac_uid=1_395980845; pt2gguin=o0395980845; ptcz=ac21bcb8313afbab85ef766760d9ae90b8cff1689b237d9ee6ab7b9594a4d2f8; o_cookie=395980845; ptui_loginuin=395980845; pgv_info=ssid=s2283165184; pgv_pvi=924861440; pgv_si=s3100392448; ad_play_index=10; ptag=www_baidu_com|/; ts_refer=www.baidu.com/link; ts_uid=5835124813; gj_mpvid=80112605; ts_last=news.qq.com/a/20170411/005581.htm; dsp_cookiemapping0=1491902012311; dsp_cookiemapping1=1491902020620" );
    }};

    public QQNews(String taskId,String url){
        this.url = url;
        this.taskId = taskId;
    }

    public Record dealQQNews(){
        try {
            Document doc = Jsoup.connect(url).headers(headers)
                            .timeout(6000)
                            .get();

            Elements contentElements ;

            List<String[]> images = new ArrayList<>();
            String tempImage[] = new String[2];
            Record record = new Record();
            record.setUrl(url);
            record.setTaskId(taskId);
            record.setReadCount(-1);
            record.setParticipateCount(-1);

            if(doc.getElementsByClass("Cnt-Main-Article-QQ").size()!=0){
                contentElements = doc.getElementsByClass("Cnt-Main-Article-QQ").get(0).children();
                String content = "";
                for(int i=0;i<=contentElements.size()-1;i++){
                    if(contentElements.get(i).tagName().equals("p")&&contentElements.get(i).children().size()==0){
                        content += contentElements.get(i).text();
                    }else if(contentElements.get(i).tagName().equals("p")&&contentElements.get(i).children().size()!=0){
                        String imageUrl =  contentElements.get(i).getElementsByTag("img").attr("src");
                        if (!imageUrl.equals("")) {
                            tempImage[1] = ImageDownload.imageDownload(imageUrl);
                            if(contentElements.get(i+1).className().equals("text image_desc")){
                                tempImage[0] = contentElements.get(i+1).text();

                            }else {
                                tempImage[0] = "无描述";
                            }
                           //System.out.println(tempImage[0]+":"+tempImage[1]);
                            images.add(tempImage);
                        }

                    }
                }

                record.setImages(images);
                //System.out.println(content);
                record.setContent(content);

                String cmtId = "";
                Elements scripts = doc.getElementsByTag("script");
                //System.out.println(scripts.size());
                for(Element element:scripts){
                    //System.out.println(element.toString());
                    if(element.toString().contains("cmt_id")) {
                       // System.out.println(element.toString());
                        cmtId = element.toString().split("cmt_id = ")[1].split(";")[0];
                        //System.out.println(cmtId);
                        break;
                    }
                }
                String commentUrl = "http://coral.qq.com/article/"+cmtId+"/commentnum";
                Document cmtDoc = Jsoup.connect(commentUrl)
                                .timeout(6000)
                                .ignoreContentType(true)
                                .get();
                String cmtInfoStr = cmtDoc.getElementsByTag("body").text();
                System.out.println(cmtInfoStr);
                JsonObject cmtInfoJson = new JsonParser().parse(cmtInfoStr).getAsJsonObject();
                int commentCount = cmtInfoJson.get("data").getAsJsonObject().get("commentnum").getAsInt();
                record.setCommentCount(commentCount);

            }
            if(doc.getElementById("picWrap").children().size()!=0){

                String urlSplit[] = url.split("a/");
                String newsId = urlSplit[urlSplit.length - 1].split("\\.")[0];
                //System.out.println(newsId);
                Document docPic = Jsoup.connect("http://news.qq.com/a/" + newsId + ".hdBigPic.js").headers(headers)
                        .ignoreContentType(true)
                        .timeout(6000)
                        .get();
                String imgInfoStr = docPic.getElementsByTag("body").text().split("/\\*\\s\\|")[0];
                //System.out.println( imgInfoStr.replace('\'','\"'));
                JsonObject imggroup = null;
                JsonObject imgInfo = new JsonParser().parse(imgInfoStr.replace('\'','\"')).getAsJsonObject();
                JsonArray rootChildren = imgInfo.get("Children").getAsJsonArray();
                for(JsonElement groupimginfo:rootChildren){
                    if(groupimginfo.getAsJsonObject().get("Name").getAsString().equals("groupimginfo")){
                        JsonArray groupimginfoChilren = groupimginfo.getAsJsonObject().get("Children").getAsJsonArray();
                        for (JsonElement jsonElement:groupimginfoChilren){
                            imggroup = jsonElement.getAsJsonObject();
                            if(imggroup.get("Name").getAsString().equals("groupimg")){
                                break;
                            }
                        }
                    }
                }
                if(imggroup != null){
                    JsonArray imgs = imggroup.get("Children").getAsJsonArray();
                    for(JsonElement img:imgs){
                        JsonObject imgJson = img.getAsJsonObject();
                        if(imgJson.get("Name").getAsString().equals("img")){
                            JsonArray singleImgInfo = imgJson.get("Children").getAsJsonArray();
                            for(JsonElement imageJE:singleImgInfo){
                                JsonObject imageJo = imageJE.getAsJsonObject();
                                if(imageJo.get("Name").getAsString().equals("cnt_article")){
                                    for(JsonElement childWithImgUrlJe:imageJo.get("Children").getAsJsonArray()) {
                                        JsonObject childWithImgUrlJo = childWithImgUrlJe.getAsJsonObject();
                                        String imgDesc = childWithImgUrlJo.get("Content").getAsString();
                                        tempImage[0] = imgDesc;
                                        //System.out.println(url);
                                    }
                                }
                                if(imageJo.get("Name").getAsString().equals("bigimgurl")){
                                    //System.out.println(imageJo.toString());
                                    for(JsonElement childWithImgUrlJe:imageJo.get("Children").getAsJsonArray()){
                                        JsonObject childWithImgUrlJo = childWithImgUrlJe.getAsJsonObject();
                                        String url = childWithImgUrlJo.get("Content").getAsString();
                                        tempImage[1] = ImageDownload.imageDownload(url);
                                       //System.out.println(url);
                                    }
                                }
                                if(tempImage[0]!=null&&tempImage[1]!=null) {
                                    //System.out.println(tempImage[0] + ":" + tempImage[1]);
                                    images.add(tempImage);
                                    tempImage[0] = null;
                                    tempImage[1] = null;
                                }
                            }
                        }
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
