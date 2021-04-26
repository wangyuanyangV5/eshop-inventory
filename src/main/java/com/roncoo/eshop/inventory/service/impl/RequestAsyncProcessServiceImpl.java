package com.roncoo.eshop.inventory.service.impl;

import com.roncoo.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.roncoo.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.roncoo.eshop.inventory.request.Request;
import com.roncoo.eshop.inventory.request.RequestQueue;
import com.roncoo.eshop.inventory.service.RequestAsyncProcessService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class RequestAsyncProcessServiceImpl  implements RequestAsyncProcessService {
    @Override
    public void process(Request request) {

        //做请求的路由，根据商品id路由到不同处理请求的队列中去
        ArrayBlockingQueue<Request> queue = getRoutingQueue(request.getProductId());

        //将请求放入到请求队列中
        try {
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取路由到的内存队列
     * @param productId 商品id
     * @return 内存队列
     */
    private ArrayBlockingQueue<Request> getRoutingQueue(Integer productId){
        RequestQueue requestQueue = RequestQueue.getInstance();
        String key = String.valueOf(productId);
        int h;
        int hash = (key == null) ? 0 : (h=key.hashCode()) ^(h>>>16);

        //对hash值取模，获取到请求处理队列的下标
        int index =  (requestQueue.queueSize() -1) & hash;
        System.out.println("商品id="+productId+",队列索引："+index);
        return requestQueue.getByIndex(index);
    }
}
