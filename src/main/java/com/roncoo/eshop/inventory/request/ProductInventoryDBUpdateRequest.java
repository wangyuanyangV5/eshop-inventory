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
        //删除缓存
       productInventoryServer.removeProductInventoryCache(productInventory);
        //更新库存
        productInventoryServer.updateProductInventory(productInventory);
    }
}
