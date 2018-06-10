package com.sinjinsong.rpc.sample.spring.api.service;

import com.sinjinsong.rpc.sample.spring.api.callback.HelloCallback;
import com.sinjinsong.rpc.sample.spring.api.domain.User;

/**
 * @author sinjinsong
 * @date 2018/6/10
 */
public interface HelloServiceWithCallback {
     void hello(User user, HelloCallback callback);
}
