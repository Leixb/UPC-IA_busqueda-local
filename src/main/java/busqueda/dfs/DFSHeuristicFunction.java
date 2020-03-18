package busqueda.dfs;

import aima.search.framework.HeuristicFunction;

public class DFSHeuristicFunction implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object arg0) {
        return new DFSHeuristicFunctionMax().getHeuristicValue(arg0);
        //return new DFSHeuristicFunctionTotal().getHeuristicValue(arg0);
    }

}
