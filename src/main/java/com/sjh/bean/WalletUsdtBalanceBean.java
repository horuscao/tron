package com.sjh.bean;

/**
 * 给别人转账前 查询对方账户余额
 */
public class WalletUsdtBalanceBean {

    /**
     * "trc20token_balances": [
     *         {
     *             "tokenId": "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
     *             "balance": "1999000",
     *             "tokenName": "Tether USD",
     *             "tokenAbbr": "USDT",
     *             "tokenDecimal": 6,
     *             "tokenCanShow": 1,
     *             "tokenType": "trc20",
     *             "tokenLogo": "https://static.tronscan.org/production/logo/usdtlogo.png",
     *             "vip": true,
     *             "tokenPriceInTrx": 16.071628241015605366,
     *             "amount": 32.127184853790195126634,
     *             "nrOfTokenHolders": 16426791,
     *             "transferCount": 639350205
     *         }
     *     ]
     */
    //余额要除 10的6次方
    public String balance;

    public String tokenId;


}
