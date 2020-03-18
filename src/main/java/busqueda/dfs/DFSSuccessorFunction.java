package busqueda.dfs;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class DFSSuccessorFunction implements SuccessorFunction {

    @Override
    public List<Successor> getSuccessors(Object arg0) {
        List<Successor>       retVal = new ArrayList<Successor>();
        DFSEstado             estado = (DFSEstado) arg0;
        DFSHeuristicFunction  DFSHF  = new DFSHeuristicFunction();

        for (int i = 0; i < estado.size(); ++i) {
            for (final Integer loc : estado.locations(i)) {
                DFSEstado newEstado = new DFSEstado(estado.getEstado());

                newEstado.set(i, loc);

                final double v = DFSHF.getHeuristicValue(newEstado);
                final String S = String.format("Request: %d Coste(%f) ---> %s", i, v, newEstado.toString());
                retVal.add(new Successor(S, newEstado));
            }
        }


        return retVal;
    }

}
