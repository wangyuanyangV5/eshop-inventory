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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试case
 * 1：先发送一个商品库存更新请求，此时会先删除redis缓存，然后模拟卡顿
 * 在卡断的5秒内发送一个获取商品缓存的请求，因为缓存中没有数据，就会将请求路由到相同队列中
 * 5秒后写请求执行完成，读请求才会返回数据
 */
@RestController
public class ProductInventoryController {

    @Autowired
    private ProductInventoryServer productInventoryServer;

    @Autowired
    private RequestAsyncProcessService requestAsyncProcessService;

   @RequestMapping("/updateProductInventory")
    public Response updateProductInventory(@RequestBody ProductInventory productInventory){
       Response response = null;
        try {
            System.out.println("接受到更新商品的请求；商品id："+
                    productInventory.getProductId() +";商品库存："+productInventory.getInventoryCnt());
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
        System.out.println("接受到获取商品id为:"+productId+"库存数量的请求");
        try {
            Request request = new ProductInventoryCacheRefreshRequest(productId,productInventoryServer,false);
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
                    System.out.println("在200毫秒内读取到了商品库存缓存,商品id为:"+ productId+
                            "库存数量为"+productInventory.getInventoryCnt());
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

                //这个过程实际上是一个读操作的过程，但是没有放到队列中串行去处理，还是有数据不一致问题
//                productInventoryServer.setProductInventoryCache(productInventory);
                request = new ProductInventoryCacheRefreshRequest(productId,productInventoryServer,true);
                requestAsyncProcessService.process(request);
                //代码运行到这里，只有三种情况：
                //1：上一次也是读请求，数据刷入redis，但是被redis lru算法给清理掉了，标志位还是false
                //所以此时下一个请求是从缓存中获取不到数据，再放一个读request 进队列，让数据去刷新一下

                //2:可能在200毫秒内，读请求一直在队列中积压着，没有等待到它执行
                //所以就直接查一次库，然后给队列里放入一个刷新缓存的请求

                //3：数据库本身就没有商品库存信息  缓存穿透 穿透redis 请求到达edis
                return productInventory;
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return new ProductInventory(productId,-1L);
    }
}
