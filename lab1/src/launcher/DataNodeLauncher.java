package launcher;

import api.DataNode;
import api.DataNodeHelper;
import api.NameNode;
import api.NameNodeHelper;
import impl.DataNodeImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.Properties;

public class DataNodeLauncher {

    public static void main(String[] args) {

        DataNodeImpl dataNodeServant = null;

        try {
//            Properties properties = new Properties();
//            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
//            properties.put("org.omg.COBRA.ORBInitialPort", "1050");
//            ORB orb = ORB.init(args, properties);

            // for debug only
//            args = new String[4];
//            args[0] = "-ORBInitialHost";
//            args[1] = "127.0.0.1";
//            args[2] = "-ORBInitialPort";
//            args[3] = "1050";

            //            ORB orb = ORB.init(args, null);

            Properties properties = new Properties();
            String orbInitialHost = "127.0.0.1";
            String orbInitialPort = "1050";
            long id = 0;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-ORBInitialHost")) {
                    orbInitialHost = args[i + 1];
                } else if (args[i].equals("-ORBInitialPort")) {
                    orbInitialPort = args[i + 1];
                } else if (args[i].equals("-id")){
                    id = Long.valueOf(args[i+1]);
                }
            }
            properties.put("org.omg.CORBA.ORBInitialHost", orbInitialHost);  //ORB IP
            properties.put("org.omg.CORBA.ORBInitialPort", orbInitialPort);       //ORB port
            ORB orb = ORB.init(args, properties);


            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            dataNodeServant = new DataNodeImpl(id);

            Object ref = rootPOA.servant_to_reference(dataNodeServant);
            DataNode href = DataNodeHelper.narrow(ref);

            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] path = ncRef.to_name("DataNode"+dataNodeServant.getId());
            ncRef.rebind(path,href);

            System.out.println("DataNode"+dataNodeServant.getId()+" is ready and start...");

            NameNode nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("Register to nameNode.");
            nameNode.registerDataNode((int)dataNodeServant.getId());


            orb.run();

        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("DataNode"+dataNodeServant.getId()+" Exiting...");
    }
}
