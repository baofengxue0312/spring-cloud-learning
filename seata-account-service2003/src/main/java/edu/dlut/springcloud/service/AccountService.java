package edu.dlut.springcloud.service;

import java.math.BigDecimal;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  20:17
 * DESCRIPTION:
 **/
public interface AccountService {
    /**
     * 减库存
     * @param userId 用户id
     * @param money  金额
     * @return
     */
    void decrease(Long userId, BigDecimal money);
}
