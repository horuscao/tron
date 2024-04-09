package com.sjh.bean;

import com.google.gson.Gson;

/**
 * 自己的钱包转账记录;
 * 用于判断此次转账是不是转过了；
 */
public class MyTransferRecordBean {

    public String address;

    public String lastTransferTimeStrap;

    public static void main(String[] args) {
        MyTransferRecordBean bean = new MyTransferRecordBean();
        bean.address ="sds";
        bean.lastTransferTimeStrap = System.currentTimeMillis() +"";
        System.out.println(new Gson().toJson(bean));

    }

}
