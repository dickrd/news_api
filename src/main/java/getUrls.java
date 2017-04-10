import message.TaskAssignment;
import util.GetUrlsFromRedis;
import webpages.WeiboPage;


/**
 * Created by first1hand on 2017/4/5.
 */
public class getUrls {
    public static void main(String[] args){

        GetUrlsFromRedis getUrlsFromRedis = new GetUrlsFromRedis();
        String keys[] = {};
        WeiboPage weiboPagetest = new WeiboPage("1","http://weibo.com/6085695259/ED4G2d0mz?refer_flag=1001030103&type=comment#_rnd1491725269721" );
        // WeiboPage weiboPage = new WeiboPage(taskAssignment.getId(),"http://weibo.com/5797152628/ED5Lczbj8?refer_flag=1001030103_" );
        weiboPagetest.dealWeiboPage();
        for (String key:keys) {
            getUrlsFromRedis.getTaskResponse(key);
            TaskAssignment taskAssignment = getUrlsFromRedis.getTaskAssignment();
            for(String url:taskAssignment.getUrls()){
                WeiboPage weiboPage = new WeiboPage("1","http://weibo.com/5797152628/ED5Lczbj8?refer_flag=1001030103_" );
                // WeiboPage weiboPage = new WeiboPage(taskAssignment.getId(),"http://weibo.com/5797152628/ED5Lczbj8?refer_flag=1001030103_" );
                weiboPage.dealWeiboPage();
                switch (key){

                }
            }
        }

    }
}
