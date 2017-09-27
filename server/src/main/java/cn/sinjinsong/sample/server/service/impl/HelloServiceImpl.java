package cn.sinjinsong.sample.server.service.impl;


import cn.sinjinsong.rpc.core.annotation.RPCService;
import cn.sinjinsong.sample.api.domain.User;
import cn.sinjinsong.sample.api.service.HelloService;

/**
 * Created by SinjinSong on 2017/7/30.
 */
@RPCService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(User user) {
        return "Hello, " + user.getUsername();
    }
}
