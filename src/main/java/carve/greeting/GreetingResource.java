package carve.greeting;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

@Path("/greeting")
public class GreetingResource {

    // TODO this is not called before first request!
    static {
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
                    .address("localhost").port(port).name("worker").build();
            ServiceDiscoveryBuilder.builder(Object.class)
                    .basePath("load-balancing-example")
                    .client(curatorFramework).thisInstance(serviceInstance)
                    .build().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Curator reg failed");
        }
    }

    @GET
    @Path("/")
    @Produces("text/plain")
    public String world() {
        return "hello world";
    }
}
