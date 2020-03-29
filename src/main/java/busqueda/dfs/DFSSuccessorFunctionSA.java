package busqueda.dfs;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DFSSuccessorFunctionSA implements SuccessorFunction {

    @Override
    public List<Successor> getSuccessors(Object arg0) {
        List<Successor> retVal = new ArrayList<Successor>();
        DFSEstado estado = (DFSEstado) arg0;
        DFSHeuristicFunction DFSHF = new DFSHeuristicFunction();

        Random rand = new Random();

        Integer location = -1;
        int i;

        Set<Integer> locations;
        int max_it = 1000; // prevent infinite loop
        do {
            i = rand.nextInt(estado.size());
            locations = estado.locations(i);
            if (--max_it <= 0) throw new RuntimeException("cannot find successor to change");
        } while (locations.isEmpty());

        int item = rand.nextInt(locations.size());
        int it = 0;
        for (final Integer loc : locations) {
            if (it == item) {
                location = loc;
                break;
            }
            it++;
        }

        DFSEstado newEstado =
                new DFSEstado(
                        estado.getEstado(),
                        estado.getTransmissionTimes(),
                        estado.getServerTimes(),
                        estado.totalTime());
        newEstado.set(i, location);

        final double v = DFSHF.getHeuristicValue(newEstado);
        final String S = String.format("Request: %d Coste(%f) ---> %s", i, v, newEstado.toString());
        retVal.add(new Successor(S, newEstado));

        return retVal;
    }
}
