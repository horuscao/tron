package com.sjh.bean;

public class BlockWalletBean {
    //我的地址情况
    public String simpleName;
    //地址
    public String address;
    //地址后5位
    public String last5Num;
    //私钥
    public String privateKey;
    //endregion

    //上一次的转账时间 避免重复转
    public long lastTransferTime;

    //监控的地址
    public String monitorAddress;

    //目标地址 即和监控地址发生交易的地址 这个地址就是要转账的第哈
    public String aimAddress;

    public static BlockWalletBean getInstance(){
        BlockWalletBean blockWalletBean = new BlockWalletBean();
        //目标地址
        blockWalletBean.aimAddress = "TEKEELcpqhPracwUbupfF5FBDRwxSRN5yH";
        //私钥 转账地址的私钥
        blockWalletBean.privateKey = "bb9d1838beaf893---自己的地址的私钥---cf84da347330cf4bf42";
        return blockWalletBean;
    }

    @Override
    public String toString() {
        return "BlockWalletBean{" +
                "simpleName='" + simpleName + '\'' +
                ", address='" + address + '\'' +
                ", last5Num='" + last5Num + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", lastTransferTime=" + lastTransferTime +
                ", monitorAddress='" + monitorAddress + '\'' +
                ", aimAddress='" + aimAddress + '\'' +
                '}';
    }
}
