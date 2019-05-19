# dubbo-convert-rest
dubbo to rest



# 在消费者端通过dubbo的protocol扩展以及proxyFactory扩展将所有的dubbo接口的调用（DubboInvoker）转换成http调用（RestInvoker）
# 在提供者端通过统一的http请求入口，将消费者的http请求转换成内部的基于反射的调用

重点：
* protocol扩展

* proxyFactory扩展

* hessian序列化

* ServiceBean
