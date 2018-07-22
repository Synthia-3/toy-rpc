package com.sinjinsong.toy.protocol.toy;

import com.sinjinsong.toy.config.ApplicationConfig;
import com.sinjinsong.toy.protocol.api.support.AbstractRemoteInvoker;
import com.sinjinsong.toy.registry.api.ServiceURL;
import com.sinjinsong.toy.transport.api.Endpoint;
import com.sinjinsong.toy.transport.api.domain.RPCRequest;
import com.sinjinsong.toy.transport.api.domain.RPCResponse;
import com.sinjinsong.toy.transport.toy.client.ToyEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author sinjinsong
 * @date 2018/7/14
 * 抽象的是一个服务接口的一个服务器地址
 */
@Slf4j
public class ToyInvoker<T> extends AbstractRemoteInvoker<T> {
    private ToyEndpoint toyEndpoint;
    
    @Override
    protected Function<RPCRequest, Future<RPCResponse>> getProcessor() {
        return rpcRequest -> getEndpoint().submit(rpcRequest);
    }
    
    @Override
    protected void doInitEndpoint(ServiceURL serviceURL, ApplicationConfig applicationConfig) {
        ToyEndpoint toyEndpoint = new ToyEndpoint();
        toyEndpoint.init(applicationConfig, serviceURL);
        this.toyEndpoint = toyEndpoint;
    }

    @Override
    public void updateServiceConfig(ServiceURL serviceURL) {
        this.toyEndpoint.updateServiceConfig(serviceURL);
    }

    @Override
    protected Endpoint getEndpoint() {
        return toyEndpoint;
    }
}
