package busqueda;

import busqueda.dfs.*;

import java.util.Random;
import java.util.List;
import java.util.Properties;

import IA.DistFS.Requests;
import IA.DistFS.Servers;
import IA.DistFS.Servers.WrongParametersException;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;

public class Main {

    public static void main(String[] args) {

        Random rand = new Random();

        int nserv = 10,
            nrep = 5,
            users = 20,
            requests = 15,
            seeds = rand.nextInt(),
            seedr = rand.nextInt();

        Servers serv;
		try {
			serv = new Servers(nserv, nrep, seeds);
            Requests req = new Requests(users, requests, seedr);

            DFSEstado.init(serv, req, nserv);
		} catch (WrongParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        DFSEstado estado = new DFSEstado();

        try {
            Problem problem = new Problem(estado, new DFSSuccessorFunction(),
                    new DFSGoalTest(), new DFSHeuristicFunctionMax());
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);

            System.out.println();

            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void printInstrumentation(Properties properties) {
        //TODO
    }
    
    private static void printActions(List actions) {
        for (Object action : actions) {
            System.out.println((String)action);
        }
    }
    
}
