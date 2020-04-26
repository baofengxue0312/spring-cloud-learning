package edu.dlut.springcloud.dao;

import edu.dlut.springcloud.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  19:50
 * DESCRIPTION:
 **/
@Mapper
public interface OrderDAO {

    int create(Order order);

    /**
     * 修改订单状态，0——>1
     *
     * @param userId
     * @param status
     * @return
     */
    int update(@Param("userId") Long userId, @Param("status") Integer status);

}
