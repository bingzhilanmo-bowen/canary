package com.lanmo.canary.client.route;

import com.lanmo.canary.register.Provider;
import com.lanmo.canary.register.RouteHandle;
import com.lanmo.canary.register.zookeeper.ZooKeeperRouteHandler;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by liuj on 2016/11/7.
 */
public class ZooKeeperClientHandler extends ZooKeeperRouteHandler implements RouteHandle {

    private final ConcurrentMap<String, List<Provider>> allProviders = new ConcurrentHashMap<String, List<Provider>>();

    public ZooKeeperClientHandler(String root, String serverString, int timeout) {
        super(root, serverString, timeout);
    }


    public void subscribeInterface(String interfaceId, String alisa) {
        final String holePath = getDataPath(interfaceId, alisa);
        //监控child改变
        zkClient.subscribeChildChanges(holePath, new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                List<Provider> providerList = getZKData(holePath,currentChilds);
                if (CollectionUtils.isNotEmpty(providerList)) {
                    changeProviders(providerList, holePath);
                    logger.debug("find new provides,change local cache.path:{},size:{}", holePath, providerList.size());
                } else {
                    logger.warn("can not find valid providers, cache not change");
                }
            }
        });
    }

    private void changeProviders(List<Provider> providerList, String holePath) {
        allProviders.put(holePath, providerList);
    }


    public List<Provider> route(String interfaceId, String alisa) {
        String dataPath = getDataPath(interfaceId, alisa);

        List<Provider> providerList = allProviders.get(dataPath);
        if (CollectionUtils.isNotEmpty(providerList)) {
            return providerList;
        }

        try {
            List<String> providerStrList = zkClient.getChildren(dataPath);
            List<Provider> providers = getZKData(dataPath, providerStrList);
            if(CollectionUtils.isNotEmpty(providerList)){
                changeProviders(providers,dataPath);
            }
            return providers;
        }catch (Exception e){
            logger.error("获取path子节点异常", e);
        }
        return null;
    }

}
