package com.sinjinsong.toy.transport.api.domain;

import io.netty.util.Recycler;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by SinjinSong on 2017/7/30.
 * 
 * 几种使用场景：
 * 1）consumer发起请求时创建，将数据序列化传输到服务器后被回收(encoder)/injvm调用服务完毕后被回收
 * 2）provider收到请求时创建，服务调用完毕后被回收（反序列化直接获得对象，如果是protostuff可以reuse，其他不可reuse，可以recycle）
 * 
 * callback：
 * 1）provider进行服务调用时创建，数据序列化传输到客户端后被回收(encoder)
 * 2）consumer收到callback请求时被创建，callback调用完毕后被回收（反序列化直接获得对象，如果是protostuff可以reuse，其他不可reuse，可以recycle）
 * 
 */
@Data
public class RPCRequest implements Serializable {
    private final transient Recycler.Handle<RPCRequest> handle;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public RPCRequest(Recycler.Handle<RPCRequest> handle) {
        this.handle = handle;
    }

    public String key() {
        return new StringBuilder(interfaceName)
                .append(".")
                .append(methodName)
                .append(".")
                .append(Arrays.toString(parameterTypes))
                .append(".")
                .append(Arrays.toString(parameters)).toString();
    }
    
    public void recycle() {
        requestId = null;
        interfaceName = null;
        methodName = null;
        parameterTypes = null;
        parameters = null;
        handle.recycle(this);
    }
}
