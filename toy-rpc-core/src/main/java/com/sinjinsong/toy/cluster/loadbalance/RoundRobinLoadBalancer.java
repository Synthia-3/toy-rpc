package com.sinjinsong.toy.cluster.loadbalance;

import com.sinjinsong.toy.cluster.support.AbstractLoadBalancer;
import com.sinjinsong.toy.registry.ServiceRegistry;
import com.sinjinsong.toy.serialize.api.Serializer;
import com.sinjinsong.toy.transport.client.endpoint.Endpoint;
import com.sinjinsong.toy.transport.domain.RPCRequest;

import java.util.List;

/**
 * @author sinjinsong
 * @date 2018/3/11
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    private int index = 0;

    public RoundRobinLoadBalancer(ServiceRegistry ServiceRegistry, Serializer serializer) {
        super(ServiceRegistry, serializer);
    }
    
    @Override
    protected Endpoint doSelect(List<Endpoint> endpoints, RPCRequest request) {
         if(endpoints.size() == 0) {
            return null;
        }
        Endpoint result = endpoints.get(index);
        index = (index + 1) % endpoints.size();
        return result;
    }
}
