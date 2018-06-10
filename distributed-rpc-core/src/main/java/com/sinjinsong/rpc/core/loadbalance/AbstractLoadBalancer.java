package com.sinjinsong.rpc.core.loadbalance;

import com.sinjinsong.rpc.core.client.endpoint.Endpoint;
import com.sinjinsong.rpc.core.domain.RPCRequest;
import com.sinjinsong.rpc.core.zk.ServiceDiscovery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sinjinsong
 * @date 2018/6/10
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {
    private ServiceDiscovery serviceDiscovery;
    private Map<String, Endpoint> endpoints = new ConcurrentHashMap<>();

    public AbstractLoadBalancer(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }
    
    @Override
    public Endpoint select(RPCRequest request) {
        List<String> newAddresses = serviceDiscovery.discover();
        Set<String> oldAddresses = endpoints.keySet();

        Set<String> intersect = new HashSet<>(newAddresses);
        intersect.retainAll(oldAddresses);
        
        for (String address : oldAddresses) {
            if (!intersect.contains(address)) {
                endpoints.remove(address);
            }
        }
        
        for (String address : newAddresses) {
            if (!intersect.contains(address)) {
                endpoints.put(address, new Endpoint(address));
            }
        }
        return doSelect(new ArrayList<>(endpoints.values()), request);
    }
    
    abstract protected Endpoint doSelect(List<Endpoint> endpoints, RPCRequest request);

    @Override
    public void close() {
        endpoints.values().forEach(endpoint -> endpoint.close());
    }
}
