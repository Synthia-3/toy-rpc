package com.sinjinsong.toy.invoke.async;

import com.sinjinsong.toy.common.context.RPCThreadLocalContext;
import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.invoke.api.support.AbstractInvocation;
import com.sinjinsong.toy.transport.common.domain.RPCResponse;

import java.util.concurrent.Future;

/**
 * @author sinjinsong
 * @date 2018/6/10
 */
public class AsyncInvocation extends AbstractInvocation {
    
    @Override
    public RPCResponse invoke() throws RPCException {
        Future<RPCResponse> future = invoker.getEndpoint().submit(rpcRequest);
        RPCThreadLocalContext.getContext().setFuture(future);
        return null;
    }
}
