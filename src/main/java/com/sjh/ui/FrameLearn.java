package com.sjh.ui;


import java.awt.*;

public class FrameLearn {

    public static void main(String[] args) {
        Frame frame = new Frame("Frame 学习");

        //设置可见性 不设置看不到
        frame.setVisible(true);

        //设置窗口大小
        frame.setSize(500, 500);

        //设置窗口 背景色

        frame.setBackground(new Color(214, 194, 194));

        //设置位置 位置坐标参考android 屏幕，坐标原点在左上角
        frame.setLocation(400, 500);

        //设置窗口是否可拉动
        frame.setResizable(false);
    }
}
