package com.demo.cpe.pool;

import com.demo.cpe.ServerSetting;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 线程池维护
 * Created by zjial on 2016/5/30.
 */
public class AbThreadPool {

    private static final EventExecutorGroup fixedThreadPool = new DefaultEventExecutorGroup(ServerSetting.BUSINESS_POOL_NUM, new NamedThreadFactory("NETTY-SERVER-BUSINESS-"));

    public static void execute(Runnable runnable) {
        fixedThreadPool.execute(runnable);
    }

}
