package com.heartmove.controller;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.heartmove.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DubboController {

    private static volatile Map<String,Object>  DUBBO_BEAN_MAP = null;

    @PostMapping("{group}/{version}/invoke")
    public void invoke(@PathVariable String group, @PathVariable String version,
                       @RequestHeader(name = "className") String className,
                       HttpServletRequest request, HttpServletResponse response){
        log.info("className:{} , group:{}, version:{}", className, group, version);
        RpcResult rpcResult = new RpcResult();
        // 处理请求
        try {
            RpcInvocation invocation = convertToInvocation(request);
            //开始调用接口
            Object bean = getBean(className, group, version);
            if(null == bean){
                throw new RpcException(String.format("dubbo service bean not found: class:%s,group:%s,version:%s", className, group, version));
            }
            Method method = bean.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            Object result = method.invoke(bean, invocation.getArguments());

            //设置返回结果
            rpcResult.setValue(result);
            byte[] buffer = serialization(rpcResult);
            response.setContentType("application/octet-stream");
            OutputStream stream = response.getOutputStream();
            stream.write(buffer);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rpcResult.setException(e);
            try {
                byte[] buffer = serialization(rpcResult);
                response.setContentType("application/octet-stream");
                OutputStream stream = response.getOutputStream();
                stream.write(buffer);
                stream.flush();
                stream.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 从请求中拿到RpcInvocation对象
     * @param request
     * @return
     * @throws IOException
     */
    private RpcInvocation convertToInvocation(HttpServletRequest request) throws IOException{
        ServletInputStream sis = null;
        Hessian2Input h2i = null;
        try {
            sis = request.getInputStream();
            h2i = new Hessian2Input(sis);
            h2i.startMessage();
            RpcInvocation invocation = (RpcInvocation) h2i.readObject();
            h2i.completeMessage();
            return invocation;
        } finally {
            try {
                if(null != h2i){
                    h2i.close();
                }
                if(null != sis){
                    sis.close();
                }
            } catch (IOException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    /**
     * 将RpcResult序列化成byte[]
     * @param rpcResult
     * @return
     * @throws IOException
     */
    private byte[] serialization(RpcResult rpcResult) throws IOException{
        // 序列化
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output h2o = new Hessian2Output(os);
        try {
            h2o.startMessage();
            h2o.writeObject(rpcResult);
            h2o.completeMessage();
            h2o.flush();
            return os.toByteArray();
        } finally {
            h2o.close();
            os.close();
        }
    }



    private Object getBean(String className, String group, String version){
        String key = beanKey(className, group, version);
        if(null == DUBBO_BEAN_MAP){
            synchronized (this){
                if(null == DUBBO_BEAN_MAP){
                    ApplicationContext context = App.applicationContext;
                    Map<String,Object> tempMap = new HashMap<>();
                    Arrays.stream(context.getBeanNamesForType(ServiceBean.class))
                            .map(beanName -> context.getBean(beanName, ServiceBean.class))
                            .forEach(bean -> tempMap.put(beanKey(bean.getInterface(), bean.getGroup(), bean.getVersion()), bean.getRef()));
                    DUBBO_BEAN_MAP = tempMap;
                }
            }
        }
         return DUBBO_BEAN_MAP.get(key);
    }

    public String beanKey(String className, String group, String version){
        return className+"|"+group+"|"+version;
    }

}
