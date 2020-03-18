package busqueda.dfs;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import IA.DistFS.Requests;
import IA.DistFS.Servers;

public class DFSEstado {

    // Para cada id de request, id del server que proveera el archivo.
    private final int []servidor;

    private static Servers servers;
    private static Requests requests;

    public static void init(final Servers serv, final Requests req) {
        DFSEstado.servers = serv;
        DFSEstado.requests = req;
    }

    public DFSEstado() {
        // loop through the requests and assign first server.
        servidor = new int[requests.size()];
        for (int i=0; i < requests.size(); ++i) {
            // [UserID, FileID]
            final int[] req = requests.getRequest(i);
            final Set<Integer> locations = servers.fileLocations(req[1]);

            // Assign first server of the set. (Assumes not empty set)
            servidor[i] = locations.iterator().next(); // serverID

        }
    }

    public DFSEstado(final int[] estado)  {
        // Copiamos el array
        this.servidor = Arrays.copyOf(estado, estado.length);
    }

    // changes server that gives file for ith request.
    public void set(final int i, final int serv) {
        // [UserID, FileID]
        servidor[i] = serv;
    }

    // All posible file locations for request i different from current one.
    public Set<Integer> locations(final int i) {
        final int[] req = requests.getRequest(i);

        final Set<Integer> locations = servers.fileLocations(req[1]);
        locations.remove(servidor[i]);

        return locations;
    }

    /* funcions auxiliars */

    public int size() {
        return servidor.length; // == requests.size()
    }

    public int[] getEstado() {
        return servidor.clone();
    }

}
