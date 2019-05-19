# dubbo-convert-rest
该项目时将dubbo协议的接口调用转变成了http接口调用，不用检测提供者是否存在，可以直接通过公网调用


重点：
* proxyFactory扩展  
dubbo在容器启动时当解析到dubbo:reference 标签时，会生成ReferenceBean，针对于其proxy属性会设置其代理类，默认会使用JavassistProxyFactory。在后续的消费者调用过程中会通过这个代理类生成对应InvocationHandler，再得到对应的Invoker。通过对这里的扩展，我们屏蔽了原有的InvocationHandler会检测是否有对应的provider注册，并直接通过protocol扩展发起了http调用。

* protocol扩展  
RPC协议扩展，封装了远程调用细节，在本项目中通过这里将调用细节转变成了HTTP调用。

* hessian序列化  
将http的请求参数以及相应结果进行hessian序列化。

* ServiceBean  
ServiceBean包含了dubbo:service 标签相关信息，通过这个类可以在spring上下文找到该应用注册的所有的dubbo服务以及信息。
