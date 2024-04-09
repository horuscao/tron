package com.sjh;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteUtil {

    static Logger logger = LoggerFactory.getLogger(WriteUtil.class);

    public static void log(String msg, boolean writeLocal) {
        System.out.println(msg);

        String os = System.getProperty("os.name");
        String filePath = "";
        if (os.toLowerCase().startsWith("win")) {
            filePath = "C:\\log\\monitor_run.log";
        } else if (os.toLowerCase().startsWith("mac")) {
            filePath = "/Users/shijianhua/monitor/log.log";
        } else {
            filePath = "/var/log/monitor/log.log";
        }
        if (writeLocal) {
            WriteUtil.write(CommonUtil.getNowTime() + msg + "\n", filePath);
        } else {
            System.out.println(CommonUtil.getNowTime() + msg);
        }

    }

    public static void log(String msg, String filePath, boolean writeLocal) {
        if (writeLocal) {
            System.out.println(msg);
            WriteUtil.write(CommonUtil.getNowTime() + msg + "\n", filePath);
        } else {
            System.out.println(CommonUtil.getNowTime() + msg);
        }

    }

    public static void log(String msg, String filePath) {
        System.out.println(msg);
        if (!StringUtils.isEmpty(filePath)){
            WriteUtil.write(CommonUtil.getNowTime() + " " + msg + "\n", filePath);
        }
    }


    public static void log(String msg) {
        log(msg, true);
    }


    public static void write(String content, String filePath ){
        FileOutputStream fos = null;
        try {
            File file = new File(filePath);
            if (!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(filePath,true);
            fos.write(content.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        WriteUtil writeUtil = new WriteUtil();
        //这种方式获取的目录在 生成的target 文件下 而不是java文件目录下
        String path = writeUtil.getClass().getResource("").getPath() + "test11.txt";
        System.out.println(path);
        write("testtes",path);

    }



}
