package edu.dlut.springcloud.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  20:05
 * DESCRIPTION:
 **/
@Mapper
public interface StorageDAO {
    /**
     * 减库存
     *
     * @param productId
     * @param count
     * @return
     */
    int decrease(@Param("productId") Long productId, @Param("count") Integer count);
}
