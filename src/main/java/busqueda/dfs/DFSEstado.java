package busqueda.dfs;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import IA.DistFS.Requests;
import IA.DistFS.Servers;

public class DFSEstado {

    // Para cada id de request, id del server que proveera el archivo.
    private final int []servidor;

    private static Servers servers;
    private static Requests requests;
    private static int nserv;

    public static void init(final Servers serv, final Requests req, int nserv) {
        DFSEstado.servers = serv;
        DFSEstado.requests = req;
        DFSEstado.nserv = nserv;
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
        List<Integer> transTime = IntStream.of(new int[DFSEstado.nserv]).boxed().collect(Collectors.toList());
        for (int i=0; i < requests.size(); ++i) {
            // [UserID, FileID]
            final int userID = requests.getRequest(i)[0];
            final int serverID = servidor[i];
            final int time = transTime.get(serverID) + servers.tranmissionTime(serverID, userID);
            // transTime[serverID] +=  servers.tranmissionTime(serverID, userID);
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
