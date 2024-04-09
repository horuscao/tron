package com.sjh;

import com.sjh.bean.BlockWalletBean;
import com.sjh.monitor_block.MonitorSpecificWalletThread;
import com.sjh.monitor_block.MonitorThread;
import com.sjh.sendusdt.SendThread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RunMonitorAndSendUsdt {


    public static void main(String[] args) {

        // TODO: 2022/10/18 gui 程序
        RunMonitorAndSendUsdt monitorAndSendUsdt = new RunMonitorAndSendUsdt();
        List<BlockWalletBean> myselfAddress = monitorAndSendUsdt.createMySelfAddress();

        LinkedList<BlockWalletBean> capList = new LinkedList<>();

//        MonitorThread monitorThread = new MonitorThread("监控线程", capList, myselfAddress);
        MonitorSpecificWalletThread monitorSpecificWalletThread = new MonitorSpecificWalletThread("监控线程", capList, myselfAddress);
        SendThread sendThread  = new SendThread("发送线程", capList);

//        monitorThread.start();
        monitorSpecificWalletThread.start();
        sendThread.start();


    }


    /**
     * 36277016 笔交易
     *
     * T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb

   06b4135e3306b08
     *      * privateKey:e10674dada725443d503eae6fa109f7ad4610f540507
     * @return
     */
    public List<BlockWalletBean> createMySelfAddress(){
        BlockWalletBean blockWalletBean1 =new BlockWalletBean();
        BlockWalletBean blockWalletBean2 =new BlockWalletBean();
        BlockWalletBean blockWalletBean3 =new BlockWalletBean();



        blockWalletBean1.address = "TWUjH9VhE2ZehW1vqdGcK7xuWwb";
        blockWalletBean1.privateKey = "ce2f7vvvvvb595---------baf4efgbb0c24a0dc498b5e50de";
        blockWalletBean1.monitorAddress ="T9yD1334Nj9j7xAB4dbGeiX9h8unkKHxuWwb";
        System.out.println("blockWalletBean1:" + blockWalletBean1.toString());



        blockWalletBean3.address = "";
        blockWalletBean3.privateKey = "e10eee748da7nnnnn0fffffeaennnnnnad4610kk40507";
        blockWalletBean3.monitorAddress ="TFS33YS1HzZF931Ey86rESHDSvvJB4NeQ";
        System.out.println("blockWalletBean3:" + blockWalletBean3.toString());

        List<BlockWalletBean> myselfAddress = new ArrayList<>();

        myselfAddress.add(blockWalletBean1);
//        myselfAddress.add(blockWalletBean2);
        myselfAddress.add(blockWalletBean3);

        return myselfAddress;
    }



}
