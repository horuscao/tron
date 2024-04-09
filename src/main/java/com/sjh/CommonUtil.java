package com.sjh;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String getNowTime(){
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return  dateFormat.format(date);
    }

    public static String geTimeBySpec(long time){
        Date date = new Date(time);
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return  dateFormat.format(date);
    }


    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
        if (args.length > 0){
            if ("test".equals(args[0])){
                System.out.println("test");
            }
        }
    }
}
