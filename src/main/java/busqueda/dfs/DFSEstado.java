package busqueda.dfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

    // Maximo transmission total de los servidores
    public int getHeuristicValueMax() {
        //return Collections.max(transmissionTimes());
        return transmissionTimes().stream().reduce(0, Integer::max);
    }

    private List<Integer> transmissionTimes() {
        List<Integer> transTime = new ArrayList<Integer>(servers.size());
        for (int i=0; i < requests.size(); ++i) {
            // [UserID, FileID]
            final int userID = requests.getRequest(i)[0];
            final int serverID = servidor[i];
            final int time = transTime.get(i) + servers.tranmissionTime(serverID, userID);
            transTime.set(serverID, time);
        }
        return transTime;
    }


    // Suma total de transmission total de los servidores con penalizacion por
    // cargas muy distintas
    public double getHeuristicValueTotal() {
        List<Integer> transTimes = transmissionTimes();

        final int totalTime = transTimes.stream().reduce(0, Integer::sum);

        final double mean = totalTime/servers.size();

        double sd = 0.0; // standard deviation
        for (Integer time : transTimes) {
            sd += Math.pow(mean - time, 2);
        }
        sd = Math.sqrt(sd/servers.size());

        return totalTime*sd; //TODO: mejorar como hacer esta heuristica
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
