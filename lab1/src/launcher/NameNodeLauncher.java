package launcher;

import api.NameNode;
import api.NameNodeHelper;
import impl.NameNodeImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

public class NameNodeLauncher {

    public static void main(String[] args) {
        try {

//            Properties properties = new Properties();
//            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
//            properties.put("org.omg.COBRA.ORBInitialPort", "1050");
//
//            // create and initialize the ORB
//            ORB orb = ORB.init(args, properties);

            // for debug only
            args = new String[4];
            args[0] = "-ORBInitialHost";
            args[1] = "127.0.0.1";
            args[2] = "-ORBInitialPort";
            args[3] = "1050";

            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            NameNodeImpl nameNodeServant = new NameNodeImpl();

            // 将服务实现对象交给 POA 管理，并向外暴露接口
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeServant);
            NameNode href = NameNodeHelper.narrow(ref);

            // 命名上下文为 NameService
            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent path[] = ncRef.to_name("NameNode");  // 绑定名字，可以区分不同的launcher
            ncRef.rebind(path, href);

            System.out.println("NameNode is ready and waiting ...");

            // wait for invocations from clients
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("NameNode Exiting ...");
    }
}
