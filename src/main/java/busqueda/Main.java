package busqueda;

import busqueda.dfs.*;

import IA.DistFS.Requests;
import IA.DistFS.Servers;
import IA.DistFS.Servers.WrongParametersException;

public class Main {

    public static void main(String[] args) {

        int nserv = 10,
            nrep = 5,
            users = 20,
            requests = 15,
            seeds = 1234, // TODO: Proper seeding
            seedr = 4321;

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
