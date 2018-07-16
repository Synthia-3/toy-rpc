package com.sinjinsong.toy.autoconfig;


import com.sinjinsong.toy.autoconfig.beanpostprocessor.RPCConsumerBeanPostProcessor;
import com.sinjinsong.toy.autoconfig.beanpostprocessor.RPCProviderBeanPostProcessor;
import com.sinjinsong.toy.cluster.support.AbstractLoadBalancer;
import com.sinjinsong.toy.common.enumeration.LoadBalanceType;
import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.config.ApplicationConfig;
import com.sinjinsong.toy.config.ClusterConfig;
import com.sinjinsong.toy.config.ProtocolConfig;
import com.sinjinsong.toy.config.RegistryConfig;
import com.sinjinsong.toy.filter.impl.ActiveLimitFilter;
import com.sinjinsong.toy.protocol.toy.ToyProtocol;
import com.sinjinsong.toy.proxy.JDKRPCProxyFactory;
import com.sinjinsong.toy.registry.zookeeper.ZkServiceRegistry;
import com.sinjinsong.toy.serialize.protostuff.ProtostuffSerializer;
import com.sinjinsong.toy.transport.server.RPCServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author sinjinsong
 * @date 2018/3/10
 */
@EnableConfigurationProperties(RPCProperties.class)
@Configuration
@Slf4j
public class ToyRPCAutoConfiguration implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    public static Boolean NEED_SERVER = Boolean.FALSE;
    @Autowired
    private RPCProperties properties;
    private ApplicationContext ctx;

    @Bean(initMethod = "init", destroyMethod = "close")
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = properties.getRegistry();
        if (registryConfig == null) {
            throw new RPCException("必须配置registry");
        }
        //TODO 根据type创建ServiceRegistry
        ZkServiceRegistry serviceRegistry = new ZkServiceRegistry(registryConfig);
        registryConfig.setRegistryInstance(serviceRegistry);
        log.info("{}", registryConfig);
        return registryConfig;
    }

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig application = properties.getApplication();
        if (application == null) {
            throw new RPCException("必须配置applicationConfig");
        }
        // TODO 根据类型创建proxyFactory和serializer
        application.setProxyFactoryInstance(new JDKRPCProxyFactory());
        application.setSerializerInstance(new ProtostuffSerializer());
        log.info("{}", application);
        return application;
    }

    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = properties.getProtocol();
        if (protocolConfig == null) {
            throw new RPCException("必须配置protocolConfig");
        }
        protocolConfig.setProtocolInstance(new ToyProtocol());
        log.info("{}", protocolConfig);
        return protocolConfig;
    }

    @Bean
    public ClusterConfig clusterconfig(RegistryConfig registryConfig,ProtocolConfig protocolConfig) {
        ClusterConfig clusterConfig = properties.getCluster();
        if (clusterConfig == null) {
            throw new RPCException("必须配置clusterConfig");
        }
        AbstractLoadBalancer loadBalancer = LoadBalanceType.valueOf(clusterConfig.getLoadbalance().toUpperCase()).getLoadBalancer();
        loadBalancer.setRegistryConfig(registryConfig);
        loadBalancer.setProtocolConfig(protocolConfig);
        loadBalancer.setClusterConfig(clusterConfig);
        loadBalancer.setApplicationConfig(applicationConfig());
        
        clusterConfig.setLoadBalanceInstance(loadBalancer);
        log.info("{}", clusterConfig);
        return clusterConfig;
    }


    @Bean
    public RPCConsumerBeanPostProcessor rpcConsumerBeanPostProcessor(ApplicationConfig applicationConfig, ClusterConfig clusterConfig, ProtocolConfig protocolConfig, RegistryConfig registryConfig) {
        RPCConsumerBeanPostProcessor processor = new RPCConsumerBeanPostProcessor();
        processor.init(applicationConfig, clusterConfig, protocolConfig, registryConfig);
        log.info("RPCConsumerBeanPostProcessor init");
        return processor;
    }

    @Bean
    public RPCProviderBeanPostProcessor rpcProviderBeanPostProcessor(ApplicationConfig applicationConfig, ClusterConfig clusterConfig, ProtocolConfig protocolConfig, RegistryConfig registryConfig) {
        RPCProviderBeanPostProcessor processor = new RPCProviderBeanPostProcessor();
        processor.init(applicationConfig, clusterConfig, protocolConfig, registryConfig);
        log.info("RPCProviderBeanPostProcessor init");
        return processor;
    }

    @Bean
    public ActiveLimitFilter activeLimitFilter() {
        return new ActiveLimitFilter();
    } 
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Spring容器启动完毕，且NEED_SERVER为{}",NEED_SERVER);
        if (NEED_SERVER) {
            new RPCServer(
                    ctx.getBean(ApplicationConfig.class),
                    ctx.getBean(ClusterConfig.class),
                    ctx.getBean(RegistryConfig.class),
                    ctx.getBean(ProtocolConfig.class)
            ).run();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

}


