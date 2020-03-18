package busqueda;

import busqueda.dfs.*;

import java.util.Random;

import IA.DistFS.Requests;
import IA.DistFS.Servers;
import IA.DistFS.Servers.WrongParametersException;

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

            DFSEstado.init(serv, req);
		} catch (WrongParametersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        DFSEstado estado = new DFSEstado();
    }
}
