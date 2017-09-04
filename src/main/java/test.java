import content.Record;
import sun.nio.ch.Net;
import util.CommentInfo;
import util.ImageDownload;
import webpages.NeteaseNews;
import webpages.QQNews;
import webpages.WeiboPage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.File;

/**
 * Created by first1hand on 2017/4/11.
 */
public class test {
    public static void main(String args[]) throws IOException {
        NeteaseNews neteaseNews = new NeteaseNews("1","http://news.163.com/17/0604/16/CM3P7J9R000187VE.html");
        //        WeiboPage neteaseNews = new WeiboPage("1","http://weibo.com/3317985444/EE7YIyoxa?refer_flag\\u003d1001030103_");
        //http://weibo.com/2211979435/EE7X8c2Oz?refer_flag\u003d1001030103_
        Record record = neteaseNews.dealNeteasenews();
       // System.out.println(record.getImages().get(0)[0]+":"+record.getImages().get(0)[1]);
       // ImageDownload.imageDownload("http://img.t.sinajs.cn/t4/appstyle/expression/ext/normal/09/pcmoren_tanshou_org.png");
//        Set<String> set = new HashSet<>();
//        set.add("123");
//        set.add("123");
//        System.out.println(set.contains("123"));
        List<CommentInfo> comments = record.getNewComments();
        File file = new File("comments.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(CommentInfo comment:comments){
            writer.write(comment.getComment()+"\n");
        }
        writer.close();
    }
}
