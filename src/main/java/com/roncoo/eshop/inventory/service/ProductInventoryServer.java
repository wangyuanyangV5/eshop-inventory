package com.roncoo.eshop.inventory.service;

import com.roncoo.eshop.inventory.model.ProductInventory;

public interface ProductInventoryServer {

    /**
     * 更新商品库存
     * @param productInventory 商品库存
     */
    void  updateProductInventory(ProductInventory productInventory);


    void removeProductInventoryCache(ProductInventory productInventory);
}
