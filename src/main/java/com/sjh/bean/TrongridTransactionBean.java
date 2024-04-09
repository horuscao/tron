package com.sjh.bean;

/**
 * 通过 trongrid 接口查询到的某个地址交易情况bean
 */
public class TrongridTransactionBean {
    /**
     * {
     * 			"transaction_id": "5012047ab335ad9627f3dd4a6cc69af9b3cb03261e9179d869da6f968a743870",
     * 			"token_info": {
     * 				"symbol": "USDT",
     * 				"address": "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
     * 				"decimals": 6,
     * 				"name": "Tether USD"
     *                        },
     * 			"block_timestamp": 1666247859000,
     * 			"from": "TP2zZBXCCdTvXxnPxNotaL6ze38GEpkm3W",
     * 			"to": "TC4hBhKgopXcdrQMy9WC82RHYbt1vPAhCE",
     * 			"type": "Transfer",
     * 			"value": "3000000"
     * 			* 		}
     */

    public String transaction_id;
    public String block_timestamp;
    public String last_transfer_timestamp;
    public String from;
    public String type;

    public String to;
    public String value;


    @Override
    public String toString() {
        return "TrongridTransactionBean{" +
                "block_timestamp='" + block_timestamp + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
