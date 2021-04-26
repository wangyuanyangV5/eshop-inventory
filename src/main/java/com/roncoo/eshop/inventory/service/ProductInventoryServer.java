package com.roncoo.eshop.inventory.service;

import com.roncoo.eshop.inventory.model.ProductInventory;

public interface ProductInventoryServer {

    /**
     * 更新商品库存
     * @param productInventory 商品库存
     */
    void  updateProductInventory(ProductInventory productInventory);


    void removeProductInventoryCache(ProductInventory productInventory);

    /**
     * 根据商品id查询商品库存
     * @param productId 商品id
     * @return 商品库存信息
     */
    ProductInventory findProductInventory(Integer productId);

    /**
     * 设置商品库存的缓存
     * @param productInventoryCache 商品库存
     */
    void  setProductInventoryCache(ProductInventory productInventoryCache);

    /**
     * 获取缓存中的商品数据
     * @param productId 商品id
     * @return 商品信息
     */
    ProductInventory getProductInventoryCache(Integer productId);
}
