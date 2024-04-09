package com.sjh.test_func;

import com.sjh.CommonUtil;

import java.util.LinkedList;

public class Consumer extends Thread{
    private LinkedList<Integer> cap;

    public Consumer(LinkedList<Integer> cap) {
        this.cap = cap;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (cap) {
                if (cap.size() == 0) { //如果缓冲区为0，消费者阻塞
                    try {
                        cap.wait();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //消费者消费产品
                Integer i = cap.remove();
                System.out.println(CommonUtil.getNowTime() + "消费者消费了" + i);


                //通知生产者生产

                //这里必须放到最后，不能放到wait的判断条件 下面；虽然这里每次都会notify 生产者，但是此时锁被消费者占用者，所以即使
                //唤醒也没有用；直到wait方法调用，进入等待状态，并释放锁；
                cap.notify();
            }
        }
    }


}
