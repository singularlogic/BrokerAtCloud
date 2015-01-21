package org.broker.orbi.util.openstack;

import java.util.List;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.network.NetFloatingIP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Pool;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;

/**
 *
 * @author pgouvas
 */
public class OpenStackIntegration {

    public static void main(String[] args) {
        OpenStackIntegration osi = new OpenStackIntegration();
        osi.authenticate();
        //osi.destroyInstance("http://192.168.4.201:5000/v2.0", "admin", "!strat3g1c!", "admin", "82632111-1279-4b10-9997-8a1c15930c02");
        //osi.createInstance("http://192.168.4.201:5000/v2.0", "admin", "!strat3g1c!", "admin","OrbiInstance","2","a4fcc126-0a68-40bf-bc83-d09b14b92293","192.168.10.12");        
    }

    public OpenStackIntegration() {

    }

    public boolean destroyInstance(String endpoint, String username, String password, String tenant, String serverid) {

        try {
            OSClient os = OSFactory.builder()
                    .endpoint(endpoint)
                    .credentials(username, password)
                    .tenantName(tenant)
                    .authenticate();
            os.compute().servers().delete(serverid);
            System.out.println("Destroy server with ID: " + serverid + " success!");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Object[] createInstance(String endpoint, String username, String password, String tenant, String instancename, String flavorid, String templateid, String internalip) {
        Object[] returnObject = new Object[2];
        NetFloatingIP chosenfloatip = null;
        String serverid = "";
        try {

            OSClient os = OSFactory.builder()
                    .endpoint(endpoint)
                    .credentials(username, password)
                    .tenantName(tenant)
                    .authenticate();

            //Enable DHCP cabapility
            Port port = os.networking().port().create(Builders.port()
                    .name("port-1")
                    .networkId("38879a94-d60e-4702-ba5b-e5ef935fc045")
                    .build());

            ServerCreate sc = Builders.server()
                    .name(instancename)
                    .flavor(flavorid)
                    .image(templateid)
                    .addSecurityGroup("98ebff13-c074-40d2-a28e-fb367a744cfd") //defaultsecuritygroup        
                    .keypairName("pgouvaslaptop")
                    .addNetworkPort(port.getId())
                    .build();

            Server server = os.compute().servers().boot(sc);
            serverid = server.getId();
            List<? extends NetFloatingIP> floatips = os.networking().floatingip().list();
            for (NetFloatingIP floatip : floatips) {
                System.out.println("floating: " + floatip + " fixed: " + floatip.getFixedIpAddress());
                if (floatip.getFixedIpAddress() == null) {
                    chosenfloatip = floatip;
                    break;
                }
            }
            System.out.println("chosen:" + chosenfloatip.toString());

            Status stat = Status.BUILD;
            while (stat != Status.ACTIVE) {
                stat = os.compute().servers().get(serverid).getStatus();
                System.out.println("stat:" + stat.toString());
            }
            os.compute().floatingIps().addFloatingIP(os.compute().servers().get(serverid), chosenfloatip.getFloatingIpAddress());
            System.out.println("VM Creation success");

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        returnObject[0] = serverid;
        returnObject[1] = chosenfloatip.getFloatingIpAddress();

        return returnObject;
    }//EoM

    private void authenticate() {
        OSClient os = OSFactory.builder()
                .endpoint("http://192.168.4.201:5000/v2.0") //213.249.38.66
                .credentials("admin", "!strat3g1c!")
                .tenantName("admin")
                .authenticate();
        // Find all running Servers
//        List<? extends Server> servers = os.compute().servers().list();
//        System.out.println("servers.size: " + servers.size());
        //get Images
//        List<? extends Image> images = os.compute().images().list();
//        for (Image image : images) {
//            System.out.println("image:" + image.toString());
//        }
//        // Find all Compute Flavors
//        List<? extends Flavor> flavors = os.compute().flavors().list();        
//        for (Flavor flavor : flavors) {
//            System.out.println("flavor: "+flavor.toString());
//        }
//        //Subnets
//        List<? extends Subnet> subnets = os.networking().subnet().list();
//        for (Subnet subnet : subnets) {
//            System.out.println("subnet: "+subnet.toString());
//        }
        //SecurityGroup
//            List<? extends SecGroupExtension> secgroups = os.compute().securityGroups().list();
//            for (SecGroupExtension secGroupExtension : secgroups) {
//                System.out.println("secgroup:"+secGroupExtension.toString());
//            }              
//                        
//        //Ports
//        List<? extends Port> ports = os.networking().port().list();
//        for (Port port : ports) {
//            System.out.println("port: "+port.toString());
//        }
        //Networs
        List<? extends Network> networks = os.networking().network().list();
        for (Network network : networks) {
            System.out.println("network: " + network.toString());
        }
//        String allocated = os.networking().network().get("38879a94-d60e-4702-ba5b-e5ef935fc045").getNeutronSubnets().get(0).getAllocationPools().get(0).getEnd();
//        System.out.println("allocated:"+allocated);
//        //Create-Server

//        List<? extends Port> ports = os.networking().port().list();
//        for (Port port : ports) {
//            System.out.println("port:" + port.toString());
//        }
//        // Get a Port by ID
//        Port dhcpport = os.networking().port().get("aae6e443-a211-44a1-b491-d0e96f747422");
//
//
//    List<? extends Subnet> subnet = os.networking().subnet().list();
//        for (Subnet subnet1 : subnet) {
//            System.out.println("subnet1 "+subnet1.toString());
//        }
//    
//        Subnet subn = os.networking().subnet().get("7113d9ca-7cc9-4dd6-b459-ebf746599bf0");
//        
//        //iterate pools
//        List<? extends Pool> pools = subn.getAllocationPools();
//        for (Pool pool : pools) {
//            System.out.println("pool"+pool);
//        }
//        Pool pool = pools.get(0);
//------------------------------------------------------------        
        NetFloatingIP chosenfloatip = null;
        List<? extends NetFloatingIP> floatips = os.networking().floatingip().list();
        for (NetFloatingIP floatip : floatips) {
            System.out.println("floating: " + floatip + " fixed" + floatip.getFixedIpAddress());
            if (floatip.getFixedIpAddress() == null) {
                chosenfloatip = floatip;
                break;
            }
        }
        System.out.println("chosen:" + chosenfloatip.toString());   //TODO addlogic if no floating ip is available        

        Port port = os.networking().port().create(Builders.port()
                .name("port-1")
                .networkId("38879a94-d60e-4702-ba5b-e5ef935fc045")
                .build());

        ServerCreate sc = Builders.server()
                .name("Cirros2")
                .flavor("1") //tiny
                .image("fe9aee7f-71fb-4e46-9a7d-bbf5e13dd35b") //cirros           
                .addSecurityGroup("98ebff13-c074-40d2-a28e-fb367a744cfd")
                .keypairName("pgouvaslaptop")
                .addNetworkPort(port.getId())
                .build();

        Server server = os.compute().servers().boot(sc);
        String serverid = server.getId();                   //here is the error

//-----        String serverid = os.compute().servers().boot(sc).getId(); //oxi auto giati kanei generate errors
        Status stat = Status.BUILD;
        while (stat != Status.ACTIVE) {
            stat = os.compute().servers().get(serverid).getStatus();
            System.out.println("stat:" + stat.toString());
        }
        os.compute().floatingIps().addFloatingIP(os.compute().servers().get(serverid), chosenfloatip.getFloatingIpAddress());

    }//EoM 

}
