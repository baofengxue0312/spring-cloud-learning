package edu.dlut.springcloud.service.impl;

import edu.dlut.springcloud.dao.StorageDAO;
import edu.dlut.springcloud.service.StorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @AUTHOR: raymond
 * @DATETIME: 2020/4/26  20:08
 * DESCRIPTION:
 **/
@Service
public class StorageServiceImpl implements StorageService {

    @Resource
    private StorageDAO storageDao;

    /**
     * 减库存
     *
     * @param productId
     * @param count
     * @return
     */
    @Override
    public void decrease(Long productId, Integer count) {
        storageDao.decrease(productId, count);
    }
}
