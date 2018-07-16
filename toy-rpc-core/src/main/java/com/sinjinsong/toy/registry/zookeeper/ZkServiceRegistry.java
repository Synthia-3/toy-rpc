package com.sinjinsong.toy.registry.zookeeper;

import com.sinjinsong.toy.common.constant.CharsetConst;
import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.config.RegistryConfig;
import com.sinjinsong.toy.registry.api.ClusterCallback;
import com.sinjinsong.toy.registry.api.support.AbstractServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by SinjinSong on 2017/9/27.
 * 服务器进行服务注册或者客户端进行服务发现
 */
@Slf4j
public class ZkServiceRegistry extends AbstractServiceRegistry {
    private ZkSupport zkSupport;
    
    private static long TEN_SEC = 10000000000L;
    private static final String ZK_REGISTRY_PATH = "/toy";
    
    private volatile Thread discoveringThread;
    
    public ZkServiceRegistry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    @Override
    public void init() {
        zkSupport = new ZkSupport();
        zkSupport.connect(registryConfig.getAddress());
    }    
    
    /**
     * 服务发现
     * 返回值的key是接口名，返回值的value是IP地址列表
     *
     * @return
     */
    @Override
    public void discover(String interfaceName, ClusterCallback callback) {
        // 如果该接口对应的地址不存在，那么watchNode
        log.info("discovering...");
        this.discoveringThread = Thread.currentThread();
        watchNode(interfaceName,callback);
        log.info("开始Park... ");
        LockSupport.parkNanos(this, TEN_SEC);
        log.info("Park结束");
    }

    /**
     * 数据格式：
     * /toy/AService/192.168.1.1:1221 -> 192.168.1.1:1221
     * /toy/AService/192.168.1.2:1221 -> 192.168.1.2:1221
     * /toy/BService/192.168.1.3:1221 -> 192.168.1.3:1221
     */
    private void watchNode(String interfaceName,ClusterCallback callback) {
        try {
            List<String> interfaceNames = zkSupport.getChildren(ZK_REGISTRY_PATH, false);
            for (String i : interfaceNames) {
                String path = generatePath(interfaceName);
                if (i.equals(interfaceName)) {
                    List<String> addresses = zkSupport.getChildren(path, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                watchNode(interfaceName,callback);
                            }
                        }
                    });
                    log.info("interfaceName:{} -> addresses:{}", interfaceName, addresses);
                    List<String> dataList = new ArrayList<>();
                    for (String node : addresses) {
                        byte[] bytes = zkSupport.getData(path + "/" + node, false, null);
                        dataList.add(new String(bytes, CharsetConst.UTF_8));
                    }
                    log.info("node data: {}", dataList);
                    callback.addresseChanged(dataList);
                }
            }
            LockSupport.unpark(discoveringThread);
        } catch (KeeperException | InterruptedException e) {
            throw new RPCException("ZK故障",e);
        }
    }

    /**
     * 服务注册
     *
     * @param address
     * @param interfaceName
     */
    @Override
    public void register(String address, String interfaceName) {
        String path = generatePath(interfaceName);
        try {
            zkSupport.createPathIfAbsent(path, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RPCException("ZK故障",e);
        }
        zkSupport.createNode(address, path);
    }

    @Override
    public void close() {
        zkSupport.close();
    }

    private static String generatePath(String interfaceName) {
        return new StringBuilder(ZK_REGISTRY_PATH).append("/").append(interfaceName).toString();
    }
}
