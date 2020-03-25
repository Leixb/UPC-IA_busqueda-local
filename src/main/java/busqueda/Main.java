package busqueda;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.plaf.synth.SynthStyle;

import IA.DistFS.Requests;
import IA.DistFS.Servers;
import IA.DistFS.Servers.WrongParametersException;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import busqueda.dfs.DFSEstado;
import busqueda.dfs.DFSGoalTest;
import busqueda.dfs.DFSHeuristicFunction;
import busqueda.dfs.DFSHeuristicFunctionMax;
import busqueda.dfs.DFSHeuristicFunctionTotal;
import busqueda.dfs.DFSSuccessorFunction;
import busqueda.dfs.DFSSuccessorFunctionSA;

public class Main {

    private static Servers serv;
    private static Requests req;

    public static void main(String[] args) {

        Random rand = new Random();

        int nserv = 50,
                nrep = 5,
                users = 200,
                requests = 5,
                seeds = rand.nextInt(),
                seedr = rand.nextInt();

        String algorithm = "ALL"; // ALL / HC / SA
        String heuristic = "Max"; // Max / Total
        int generador = 0; // 0 is normal, 1 is optimized

        // Parse arguments
        if (args.length > 0) {
            algorithm = args[0];
        }
        if (args.length > 1) {
            heuristic = args[1];
        }
        if (args.length > 2) {
            generador = Integer.parseInt(args[2]);
        }
        if (args.length > 3) {
            nserv = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            nrep = Integer.parseInt(args[4]);
        }
        if (args.length > 5) {
            users = Integer.parseInt(args[5]);
        }
        if (args.length > 6) {
            requests = Integer.parseInt(args[6]);
        }
        if (args.length > 7) {
            if (!args[7].equals("rand")) {
                seeds = Integer.parseInt(args[7]);
                seedr = seeds;
            }
        }

        System.out.println("Configuracion");
        System.out.printf(
                "algo = %s\nheu = %s\n"
                        + "findSmallest = %b\nnserv = %d\nnrep = %d\nusers = %d\nrequests = %d\n"
                        + "seeds = %d\nseedr = %d\n",
                algorithm, heuristic,
                generador == 1, nserv, nrep, users, requests, seeds, seedr);

        req = new Requests(users, requests, seedr);
        try {
            serv = new Servers(nserv, nrep, seeds);
        } catch (WrongParametersException e) {
            e.printStackTrace();
        }
        DFSEstado.init(serv, req, nserv);

        System.out.printf("num requests: %d\n", req.size());

        // Set generador
        final boolean findSmallest = (generador == 1);
        DFSEstado estado = new DFSEstado(findSmallest);

        printSep();
        System.out.println("Estado inicial");
        printInfo(estado); // Muestra heurisiticas para estado inicial
        printSep();

        // Set heuristic function
        if (heuristic.equals("Max")) {
            DFSHeuristicFunction.setHeurisitcFunction(new DFSHeuristicFunctionMax());
        } else if (heuristic.equals("Total")) {
            DFSHeuristicFunction.setHeurisitcFunction(new DFSHeuristicFunctionTotal());
        }

        if (algorithm.equals("HC") || algorithm.equals("ALL")) {
            DFSHillClimbingSearch(estado);
        }

        final int steps = 2000, stiter = 100, k = 5;
        final double lamb = 0.001;

        if (algorithm.equals("SA") || algorithm.equals("ALL")) {
            DFSHillSimulatedAnnealing(estado, steps, stiter, k, lamb);
        }
    }

    private static void DFSHillClimbingSearch(DFSEstado estado) {
        System.out.println("\nDistFS HillClimbing --> ");
        try {
            Problem problem =
                    new Problem(
                            estado,
                            new DFSSuccessorFunction(),
                            new DFSGoalTest(),
                            new DFSHeuristicFunction());
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);

            System.out.println();

            printActions(agent.getActions());
            printInfo((DFSEstado) search.getGoalState());
            printInstrumentation(agent.getInstrumentation());
            printSep();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void DFSHillSimulatedAnnealing(
            DFSEstado estado, int steps, int stiter, int k, double lamb) {
        System.out.println("\nDistFS Simulated Annealing --> ");
        try {
            Problem problem =
                    new Problem(
                            estado,
                            new DFSSuccessorFunctionSA(),
                            new DFSGoalTest(),
                            new DFSHeuristicFunction());
            Search search = new SimulatedAnnealingSearch(steps, stiter, k, lamb);
            SearchAgent agent = new SearchAgent(problem, search);

            System.out.println();

            // printActions(agent.getActions());
            printInfo((DFSEstado) search.getGoalState());
            printInstrumentation(agent.getInstrumentation());
            printSep();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printInfo(DFSEstado estado) {
        System.out.printf("Estado:\n%s\n", estado.toString());
        System.out.printf("Coste heurisica Max: %d\n", estado.getHeuristicValueMax());
        System.out.printf("Coste heurisica Total: %f\n", estado.getHeuristicValueTotal());
        System.out.printf("Tiempo total de transmission: %d\n", estado.totalTime());
        if (System.getenv("DEBUG") != null) printDebugEstado(estado);
    }

    private static void printDebugEstado(DFSEstado estado) {
        int []serverIDs = estado.getEstado();
        System.out.printf("%s\t%s\t%s\t%s\t%s\n", "request", "user", "file", "server", "time");
        for (int i = 0; i < serverIDs.length; i++) {
            final int serverID = serverIDs[i];
            final int[] request = req.getRequest(i);
            final int userID = request[0];
            final int fileID = request[1];

            System.out.printf("%d\t%d\t%d\t%d\t%d\n", i, userID, fileID, serverID, serv.tranmissionTime(serverID, userID));
        }
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }

    private static void printActions(List actions) {
        for (Object action : actions) {
            System.out.println(action.toString());
        }
    }

    private static void printSep() {
        System.out.println(
                "--------------------------------------------------------------------------------");
    }
}
