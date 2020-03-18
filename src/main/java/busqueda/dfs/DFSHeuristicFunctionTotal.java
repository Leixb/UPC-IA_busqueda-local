package busqueda.dfs;

import aima.search.framework.HeuristicFunction;

public class DFSHeuristicFunctionTotal implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object arg0) {
        final DFSEstado estado = (DFSEstado) arg0;
        return estado.getHeuristicValueTotal();
    }

}
