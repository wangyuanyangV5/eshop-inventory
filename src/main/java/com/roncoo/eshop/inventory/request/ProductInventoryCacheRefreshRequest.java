package com.roncoo.eshop.inventory.request;

import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;

public class ProductInventoryCacheRefreshRequest implements Request{

    //商品库存
    private Integer productId;

    //商品库存service
    private ProductInventoryServer productInventoryServer;

    private boolean forceRefresh;


    public ProductInventoryCacheRefreshRequest(Integer productId, ProductInventoryServer productInventoryServer,
                                               boolean forceRefresh) {
        this.productInventoryServer = productInventoryServer;
        this.productId = productId;
        this.forceRefresh = forceRefresh;
    }

    @Override
    public void process() {
        //从数据库中查询最新商品库数量
        ProductInventory productInventory = productInventoryServer.findProductInventory(productId);
        System.out.println("已查询到商品库存数量，商品id为"+ productId+"剩余库存数量为:"
                + productInventory.getInventoryCnt());
        //将最新的数据库库存数量存入缓存中去
       productInventoryServer.setProductInventoryCache(productInventory);

    }

    @Override
    public Integer getProductId() {
        return productId;
    }


    public boolean isForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
}
