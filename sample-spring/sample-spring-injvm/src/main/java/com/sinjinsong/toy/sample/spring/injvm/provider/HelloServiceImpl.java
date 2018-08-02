package com.sinjinsong.toy.sample.spring.injvm.provider;


import com.sinjinsong.toy.autoconfig.annotation.RPCService;
import com.sinjinsong.toy.sample.spring.api.domain.User;
import com.sinjinsong.toy.sample.spring.api.service.HelloService;

/**
 * Created by SinjinSong on 2017/7/30.
 */
@RPCService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(User user) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello, " + user.getUsername();
    }
}
