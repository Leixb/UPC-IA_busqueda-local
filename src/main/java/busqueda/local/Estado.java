package busqueda.local;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import IA.DistFS.Requests;
import IA.DistFS.Servers;

public class Estado {

    // Para cada id de request, id del server que proveera el archivo.
    private final int []servidor;

    private static Servers servers;
    private static Requests requests;

    public static void init(final Servers serv, final Requests req) {
        Estado.servers = serv;
        Estado.requests = req;
    }

    public Estado() {
        // loop through the requests and assign first server.
        servidor = new int[requests.size()];
        for (int i=0; i < requests.size(); ++i) {
            // [UserID, FileID]
            final int[] req = requests.getRequest(i);
            final Set<java.lang.Integer> locations = servers.fileLocations(req[1]);

            // Assign first server of the set. (Assumes not empty set)
            servidor[i] = locations.iterator().next(); // serverID

        }
    }

    public Estado(final int[] estado)  {
        // Copiamos el array
        this.servidor = Arrays.copyOf(estado, estado.length);
    }

    // changes server that gives file for ith request.
    // returns false if it fails
    // makes no real sense
    public boolean change(final int i) {
        // [UserID, FileID]
        final int[] req = requests.getRequest(i);

        final Set<Integer> locations = servers.fileLocations(req[1]);

        final Iterator<Integer> it = locations.iterator(); // serverID

        while (it.hasNext()) {
            final int serv = it.next();
            if (serv != servidor[i]) {
                servidor[i] = serv;
                return true;
            }
        }

        return false;
    }

    /* funcions auxiliars */

    public int size() {
        return servidor.length; // == requests.size()
    }

    public int[] getEstado() {
        return servidor.clone();
    }

}
