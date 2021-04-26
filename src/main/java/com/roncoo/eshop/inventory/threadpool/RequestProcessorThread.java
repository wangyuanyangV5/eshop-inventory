package com.roncoo.eshop.inventory.threadpool;

import com.roncoo.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.roncoo.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.roncoo.eshop.inventory.request.Request;
import com.roncoo.eshop.inventory.request.RequestQueue;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * 执行请求的工作线程
 * @author Administrator
 *
 */
public class RequestProcessorThread implements Callable<Boolean> {

	/**
	 * 自己监控的内存队列
	 */
	private ArrayBlockingQueue<Request> queue;

	public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
		this.queue = queue;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			while(true) {
				//block 就说明队列满了或者是空的都会在执行操作的时候阻塞
				Request request = queue.take();

				RequestQueue requestQueue = RequestQueue.getInstance();
				Map<Integer,Boolean> flagMap = requestQueue.getFlagMap();
                boolean forceRefresh = request.isForceRefresh();
               if(!forceRefresh){
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
						   return  true;
					   }

				   }
			   }

                System.out.println("工作线程处理请求，商品id="+ request.getProductId());
				//执行request操作
				request.process();

				//假如说执行完读请求之后，加入数据已经刷新到redis中了
				//但是后面可能redis中的数据因为内存满了，被自动清理掉
				//如果数据从 redis中被自动清理掉了以后
				//然后又来了一个读请求，此时发现标志位为false，就不会执行刷新的操作了
				//所以在执行完读请求之后实
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}

}
