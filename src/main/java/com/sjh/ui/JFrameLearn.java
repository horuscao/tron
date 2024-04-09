package com.sjh.ui;

import javax.swing.*;
import java.awt.*;

/**
 * jframe 和 frame 不一样；
 * jframe 需要用Container包裹
 *
 */
public class JFrameLearn {

    public void init(){
        JFrame jFrame = new JFrame("JFrame");
        jFrame.setVisible(true);
        jFrame.setBounds(100 ,100 , 300 ,300);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel label = new JLabel("jlable");
//        jFrame.add(label);

        Container container = jFrame.getContentPane();
        container.add(label);


    }

    public static void main(String[] args) {
        new JFrameLearn().init();
    }
}
