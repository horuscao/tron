package com.sjh.monitor_block;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjh.CommonUtil;
import com.sjh.WriteUtil;
import com.sjh.bean.*;
import com.sjh.sendusdt.SendThread;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 监控具体的钱包地址
 * <p>
 * 思路：有n个监控地址；
 * 1 循环查找，找出所有的交易；
 * 2 找出当前最新的交易，比当前时间10s 内发生的，然后转账；
 */
public class MonitorSpecificWalletThread extends Thread {


    private final LinkedList<BlockWalletBean> cap;//共享仓库

    public List<BlockWalletBean> mMyselfList; //自己的地址集合

//    public Map<String , MyTransferRecordBean> myTransferRecordMap = new HashMap<>();


    public MonitorSpecificWalletThread(String name, LinkedList<BlockWalletBean> cap, List<BlockWalletBean> myselfList) {
        super(name);
        this.cap = cap;
        this.mMyselfList = myselfList;

//        for (int i = 0; i < mMyselfList.size(); i++) {
//            String key = mMyselfList.get(i).address;
//            MyTransferRecordBean myTransferRecordBean = new MyTransferRecordBean();
//            myTransferRecordBean.transferTimeStrap = "";
//            myTransferRecordBean.blockId = "";
//            myTransferRecordMap.put(key, myTransferRecordBean);
//        }
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
                    Thread.sleep(1000 * 30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("开始下一轮的请求");
                for (int i = 0; i < mMyselfList.size(); i++) {
                    // 1 查找监控账户的交易情况
                    BlockWalletBean blockWalletBean = mMyselfList.get(i);
                    String specificWalletTransferUrl = "https://api.trongrid.io/v1/accounts/" + blockWalletBean.monitorAddress + "/transactions/trc20";

                    Map<String, String> paraMap = new HashMap<>();
                    paraMap.put("limit", "30");
                    paraMap.put("only_confirmed", "true");
                    paraMap.put("contract_address", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
                    String content = httpGet(specificWalletTransferUrl, paraMap);
                    System.out.println("content:" + content);
                    if (!StringUtils.isEmpty(content)) {

                        //2 解析被监控账户的交易数据
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        JSONArray results = jsonObject.getJSONArray("data");
                        System.out.println("交易信息个数:" + results.size());

                        if (results.size() == 0) {
                            continue;
                        }

                        boolean isFindAim = false;
                        //for (int m = 0; m < results.size(); m++) {
                        TrongridTransactionBean trongridTransactionBean = new TrongridTransactionBean();
                        JSONObject transferJsonObject = results.getJSONObject(0);
                        trongridTransactionBean.transaction_id = transferJsonObject.getString("transaction_id");
                        trongridTransactionBean.from = transferJsonObject.getString("from");
                        trongridTransactionBean.to = transferJsonObject.getString("to");
                        trongridTransactionBean.block_timestamp = transferJsonObject.getString("block_timestamp");
                        trongridTransactionBean.value = transferJsonObject.getString("value");
                        trongridTransactionBean.type = transferJsonObject.getString("type");

                        System.out.println("最新的一笔交易情况:" + trongridTransactionBean.toString());
                        String recevierAddress = trongridTransactionBean.to;
                        if (!recevierAddress.equals(blockWalletBean.monitorAddress)) {
                            System.out.println("收款人必须是当前监控人");
                            continue;
                        }

                        if (!"Transfer".equals(trongridTransactionBean.type)) {
                            System.out.println("交易类型必须为转账");
                            continue;
                        }


                        long transferAmount = Long.parseLong(trongridTransactionBean.value);
                        if (transferAmount < 1000000) {
                            System.out.println("交易金额小于1u，不转账");
                            String filePath = SendThread.class.getResource("").getPath() + "log.txt";
                            System.out.println("filepath" + filePath);

                            continue;
                        }

                        long curTimestamp = System.currentTimeMillis();
                        long transferTimestrap = Long.parseLong(trongridTransactionBean.block_timestamp);

                        if (curTimestamp - transferTimestrap > 1000 * 60 * 30 ) {
                            System.out.println("交易时间太久远了");
                            continue;
                        }

//                            MyTransferRecordBean myTransferRecordBean = myTransferRecordMap.get(blockWalletBean.address);
//                            if (myTransferRecordBean.blockId.equals(trongridTransactionBean.transaction_id)){
//                                System.out.println("交易已经转过帐了 不在转账了");
//                                continue;
//                            }

                        // 3 查询目标的账户信息，看账户中usdt 是否大于500；目标账户是给监控账户转账的账户
                        String findAccountInfoUrl = "https://apilist.tronscan.org/api/account";
                        Map<String, String> accountParaMap = new HashMap<>();
                        accountParaMap.put("address", trongridTransactionBean.from);


                        String accountInfoResp = httpGet(findAccountInfoUrl, accountParaMap);
                        System.out.println("accountInfoResp:" + accountInfoResp);
                        JSONObject accountInfoObject = JSONObject.parseObject(accountInfoResp);
                        JSONArray walletInfos = accountInfoObject.getJSONArray("trc20token_balances");

                        if (walletInfos != null && walletInfos.size() >= 1) {
                            for (int k = 0; k < walletInfos.size(); k++) {
                                WalletUsdtBalanceBean walletUsdtBalanceBean = walletInfos.getObject(k, WalletUsdtBalanceBean.class);
                                String tokenId = walletUsdtBalanceBean.tokenId;
                                //这里有的人账户可能会有多个余额
                                if (tokenId.equals("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")) {
                                    double balance = Double.parseDouble(walletUsdtBalanceBean.balance);
                                    // 4 判断目标账户是否符合要求
                                    if ((int) balance > 20 * 1000000) {
                                        System.out.println("找到符合要求的账户了，账户余额为" + (balance / 1000000));
                                        blockWalletBean.aimAddress = trongridTransactionBean.from;

                                        //暂存上一次交易的对象 用于去重
//                                            if (myTransferRecordMap.get(blockWalletBean.address).blockId.equals(trongridTransactionBean.transaction_id)){
//                                                System.out.println("该交易已经发生了，不在转账");
//                                                continue;
//                                            }
//                                            myTransferRecordBeanTemp.transferTimeStrap = trongridTransactionBean.block_timestamp;
//                                            myTransferRecordBeanTemp.blockId = trongridTransactionBean.transaction_id;

                                        cap.add(blockWalletBean);

                                        //暂存上一次交易的对象 用于去重
//                                            MyTransferRecordBean myTransferRecordBeanTemp = myTransferRecordMap.get(blockWalletBean.address);
//                                            myTransferRecordBeanTemp.transferTimeStrap = trongridTransactionBean.block_timestamp;
//                                            myTransferRecordBeanTemp.blockId = trongridTransactionBean.transaction_id;

//                                            isFindAim = true;

                                    }
                                }

                            }
                        }





//                            if (isFindAim){
//                                break;
//                            }
//                        }
                    }
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


}
