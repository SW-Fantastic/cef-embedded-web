package org.swdc.cef;

import org.cef.callback.CefQueryCallback;
import org.swdc.cef.control.CEFResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CEFAsyncDispatcher {

    public static class CEFAsyncMessageExecutor implements Runnable {

        private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        private Map<CEFAsync, CefQueryCallback> callbackMap = new HashMap<>();

        @Override
        public void run() {
            while (true) {
                try {
                    try {
                        // 刚开始是肯定没有需要分发的异步方法调用的，所以这里直接阻塞掉线程即可
                        cyclicBarrier.await();
                    } catch (InterruptedException|BrokenBarrierException e) {
                        // 阻塞不掉就直接reset，就算是执行一次额外的消息分发也没有多大的消耗
                        cyclicBarrier.reset();
                    }
                    // 过滤已经处理完毕的异步消息，通过CallBack触发JavaScript回调。
                    List<CEFAsync> keys = callbackMap.keySet()
                            .stream()
                            .filter(async -> async.getResult() != null)
                            .collect(Collectors.toList());

                    for (CEFAsync async: keys) {
                        // 回调JavaScript的CallBack，发送处理结果
                        CefQueryCallback callback = callbackMap.remove(async);
                        CEFResult result = async.getResult();
                        if (result.getCode() != 200) {
                            callback.failure(result.getCode(),result.getMessage());
                        } else {
                            callback.success(result.getMessage());
                        }
                    }
                } catch (Exception e) {
                    // 异常处理放在while内部，防止线程因为异常挂掉。
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * 增加一个待处理的异步消息，
         * @param async 异步消息对象
         * @param callback CallBack
         */
        void await(CEFAsync async, CefQueryCallback callback) {
            callbackMap.put(async,callback);
        }

        /**
         * 激活线程，分发处理结果。
         */
        synchronized void notifyCallback() {
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                cyclicBarrier.reset();
            }
        }

    }

    private Executor executor = Executors.newSingleThreadExecutor( t -> {
        // 需要一个守护线程，它会一直运行下去直到应用结束。
        Thread thread = new Thread(t);
        thread.setName("CEF Async Deamon");
        thread.setDaemon(true);
        return thread;
    });

    private CEFAsyncMessageExecutor msgExec = new CEFAsyncMessageExecutor();

    public CEFAsyncDispatcher() {
        // 启动异步消息处理器。
        executor.execute(msgExec);
    }

    /**
     * 分发异步消息
     * @param async 消息对象
     * @param callback JavaScript回调
     */
    public void dispatch(CEFAsync async,CefQueryCallback callback) {
        if (async.getResult() != null) {
            // 如果消息是已经处理完毕的，就直接通过CallBack回调JavaScript。
            CEFResult result = async.getResult();
            if (result.getCode() != 200) {
                callback.failure(result.getCode(),result.getMessage());
            } else {
                callback.success(result.getMessage());
            }
            return;
        }
        // 投放消息对象
        msgExec.await(async,callback);
    }

    /**
     * 激活处理器，进行一次消息分发。
     */
    public void awake() {
        msgExec.notifyCallback();
    }

}
