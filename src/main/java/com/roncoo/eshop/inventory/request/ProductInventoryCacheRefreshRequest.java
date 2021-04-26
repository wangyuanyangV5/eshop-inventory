package com.roncoo.eshop.inventory.request;

import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;

public class ProductInventoryCacheReloadRequest implements Request{

    //商品库存
    private Integer productId;

    //商品库存service
    private ProductInventoryServer productInventoryServer;


    public ProductInventoryCacheReloadRequest(Integer productId,
                                           ProductInventoryServer productInventoryServer) {
        this.productInventoryServer = productInventoryServer;
        this.productId = productId;
    }

    @Override
    public void process() {
        //从数据库中查询最新商品库数量
        ProductInventory productInventory = productInventoryServer.findProductInventory(productId);
        //将最新的数据库库存数量存入缓存中去
       productInventoryServer.setProductInventoryCache(productInventory);
    }
}
