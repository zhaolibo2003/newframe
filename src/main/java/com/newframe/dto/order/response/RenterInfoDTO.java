package com.newframe.dto.order.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kfm
 * @date 2018.08.31 16:37
 */
@Data
public class RenterInfoDTO {
    private Long renterId;
    private String renterName;
}