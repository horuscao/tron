package com.sjh.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 不能单独存在，需要和Frame配合使用；
 * 可以类比android 中的View 布局中的顶层布局；
 * Frame 是一个最外层view ，Panel 是里面的一个个空间区域，然后把panel装进Frame ；
 *
 */
public class PanelLearn {

    public static void main(String[] args) {
        Frame frame = new Frame();

        Panel panel = new Panel();

        frame.setLayout(null);

        frame.setBounds(400, 400 , 600 ,600);
        frame.setBackground(Color.darkGray);

        panel.setBounds(450, 450 , 500 ,500);
        panel.setBackground(Color.RED);

        frame.add(panel);

        frame.setVisible(true);


        //监听事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        while (true){
            System.out.println("11");
        }
    }
}
