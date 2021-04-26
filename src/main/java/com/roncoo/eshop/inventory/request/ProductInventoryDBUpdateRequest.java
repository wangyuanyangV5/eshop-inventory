package com.roncoo.eshop.inventory.request;

import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;

/**
 * cache aside pattern
 *
 * 删除缓存
 *
 * 更新数据库
 */
public class ProductInventoryDBUpdateRequest implements  Request{
    //商品库存
    private ProductInventory productInventory;

    //商品库存service
    private ProductInventoryServer productInventoryServer;


    public ProductInventoryDBUpdateRequest(ProductInventory productInventory,
                                           ProductInventoryServer productInventoryServer) {
        this.productInventoryServer = productInventoryServer;
        this.productInventory = productInventory;
    }

    @Override
    public void process() {
        System.out.println("数据库更新请求开始执行，商品id："+ productInventory.getProductId());
        //删除缓存
       productInventoryServer.removeProductInventoryCache(productInventory);
       System.out.println("已删除redis缓存，productId："+ productInventory.getProductId());
       //todo:为了演示先删除缓存然后还没有更新数据库的时候读请求过来了，这里可以人工sleep一下
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //更新库存
        productInventoryServer.updateProductInventory(productInventory);
    }

    @Override
    public Integer getProductId() {
        return productInventory.getProductId();
    }

    @Override
    public boolean isForceRefresh() {
        return false;
    }
}
