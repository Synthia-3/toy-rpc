package com.sinjinsong.toy.protocol.api;


import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.transport.client.Endpoint;
import com.sinjinsong.toy.transport.common.domain.RPCRequest;
import com.sinjinsong.toy.transport.common.domain.RPCResponse;

/**
 * @author sinjinsong
 * @date 2018/7/7
 */
public interface Invoker<T> {

    Class<T> getInterface();

    RPCResponse invoke(RPCRequest rpcRequest) throws RPCException;
    
    Endpoint getEndpoint();
}
