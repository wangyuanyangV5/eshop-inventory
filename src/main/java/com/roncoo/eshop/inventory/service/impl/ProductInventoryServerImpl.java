package com.roncoo.eshop.inventory.service.impl;

import com.roncoo.eshop.inventory.dao.RedisDAO;
import com.roncoo.eshop.inventory.mapper.ProductInventoryMapper;
import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;

import javax.annotation.Resource;

public class ProductInventoryServerImpl implements ProductInventoryServer {
    @Resource
    private ProductInventoryMapper productInventoryMapper;

    @Resource
    private RedisDAO redisDAO;

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
          String key = "product:inventory:"+ productInventory.getProductId();
          redisDAO.delet(key);
    }

    @Override
    public ProductInventory findProductInventory(Integer productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:"+ productInventory.getProductId();
        redisDAO.set(key,String.valueOf(productInventory.inventoryCnt));
    }

    @Override
    public ProductInventory getProductInventoryCache(Integer productId) {
        Long inventoryCnt = 0L;
        String key = "product:inventory:"+ productId;
        String result = redisDAO.get(key);
        if(result != null && !"".equals(result)){
            inventoryCnt = Long.parseLong(result);
            return new ProductInventory(productId,inventoryCnt);
        }
        return null;
    }
}
