package com.sinjinsong.toy.invocation.api.support;


import com.sinjinsong.toy.common.enumeration.ErrorEnum;
import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.config.ReferenceConfig;
import com.sinjinsong.toy.invocation.api.Invocation;
import com.sinjinsong.toy.transport.api.domain.RPCRequest;
import com.sinjinsong.toy.transport.api.domain.RPCResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;

/**
 * @author sinjinsong
 * @date 2018/6/10
 */
@Slf4j
public abstract class AbstractInvocation implements Invocation {
    private ReferenceConfig referenceConfig;
    private RPCRequest rpcRequest;

    public final void setReferenceConfig(ReferenceConfig referenceConfig) {
        this.referenceConfig = referenceConfig;
    }

    public final void setRpcRequest(RPCRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }
    
    /**
     * 留给Sync/Oneway/Async/Callback的子类去覆盖，用来获取远程调用结果
     *
     * @return
     */
    protected abstract Future<RPCResponse> doCustomProcess();
    
    @Override
    public final RPCResponse invoke() throws RPCException {
        RPCResponse response;
        try {
            response = doInvoke();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RPCException(ErrorEnum.TRANSPORT_FAILURE, e, "transport异常");
        }
        return response;
    }

    /**
     * 执行对应子类的调用逻辑，可以抛出任何异常
     *
     * @return
     * @throws Throwable
     */
    protected abstract RPCResponse doInvoke() throws Throwable;

    public final ReferenceConfig getReferenceConfig() {
        return referenceConfig;
    }
    
    public final RPCRequest getRpcRequest() {
        return rpcRequest;
    }
}
