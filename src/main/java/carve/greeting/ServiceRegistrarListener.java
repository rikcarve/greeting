package carve.greeting;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

@WebListener
public class ServiceRegistrarListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            int port = (int) ManagementFactory
                    .getPlatformMBeanServer()
                    .getAttribute(
                            new ObjectName(
                                    "jboss.as:socket-binding-group=standard-sockets,socket-binding=http"),
                            "boundPort");
            System.out.println(port);

            CuratorFramework curatorFramework = CuratorFrameworkFactory
                    .newClient("localhost:2181", new RetryNTimes(5, 1000));
            curatorFramework.start();
            ServiceInstance<Object> serviceInstance = ServiceInstance.builder()
                    .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                    .address("localhost").port(port).name("greeting").build();
            ServiceDiscoveryBuilder.builder(Object.class).basePath("carve")
                    .client(curatorFramework).thisInstance(serviceInstance)
                    .build().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Curator reg failed");
        }

    }

}