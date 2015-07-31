package carve.greeting;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

@Singleton
@Startup
public class ServiceRegistrarStartupBean {

    @Resource
    TimerService timerService;

    ServiceDiscovery<Object> serviceDiscovery;

    @PostConstruct
    public void init() {
        timerService.createTimer(2000, "Startup service registrar timer");
    }

    @PreDestroy
    public void cleanup() {
        try {
            serviceDiscovery.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Timeout
    public void register(Timer timer) {
        try {
            int port = (int) ManagementFactory
                    .getPlatformMBeanServer()
                    .getAttribute(
                            new ObjectName(
                                    "jboss.as:socket-binding-group=standard-sockets,socket-binding=http"),
                            "boundPort");
            System.out.println("HTTP port: " + port);

            CuratorFramework curatorFramework = CuratorFrameworkFactory
                    .newClient("localhost:2181", new RetryNTimes(5, 1000));
            curatorFramework.start();
            // TODO make "greeting" injectable
            ServiceInstance<Object> serviceInstance = ServiceInstance.builder()
                    .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                    .address("localhost").port(port).name("greeting").build();
            serviceDiscovery = ServiceDiscoveryBuilder.builder(Object.class)
                    .basePath("carve")
                    .client(curatorFramework).thisInstance(serviceInstance)
                    .build();
            serviceDiscovery.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Curator reg failed");
            // try again
            timerService.createTimer(2000, "Startup service registrar timer");
        }
        System.out.println("Service registered in Curator/Zookeeper");
    }
}
