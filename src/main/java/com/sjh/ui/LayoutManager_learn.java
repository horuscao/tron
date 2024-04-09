package com.sjh.ui;

import java.awt.*;

/**
 * 1 默认布局为流式布局
 * 2 有东西南北布局、表格布局
 * 3 复杂的布局由上面三个布局组成；
 * 类似anddroid 里面的相对布局、线性布局，由这两个布局组成各种各样的UI
 *
 */
public class LayoutManager_learn {

    public static void main(String[] args) {

        Frame frame = new Frame();

        Button button = new Button("开始");
        Button button1 = new Button("开始1");
        Button button2 = new Button("开始2");

        frame.setLayout(new FlowLayout(FlowLayout.LEFT));

        frame.setSize(400, 400);
        frame.add(button);
        frame.add(button1);
        frame.add(button2);

        frame.setVisible(true);

    }
}
