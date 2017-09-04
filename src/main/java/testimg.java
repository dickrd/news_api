import util.ImageDownload;

import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by first1hand on 2017/5/6.
 */
public class testimg {
    static int i;
    public static void main(String args[]) throws IOException {
        Integer[] ints = new Integer[new testimg().getInt()];
        System.out.println(++i);
    }
    public int getInt(){
        return 100;
    }
}

class Tank{
     private boolean isEmpty = false;
     Tank(boolean isEmpty){
         this.isEmpty = isEmpty;
     }
     void empty(){
         isEmpty = true;
     }
     protected void finalize(){
         if(!isEmpty)
             System.out.println("error");

     }
}