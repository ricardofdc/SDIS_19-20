import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIinterface extends Remote {

    boolean backup(String fileName, int replicationDegree) throws RemoteException;
    boolean restore(String fileName) throws RemoteException;
    boolean delete(String fileName) throws RemoteException;
    boolean reclaim(int space) throws RemoteException;
    boolean state() throws RemoteException;
}
