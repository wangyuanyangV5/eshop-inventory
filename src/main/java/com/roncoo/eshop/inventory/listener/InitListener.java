package com.roncoo.eshop.inventory.listener;



import com.roncoo.eshop.inventory.threadpool.RequestProcessorThreadPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 系统初始化监听器
 */
public class InitListener  implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //初始化工作线程池和内存队列
        RequestProcessorThreadPool.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
