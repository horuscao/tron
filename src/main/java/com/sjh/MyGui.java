package com.sjh;

import com.sjh.bean.BlockWalletBean;
import com.sjh.monitor_block.MonitorThread;
import com.sjh.sendusdt.SendThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * UI 界面；监控地址
 */
public class MyGui extends JFrame {


    boolean isRunning;

    public MyGui(String title) {
        super(title);
        setBounds(500, 500, 1000, 1000);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        Container container = getContentPane();
        container.setLayout(new GridLayout(3, 1));

        //设置顶层两个按钮， 启动、停止
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 2));
        JButton startBtn = new JButton("开始");
        JButton stopBtn = new JButton("停止");
        topPanel.add(startBtn);
        topPanel.add(stopBtn);

        JPanel middlePanel = new JPanel();
        middlePanel.add(new JButton("中间button"));

        JPanel bottomPanel = new JPanel();
        JTextArea jTextArea = new JTextArea();
        jTextArea.setText("显示日志");
        bottomPanel.add(jTextArea);

        container.add(topPanel);
        container.add(middlePanel);
        container.add(bottomPanel);

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!isRunning){
                    startBtn.setText("程序已经运行");
                    startApp();
                }

                isRunning = true;

            }
        });

        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = false;
                startBtn.setBackground(Color.GRAY);
            }
        });


    }


    public void startApp(){

        List<BlockWalletBean> myselfAddress = createMySelfAddress();

        LinkedList<BlockWalletBean> capList = new LinkedList<>();

        MonitorThread monitorThread = new MonitorThread("监控线程", capList, myselfAddress);
        SendThread sendThread  = new SendThread("发送线程", capList);

        monitorThread.start();
        sendThread.start();
    }

    public List<BlockWalletBean> createMySelfAddress(){
        BlockWalletBean blockWalletBean1 =new BlockWalletBean();
        BlockWalletBean blockWalletBean2 =new BlockWalletBean();
        BlockWalletBean blockWalletBean3 =new BlockWalletBean();


        List<BlockWalletBean> myselfAddress = new ArrayList<>();

        myselfAddress.add(blockWalletBean1);
        myselfAddress.add(blockWalletBean2);
        myselfAddress.add(blockWalletBean3);

        return myselfAddress;
    }


    public static void main(String[] args) {
        new MyGui("大生意");
    }
}
