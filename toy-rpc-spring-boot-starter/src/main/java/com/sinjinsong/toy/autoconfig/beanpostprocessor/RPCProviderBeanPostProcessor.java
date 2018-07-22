package com.sinjinsong.toy.autoconfig.beanpostprocessor;

import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.config.ServiceConfig;
import com.sinjinsong.toy.config.annotation.RPCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

/**
 * @author sinjinsong
 * @date 2018/7/15
 */
@Slf4j
public class RPCProviderBeanPostProcessor extends AbstractRPCBeanPostProcessor {
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if(!beanClass.isAnnotationPresent(RPCService.class)) {
            return bean;
        }
        Class<?>[] interfaces = beanClass.getInterfaces();
        Class<?> interfaceClass = null;
        if (interfaces.length >= 1) {
            interfaceClass = interfaces[0];
        } else {
            throw new RPCException("该服务 {} 未实现任何服务接口",beanClass);
        }
        RPCService rpcService = beanClass.getAnnotation(RPCService.class);
        ServiceConfig<Object> config = ServiceConfig.builder()
                .interfaceName(interfaceClass.getName())
                .interfaceClass((Class<Object>) interfaceClass)
                .isCallback(rpcService.callback())
                .callbackMethod(rpcService.callbackMethod())
                .callbackParamIndex(rpcService.callbackParamIndex())
                .ref(bean).build();
        initConfig(config);
        config.export();
        log.info("暴露服务:{}",interfaceClass);
        return bean;
    }
}
