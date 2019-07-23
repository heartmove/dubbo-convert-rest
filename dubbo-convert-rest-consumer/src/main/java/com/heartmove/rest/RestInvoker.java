package com.heartmove.rest;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class RestInvoker<T>  extends AbstractInvoker<T> {

    final ExecutorService executorService = Executors.newFixedThreadPool(500);

    public RestInvoker(Class<T> type, URL url) {
        super(type, url);
    }

    public RestInvoker(Class<T> type, URL url, String[] keys) {
        super(type, url, keys);
    }

    public RestInvoker(Class<T> type, URL url, Map<String, String> attachment) {
        super(type, url, new String[]{Constants.INTERFACE_KEY, Constants.GROUP_KEY, Constants.TOKEN_KEY, Constants.TIMEOUT_KEY});
    }

    @Override
    protected void setAvailable(boolean available) {
        super.setAvailable(available);
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {

        //String protocol = this.getUrl().getParameter(Constants.PROTOCOL_KEY);

        boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
        boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);

        RpcResult result = new RpcResult();
        try {
            if(isAsync){
                Future future = executorService.submit(new HttpCallable(this, invocation));
                RpcContext.getContext().setFuture(future);
                return result;
            }
            RpcContext.getContext().setFuture(null);
            result = call(invocation);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setException(e);
            throw new RpcException(e.getMessage(), e);
        }

        return result;
    }


    /**
     * 获取返回结果
     * @param request
     * @return
     */
    private static byte[] getResult(HttpRequestBase request) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3)
				.setConnectTimeout(3).build();
        request.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                int statusCodee = response.getStatusLine().getStatusCode();
                if(statusCodee == HttpStatus.SC_OK){
                    return EntityUtils.toByteArray(entity);
                }
                throw new RpcException(String.format("http call exception url：%s, statusCode：%s", request.getURI(), statusCodee));
                // long len = entity.getContentLength();// -1 表示长度未知

            }
        } finally {
            if(null != response){
                response.close();
                httpClient.close();
            }
        }
        throw new RpcException("http invoker exception, cannot get response!");
    }

    /**
     * 将字节数组转换成RpcResult
     * @param buffer
     * @return
     * @throws Exception
     */
    private static RpcResult toRpcResult(byte[] buffer) throws Exception{
        Hessian2Input h2i = new Hessian2Input(new ByteArrayInputStream(buffer));
        h2i.startMessage();
        RpcResult result = (RpcResult) h2i.readObject();
        h2i.completeMessage();
        h2i.close();
        return result;
    }


    public RpcResult call(Invocation invocation) throws Exception{

    	URL url = this.getUrl();
        String host = url.getParameter("host");
        if(StringUtils.isBlank(host) && !host.toLowerCase().startsWith("http://") && !host.toLowerCase().startsWith("https://")){
            throw new Exception("host不能为空，并且必须以http:// 或 https:// 开头");
        }

        String className = url.getParameter(Constants.INTERFACE_KEY);
        //接口版本
        String version = url.getParameter(Constants.VERSION_KEY);
        //接口分组
        String group = url.getParameter(Constants.GROUP_KEY);

        String timeout = url.getParameter(Constants.TIMEOUT_KEY);

        //将invocation进行序列化
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output h2o = new Hessian2Output(os);
        h2o.startMessage();
        h2o.writeObject(invocation);
        h2o.completeMessage();
        h2o.close();
        byte[] buffer = os.toByteArray();
        os.close();

        //进行http调用
        HttpPost httpPost = new HttpPost(host+"/"+ group +"/"+ version+"/invoke");
        httpPost.setEntity(new ByteArrayEntity(buffer));
        httpPost.setHeader("className", className);



        return toRpcResult(getResult(httpPost));
    }
}
