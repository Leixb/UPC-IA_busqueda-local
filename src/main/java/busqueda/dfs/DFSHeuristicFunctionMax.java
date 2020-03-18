package busqueda.dfs;

import aima.search.framework.HeuristicFunction;

public class DFSHeuristicFunctionMax implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object arg0) {
        final DFSEstado estado = (DFSEstado) arg0;
        return estado.getHeuristicValueMax();
    }

}
