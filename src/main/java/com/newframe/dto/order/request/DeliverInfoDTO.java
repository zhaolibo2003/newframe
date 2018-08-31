package com.newframe.dto.order.request;

import lombok.Data;

/**
 * @author kfm
 * @date 2018-08-10
 * 发货信息封装
 */
@Data
public class DeliverInfoDTO {
    /**订单编号*/
    private Long orderId;
    /**手机序列号*/
    private String serialNumber;
    /**快递单号*/
    private String deliverId;
    /**发货时间*/
    private Long deliverTime;
    /**快递公司*/
    private String expressName;
    /**快递公司编号*/
    private String deliverCode;
}
