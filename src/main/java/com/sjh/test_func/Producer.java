package com.sjh.test_func;

import com.sjh.CommonUtil;

import java.util.LinkedList;
import java.util.Random;

public class Producer extends Thread{
    private LinkedList<Integer> cap;//共享仓库
    private Random random = new Random();

    public Producer(LinkedList<Integer> cap) {
        this.cap = cap;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (cap){
                if (cap.size() == 3) {//缓冲区满 生产者进行阻塞
                    try {
                        cap.wait();

                        //通知消费者消费产品
                        cap.notify();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //生产产品
                int i = random.nextInt(1000);
                System.out.println(CommonUtil.getNowTime() + "生产者生产了" + i);
                cap.add(i);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }
}
