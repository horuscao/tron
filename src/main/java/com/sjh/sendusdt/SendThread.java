package com.sjh.sendusdt;

import com.sjh.CommonUtil;
import com.sjh.Trc20Handler;
import com.sjh.WriteUtil;
import com.sjh.bean.BlockWalletBean;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

public class SendThread extends Thread{


    private final LinkedList<BlockWalletBean> cap;//共享仓库

    public SendThread(String name, LinkedList<BlockWalletBean> cap) {
        super(name);
        this.cap = cap;
    }

    @Override
    public void run() {

        while (true) {
            synchronized (cap){
                if (cap.size() == 0) {//缓冲区满 生产者进行阻塞
                    try {
                        cap.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //region
                //发送u到指定账号
                BlockWalletBean blockWalletBean = cap.removeFirst();

                if (!StringUtils.isEmpty(blockWalletBean.aimAddress)){

                    Trc20Handler trc20Handler = new Trc20Handler();
                    String fromAddress_private_key = blockWalletBean.privateKey;
                    String toAddress = blockWalletBean.aimAddress;
                    BigDecimal amount = new BigDecimal(0.001);
                    System.out.println(CommonUtil.getNowTime() + "BlockWalletBean：" + blockWalletBean.toString());

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        //trc20Handler.sendTrc20(toAddress, amount, fromAddress_private_key);
                        String filePath = SendThread.class.getResource("").getPath() + "log.txt";

                        WriteUtil.write(CommonUtil.getNowTime() + blockWalletBean.toString(), filePath);

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }else {
                    System.out.println("BlockWalletBean 的目标地址为空");
                }
                System.out.println("发送任务完成");
                //通知生产者生产
                cap.notify();

                //endregion
            }
        }

    }
}
