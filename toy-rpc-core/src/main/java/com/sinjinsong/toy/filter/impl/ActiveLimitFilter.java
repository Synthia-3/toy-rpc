package com.sinjinsong.toy.filter.impl;

import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.filter.Filter;
import com.sinjinsong.toy.protocol.api.Invoker;
import com.sinjinsong.toy.protocol.api.Result;
import com.sinjinsong.toy.transport.common.domain.RPCRequest;

/**
 * @author sinjinsong
 * @date 2018/7/7
 */
public class ActiveLimitFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, RPCRequest rpcRequest) throws RPCException {
        //        Result result;
//        try {
//            RPCStatus.incCount(invoker.getUrl());
//            result = invoker.invoke(invocation);
//        }catch(RPCException e) {
//            
//        }
        return null;
    }
}
