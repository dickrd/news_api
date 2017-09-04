package main.java.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by first1hand on 2017/4/10.
 */
public class ImageDownload {

    public static String imageDownload(String imageUrl){
        try {
            byte[] bytes;
            while(true) {
                URL image = new URL(imageUrl);
                URLConnection urlConnection = image.openConnection();
                InputStream is = urlConnection.getInputStream();
                bytes = readInputStream(is);
                if (bytes.length!=0) {
                    System.out.println(imageUrl+":"+"下载图片成功！");
                    is.close();
                    break;
                } else {
                    System.out.println(imageUrl+":"+"下载图片失败！");
                    is.close();
                }
            }
           //System.out.println(byte2hex(bytes));
            return byte2hex(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
    private static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while( (len=inStream.read(buffer)) != -1 ){
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }
}
