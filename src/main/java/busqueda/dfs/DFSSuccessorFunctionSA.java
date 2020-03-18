package busqueda.dfs;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DFSSuccessorFunctionSA implements SuccessorFunction {

    @Override
    public List<Successor> getSuccessors(Object arg0) {
        List<Successor>       retVal = new ArrayList<Successor>();
        DFSEstado             estado = (DFSEstado) arg0;
        DFSHeuristicFunction  DFSHF  = new DFSHeuristicFunction();
        Random myRandom = new Random();
        int i;
        Integer location = -1;

        i = myRandom.nextInt(estado.size());
        
        int item = new Random().nextInt(estado.locations(i).size());
        int it = 0;
        for (final Integer loc : estado.locations(i)) {
            if (it == item) {
                location = loc;
                break;
            }
            it++;
        }

        DFSEstado newEstado = new DFSEstado(estado.getEstado());
        newEstado.set(i, location);

        final double v = DFSHF.getHeuristicValue(newEstado);
        final String S = String.format("Request: %i Coste(%i) ---> %s", i, v, newEstado.toString());
        retVal.add(new Successor(S, newEstado));

        return retVal;
    }
}
