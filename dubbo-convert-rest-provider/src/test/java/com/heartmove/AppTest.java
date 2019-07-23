package com.heartmove;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }


    @Test
    public void test() throws Exception{
        Request request = Request.Post("https://uc.belle.net.cn/main_index");//.connectTimeout(10).socketTimeout(10);
        System.out.println(Executor.newInstance().execute(request).returnContent().asString());
    }
}
