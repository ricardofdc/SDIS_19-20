import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp{

    private static void errorPrintUsage(){
        System.err.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ");
        System.err.println("<opnd 2> only applies to the BACKUP protocol");
        System.err.println("STATE protocol takes no operands");
    }

    public static void main(String[] args) {

        if(args.length < 2 || args.length > 4){
            errorPrintUsage();
            return;
        }

        String[] peer_ap = args[0].split(":");
        String peer_ap_name = peer_ap[0];
        int peer_ap_port = Integer.parseInt(peer_ap[1]);
        String operation = args[1];
        String filename;

        Registry reg;
        RMIinterface peer;

        try {

            reg = LocateRegistry.getRegistry(peer_ap_port);

            peer = (RMIinterface) reg.lookup(peer_ap_name);


            switch (operation){
                case "BACKUP":
                    if (args.length != 4) {
                        errorPrintUsage();
                        return;
                    }

                    filename = args[2];
                    int rep_degree = Integer.parseInt(args[3]);
                    if(peer.backup(filename, rep_degree)){
                        System.out.println("BACKUP in progress.");
                    }
                    else System.out.println("File already exists, try to delete it first if you really want to backup again.");
                    break;
                case "RESTORE":
                    if (args.length != 3) {
                        errorPrintUsage();
                        return;
                    }

                    filename = args[2];
                    if(peer.restore(filename))
                        System.out.println("RESTORE in progress.");
                    else System.out.println("Sorry we could'n find " + filename + ".");

                    break;
                case "DELETE":
                    if (args.length != 3) {
                        errorPrintUsage();
                        return;
                    }
                    System.out.println("DELETE");

                    filename = args[2];
                    if(peer.delete(filename))
                        System.out.println("DELETE in progress.");
                    else System.out.println("Sorry, file " + filename + " not found.");

                    break;
                case "RECLAIM":
                    if (args.length != 3) {
                        errorPrintUsage();
                        return;
                    }
                    System.out.println("RECLAIM");

                    int max_disk_space = Integer.parseInt(args[2]);
                    peer.reclaim(max_disk_space);

                    break;
                case "STATE":
                    if (args.length != 2) {
                        errorPrintUsage();
                        return;
                    }
                    System.out.println("STATE");

                    peer.state();

                    break;
                default:
                    errorPrintUsage();
                    System.err.println("<sub_protocol> must be one of the following: BACKUP, RESTORE, DELETE, RECLAIM or STATE.");
                    return;
            }

        } catch (RemoteException | NotBoundException e) {
            System.err.println("TestApp error: peer_ap does not exist. Try whit a different one.");
            //e.printStackTrace();
        }


    }
}