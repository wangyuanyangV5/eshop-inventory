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
        RequestQueue requestQueue = RequestQueue.getInstance();
        Map<Integer,Boolean> flagMap = requestQueue.getFlagMap();


        if(request instanceof ProductInventoryDBUpdateRequest){
            //如果是一个更新数据库的请求，就将那个productId对应的标识设为true
            flagMap.put(request.getProductId(),true);
        }else if(request instanceof ProductInventoryCacheRefreshRequest){
            //如果是刷新缓存的请求，那么就判断表四不为空而且是true，就说明之前有一个这个商品的数量变更的请求
            Boolean flag = flagMap.get(request.getProductId());

            if(flag == null){
               flagMap.put(request.getProductId(),false);
            }

            if(flag != null && flag){
                flagMap.put(request.getProductId(),false);
            }

            //如果是刷新缓存的请求而且发现标识不为空，但是标识是false
            //就说明前面已经有一个数据库更新请求+一个缓存刷新请求了
            if(flag != null && !flag){
                //对于这种读请求，直接过滤掉，不妨到请求队列中去
                return;
            }

        }

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
        return requestQueue.getByIndex(index);
    }
}
