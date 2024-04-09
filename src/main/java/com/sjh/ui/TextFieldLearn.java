package com.sjh.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextFieldLearn {

    public static void main(String[] args) {
        MyFrame myFrame = new MyFrame();
    }
}


class MyFrame extends Frame {

    public MyFrame(){

        TextField textField = new TextField();
        add(textField);

        setVisible(true);
        setSize(400, 400);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //获取刚才的监控对象
                TextField textField1 = (TextField) e.getSource();
                //拿到输入的值
                System.out.println(textField1.getText());
            }
        });
    }
}