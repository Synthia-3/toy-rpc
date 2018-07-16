package com.sinjinsong.toy.protocol.api.support;

import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.invoke.api.Invocation;
import com.sinjinsong.toy.protocol.api.Invoker;
import com.sinjinsong.toy.transport.client.Endpoint;
import com.sinjinsong.toy.transport.common.domain.RPCResponse;

/**
 * @author sinjinsong
 * @date 2018/7/14
 */
public abstract class AbstractInvoker<T> implements Invoker<T> {
    protected Class<T> interfaceClass;
    protected Endpoint endpoint;
    
    @Override
    public Class<T> getInterface() {
        return interfaceClass;
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }
    
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @param invocation
     * @return
     * @throws RPCException
     */
    @Override
    public final RPCResponse invoke(Invocation invocation) throws RPCException {
        return doInvoke(invocation);
    }
    
    protected abstract RPCResponse doInvoke(Invocation invocation) throws RPCException;
    
}
