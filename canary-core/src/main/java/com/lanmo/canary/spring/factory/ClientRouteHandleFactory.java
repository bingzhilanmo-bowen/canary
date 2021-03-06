package com.lanmo.canary.spring.factory;

import com.lanmo.canary.client.Client;
import com.lanmo.canary.client.route.ZooKeeperClientHandler;
import com.lanmo.canary.register.RouteHandle;
import com.lanmo.canary.spring.api.ReferenceConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bowen on 2017/2/23.
 */
public class ClientRouteHandleFactory {

    private static Logger logger= LoggerFactory.getLogger(ClientRouteHandleFactory.class);

    private static RouteHandle clientHandle =null;
    private static volatile boolean initialized=false;
    private static Integer serverPort=null;

    private static String PROTOCOL_ZK="zookeeper",
            PROTOCOL_REDIS="redis";

    /**
     * 初始化链接
     * @param protocol
     * @param address
     * @param root
     * @param timeout
     * @param port
     */
    public static synchronized void initClient(String protocol,String address,String root,Integer timeout,Integer port){

        if(initialized){
            return;
        }
        if(PROTOCOL_ZK.equals(protocol)){
            logger.info("init zookeeper client ip is {},root is {},timeout is {},server port is {}",address,root,timeout,port);
            clientHandle=new ZooKeeperClientHandler(root,address,timeout);
            serverPort=port;
            initialized=true;
        }else {
            throw  new IllegalArgumentException("now canary only support register by zookeeper");
        }
    }

    public static synchronized Boolean haveInit(){
        return  initialized;
    }

    /**
     *
     * @return
     */
    public static synchronized Client getDefaultClient(ReferenceConfig referenceConfig){
        if(initialized){
            Client client = new Client(referenceConfig, clientHandle);
            return client;
        } else{
            throw new IllegalArgumentException("canary clientHandle not init");
        }
    }

}
