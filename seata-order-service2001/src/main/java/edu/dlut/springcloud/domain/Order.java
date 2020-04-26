package edu.dlut.springcloud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  19:34
 * DESCRIPTION:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private Long id;

    private Long userId;

    private Long productId;

    private Integer count;

    private BigDecimal money;

    /** 订单状态 0:创建中,1:已完结 */
    private Integer status;
}
