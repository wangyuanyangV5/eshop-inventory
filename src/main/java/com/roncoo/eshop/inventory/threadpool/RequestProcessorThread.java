package com.roncoo.eshop.inventory.threadpool;

import com.roncoo.eshop.inventory.request.Request;

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
				//执行request操作
				request.process();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}

}
