package com.heartmove.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;

public class MyRegistryFactory  implements RegistryFactory {

    @Override
    public Registry getRegistry(URL url) {
        return new MyRegistry();
    }
}
