package com.heartmove.rest;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
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

@Slf4j
public class RestInvoker<T>  extends AbstractInvoker<T> {


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

        String className = this.getUrl().getParameter(Constants.INTERFACE_KEY);
        //接口版本
        String version = this.getUrl().getParameter(Constants.VERSION_KEY);
        //接口分组
        String group = this.getUrl().getParameter(Constants.GROUP_KEY);

        RpcResult result = new RpcResult();
        try {
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
            HttpPost httpPost = new HttpPost("http://localhost:8080/"+ group +"/"+ version+"/invoke");
            httpPost.setEntity(new ByteArrayEntity(buffer));
            httpPost.setHeader("className", className);
            result = toRpcResult(getResult(httpPost));
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
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                return EntityUtils.toByteArray(entity);
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
}
