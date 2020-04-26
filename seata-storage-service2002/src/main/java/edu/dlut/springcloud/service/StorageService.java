package edu.dlut.springcloud.service;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  20:07
 * DESCRIPTION:
 **/
public interface StorageService {
    void decrease(Long productId, Integer count);
}
