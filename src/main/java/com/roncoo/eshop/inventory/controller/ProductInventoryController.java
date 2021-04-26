package com.roncoo.eshop.inventory.controller;

import com.roncoo.eshop.inventory.model.ProductInventory;
import com.roncoo.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.roncoo.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.roncoo.eshop.inventory.request.Request;
import com.roncoo.eshop.inventory.service.ProductInventoryServer;
import com.roncoo.eshop.inventory.service.RequestAsyncProcessService;
import com.roncoo.eshop.inventory.vo.Response;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductInventoryController {

    @Autowired
    private ProductInventoryServer productInventoryServer;

    @Autowired
    private RequestAsyncProcessService requestAsyncProcessService;

   @RequestMapping("/update/iventory")
    public Response updateProductInventory(ProductInventory productInventory){
       Response response = null;
        try {
            Request request = new ProductInventoryDBUpdateRequest(productInventory,productInventoryServer);
            requestAsyncProcessService.process(request);
            response = new Response(Response.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            response = new Response(Response.FAILURE);
        }
        return response;
   }


    @RequestMapping("/getProductIventory")
    public ProductInventory getProductInventory(@Param("productId") Integer productId){
       ProductInventory productInventory = null;

        try {
            Request request = new ProductInventoryCacheRefreshRequest(productId,productInventoryServer);
            requestAsyncProcessService.process(request);
            //将请求扔给service异步处理后，就需要while(true)一会，将在这里hang住
            //去尝试等待前面有商品库存更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中
            long nowTime = System.currentTimeMillis();
            long waitTime = 0L;
            long endTime = 0L;
            while (true){

                if(waitTime > 200){
                    break;
                }
                productInventory = productInventoryServer.getProductInventoryCache(productId);
                if(productInventory != null){
                    return productInventory;
                }
                //如果没有读取到缓存
                Thread.sleep(20);
                endTime = System.currentTimeMillis();
                waitTime = endTime - nowTime;
            }
            //如果等待时间超过200 ms 尝试一次从数据库读取数据
            productInventory = productInventoryServer.findProductInventory(productId);
            if(productInventory != null){
                //将缓存刷新一下，避免因为将flag的productId对应的值设为false后，redis中lru淘汰掉该productId
                //后造成每次都读库的操作
                productInventoryServer.setProductInventoryCache(productInventory);
                return productInventory;
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return new ProductInventory(productId,-1L);
    }
}
