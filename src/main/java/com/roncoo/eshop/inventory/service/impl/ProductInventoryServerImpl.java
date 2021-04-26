package com.roncoo.eshop.inventory.service.impl;

import com.roncoo.eshop.inventory.dao.RedisDAO;
import com.roncoo.eshop.inventory.mapper.ProductInventoryMapper;
import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProductInventoryServerImpl implements ProductInventoryServer {
    @Resource
    private ProductInventoryMapper productInventoryMapper;

    @Resource
    private RedisDAO redisDAO;

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
        System.out.println("数据库中已修改商品id为"+productInventory.getProductId()
                +"的库存，剩余："+productInventory.getInventoryCnt());
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
          String key = "product:inventory:"+ productInventory.getProductId();
          System.out.println("开始执行redis删除操作，key"+key);
          redisDAO.delet(key);
    }

    @Override
    public ProductInventory findProductInventory(Integer productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:"+ productInventory.getProductId();
        redisDAO.set(key,String.valueOf(productInventory.getInventoryCnt()));
        System.out.println("已更新商品库存缓存，商品id为:"+productInventory.getProductId()+
                "商品数量为:"+ productInventory.getInventoryCnt()+";key:"+ key);
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
