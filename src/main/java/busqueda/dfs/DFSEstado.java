package busqueda.dfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import IA.DistFS.Requests;
import IA.DistFS.Servers;

public class DFSEstado {

    private static Servers servers;
    private static Requests requests;
    private static int nserv;

    // Para cada id de request, id del server que proveera el archivo.
    private final int[] reqServer;
    // Para cada id de request, tiempo que tarda en servir el archivo.
    private final int[] reqTime;
    private int totalTime = 0;

    private Map<Integer, Integer> servTime;

    private int last_orig = -1;
    private int last_new = -1;

    public static void init(final Servers serv, final Requests req, int nserv) {
        DFSEstado.servers = serv;
        DFSEstado.requests = req;
        DFSEstado.nserv = nserv;
    }

    public DFSEstado(boolean findSmallest) {
        // loop through the requests and assign first server.
        reqServer = new int[requests.size()];
        reqTime = new int[requests.size()];
        servTime = new HashMap<Integer, Integer>(DFSEstado.nserv);

        for (int i = 0; i < requests.size(); ++i) {
            // [UserID, FileID]
            final int[] req = requests.getRequest(i);
            final int userID = req[0];
            final int fileID = req[1];

            Iterator<Integer> it = servers.fileLocations(fileID).iterator();

            // Assign first server of the set.
            if (!it.hasNext()) {
                throw new RuntimeException("No file locations for given fileID");
            }
            reqServer[i] = it.next(); // serverID

            // If findSmallest is true, find the server with the smallest trans time to the user
            if (findSmallest) {
                int mn = servers.tranmissionTime(reqServer[i], userID);
                while (it.hasNext()) { // Loop through all the servers that have fileID
                    final int serverID = it.next();
                    final int transTime = servers.tranmissionTime(serverID, userID);
                    if (transTime < mn) {
                        mn = transTime;
                        reqServer[i] = serverID;
                    }
                }
            }
            reqTime[i] = servers.tranmissionTime(reqServer[i], userID);
            final int svTime = servTime.getOrDefault(reqServer[i], 0) + reqTime[i];
            servTime.put(reqServer[i], svTime);
            totalTime += reqTime[i];
        }
    }

    public DFSEstado(
            final int[] rqServer, int[] rqTime, Map<Integer, Integer> svTime, int tiempoTotal) {
        // Copiamos el array
        this.reqServer = Arrays.copyOf(rqServer, rqServer.length);
        this.reqTime = Arrays.copyOf(rqTime, rqTime.length);
        this.servTime = new HashMap<Integer, Integer>(svTime);

        this.totalTime = tiempoTotal;
    }

    // Maximo transmission total de los servidores
    public int getHeuristicValueMax() {
        return Collections.max(servTime.values());
    }

    // Suma total de transmission total de los servidores con penalizacion por
    // cargas muy distintas
    public double getHeuristicValueTotal() {
        final double mean = totalTime / servers.size();

        double sd = 0.0; // standard deviation
        for (int time : servTime.values()) {
            sd += Math.pow(mean - time, 2);
        }
        sd = Math.sqrt(sd / servers.size());

        return totalTime * sd; // TODO: mejorar como hacer esta heuristica
    }

    public int totalTime() {
        return totalTime;
    }

    // changes server that gives file for ith request.
    public void set(final int i, final int serv) {
        last_orig = reqServer[i];
        last_new = serv;

        final int userID = requests.getRequest(i)[0];

        final int rqTimeOld = reqTime[i];
        final int rqTimeNew = servers.tranmissionTime(serv, userID);

        final int svTimeNew = servTime.getOrDefault(serv, 0) + rqTimeNew;
        final int svTimeOld = servTime.get(reqServer[i]) - rqTimeOld;
        servTime.put(serv, svTimeNew);
        servTime.put(reqServer[i], svTimeOld);

        totalTime += rqTimeNew - rqTimeOld;

        reqTime[i] = rqTimeNew;
        reqServer[i] = serv;
    }

    // All posible file locations for request i different from current one.
    public Set<Integer> locations(final int i) {
        final int[] req = requests.getRequest(i);

        final Set<Integer> locations = servers.fileLocations(req[1]);
        locations.remove(reqServer[i]);

        return locations;
    }

    /* funcions auxiliars */

    public int size() {
        return reqServer.length; // == requests.size()
    }

    public int[] getEstado() {
        return reqServer.clone();
    }

    public int[] getTransmissionTimes() {
        return reqTime.clone();
    }

    public Map<Integer, Integer> getServerTimes() {
        return servTime;
    }

    @Override
    public String toString() {
        return Arrays.toString(reqServer);
    }

    public String lastChangeString() {
        return String.format("%d --> %d", last_orig, last_new);
    }
}
