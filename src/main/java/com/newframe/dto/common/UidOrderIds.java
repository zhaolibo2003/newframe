package com.newframe.dto.common;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UidOrderIds {
    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    private Long uid;
    /**
     * 订单id
     */
    @NotNull(message = "订单ids不能为空")
    private List<Long> orderIds;
}