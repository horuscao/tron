package com.sjh.monitor_block;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjh.CommonUtil;
import com.sjh.bean.BlockWalletBean;
import com.sjh.bean.TransferBean;
import com.sjh.bean.WalletUsdtBalanceBean;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MonitorThread extends Thread {


    private final LinkedList<BlockWalletBean> cap;//共享仓库

    public List<BlockWalletBean> mMyselfList; //自己的地址集合


    public MonitorThread(String name, LinkedList<BlockWalletBean> cap, List<BlockWalletBean> myselfList) {
        super(name);
        this.cap = cap;
        this.mMyselfList = myselfList;
    }

    @Override
    public void run() {

        while (true) {
            synchronized (cap) {
                if (cap.size() >= 1) {//缓冲区满 生产者进行阻塞
                    try {
                        cap.wait();
                        //通知消费者消费产品
                        cap.notify();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //region 监控核心代码
                //查找数据
                String url = "https://apilist.tronscan.org/api/transfer";
                Map<String, String> paraMap = new HashMap<>();
                paraMap.put("sort", "-timestamp");
                paraMap.put("count", "0");
                paraMap.put("limit", "100");
//                paraMap.put("contract", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
                paraMap.put("start", "0");
                long curTimestamp = System.currentTimeMillis();

                String end_timestamp = String.valueOf(curTimestamp);
                String start_timestamp = String.valueOf(curTimestamp - 1000 * 3);
                paraMap.put("start_timestamp", start_timestamp);
                paraMap.put("end_timestamp", end_timestamp);


                try {
                    String content = httpGet(url, paraMap);
                    System.out.println("content:" + content);
                    if (!StringUtils.isEmpty(content)) {
                        //region
                        //解析数据
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        JSONArray results = jsonObject.getJSONArray("data");
                        System.out.println("交易信息个数:" + results.size());

                        for (int i = 0; i < results.size(); i++) {
                            TransferBean transferBean = new TransferBean();
                            JSONObject transferJsonObject = results.getJSONObject(i);
                            transferBean.transferFromAddress = transferJsonObject.getString("transferFromAddress");
                            transferBean.transferToAddress = transferJsonObject.getString("transferToAddress");

                            //查询到的地址和自己的地址去比较
                            for (int j = 0; j < mMyselfList.size(); j++) {
                                BlockWalletBean walletBean = mMyselfList.get(j);
                                if (walletBean.last5Num.equals(transferBean.transferToAddress.substring(transferBean.transferToAddress.length() - 5))) {
                                    System.out.println("找到和自己地址一样的地址了" + transferBean.transferToAddress + "，我的地址：" + walletBean.simpleName );

                                    //region
                                    // 查询账户信息，看账户中usdt 是否大于500
                                    String findAccountInfoUrl = "https://apilist.tronscan.org/api/account";
                                    Map<String, String> accountParaMap = new HashMap<>();
                                    accountParaMap.put("address", transferBean.transferFromAddress);


                                    String accountInfoResp = httpGet(findAccountInfoUrl, accountParaMap);
                                    System.out.println("accountInfoResp:" + accountInfoResp);
                                    JSONObject accountInfoObject = JSONObject.parseObject(accountInfoResp);
                                    JSONArray walletInfos = accountInfoObject.getJSONArray("trc20token_balances");

                                    if (walletInfos != null && walletInfos.size() >= 1) {
                                        for (int k = 0; k < walletInfos.size(); k++) {
                                            WalletUsdtBalanceBean walletUsdtBalanceBean = walletInfos.getObject(k, WalletUsdtBalanceBean.class);
                                            long balance = Long.parseLong(walletUsdtBalanceBean.balance);
                                            if (balance > 20 * 1000000) {
                                                System.out.println("找到符合要求的账户了，账户余额为" + (balance / 1000000));
                                                walletBean.aimAddress = transferBean.transferFromAddress;
                                                cap.add(walletBean);
                                            }
                                        }
                                    }
                                    //endregion

                                } else {
//                                    System.out.println("地址不匹配");
                                }

//                                if (i == 40){
//                                    System.out.println("假设找到了");
//                                    walletBean.aimAddress = "TSrC6orpPbfFXdRH9nRPLQ8T6XaLxmV87W";
//                                    cap.add(walletBean);
//                                }
                            }
                        }
                    }
                    //endregion
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public static String httpGet(String url, Map<String, String> map) {
        HttpURLConnection conn;
        BufferedReader in = null;
        StringBuilder resultStr = new StringBuilder();

        StringBuilder urlStr = new StringBuilder(url);
        urlStr.append("?");
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                urlStr.append(entry.getKey()).append("=");
                urlStr.append(entry.getValue());
                urlStr.append("&");
            }
            urlStr.deleteCharAt(urlStr.length() - 1);
        }

        System.out.println(CommonUtil.getNowTime() + "urlStr: " + urlStr);

        try {
            URL realUrl = new URL(urlStr.toString());
            conn = (HttpURLConnection) realUrl.openConnection();

            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            // 开始连接网络 进行请求
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

            if (conn.getResponseCode() == 200) {
                System.out.println("请求成功");
                String line;
                while ((line = in.readLine()) != null) {
                    resultStr.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultStr.toString();

    }

    public static void main(String[] args) {

        long time1 = System.currentTimeMillis();
        System.out.println(time1);
        String a = "1234567890";
        System.out.println(a.substring(a.length() - 5));

    }

}
