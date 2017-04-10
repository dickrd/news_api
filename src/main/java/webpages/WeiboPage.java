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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
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

    public boolean dealWeiboPage(){
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
            for (Element element:elements
                 ) {
                tempData = element.data();
                if(tempData.contains("pl.content.weiboDetail.index"))
                    break;
            }
            String contentInHtml = tempData;
            String contentHtmlSplit[] = new String[0];
            if (contentInHtml != null) {
                contentHtmlSplit = contentInHtml.split("\\{");
            }
            String contentJson = String.format("{%s}", contentHtmlSplit[1].split("}")[0]);
            JsonObject json = new JsonParser().parse(contentJson).getAsJsonObject();
            String html;
            if (json.has("html")){
                html = json.get("html").getAsString();
            }else
                return false;
            Document docNew = Jsoup.parse(html);
            String content = docNew.getElementsByClass("WB_text W_f14").text();
            record.setContent(content);

            String commentAttr = docNew.getElementsByAttributeValue("action-type","fl_comment").get(0).text();
            String commentCount = commentAttr.split(String.valueOf(commentAttr.charAt(0)))[1];
            if(commentCount.equals("评论"))
                record.setCommentCount(0);
            else
                record.setCommentCount(Integer.parseInt(commentCount));

            List<String> imageUrls = new ArrayList<>();
            List<String> images = new ArrayList<>();
            Elements imageElements = docNew.getElementsByClass("media_box").get(0).children().get(0).children();
            //System.out.println(imageElements.get(0).children());
            for(Element element:imageElements){
                imageUrls.add(element.children().select("img").attr("src"));
                System.out.println(element.children().attr("src"));
            }
            Elements imagesInContent = docNew.getElementsByClass("WB_text W_f14").get(0).children().select("img");
            for (Element element:imagesInContent){
                imageUrls.add(element.attr("src"));
                System.out.println(element.attr("src"));
            }

            for(String imageUrl:imageUrls){
                URL image = new URL(imageUrl);
                URLConnection urlConnection = image.openConnection();
                InputStream is = urlConnection.getInputStream();
                byte[] bytes = new byte[is.available()];
                if(is.read(bytes)!=0)
                    System.out.println("下载图片成功！");
                else
                    System.out.println("下载图片失败！");
                is.close();
                String imageStr = byte2hex(bytes);
                //System.out.println(imageStr);
                images.add(imageStr);
            }
            record.setImages(images);
            record.setParticipateCount(-1);
            record.setReadCount(-1);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String byte2hex(byte[] b) // 二进制转字符串
    {
        StringBuilder sb = new StringBuilder();
        String stmp = "";
        for (byte aB : b) {
            stmp = Integer.toHexString(aB & 0XFF);
            if (stmp.length() == 1) {
                sb.append("0").append(stmp);
            } else {
                sb.append(stmp);
            }

        }
        return sb.toString();
    }

    public static byte[] hex2byte(String str) { // 字符串转二进制
        if (str == null)
            return null;
        str = str.trim();
        int len = str.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }
}
