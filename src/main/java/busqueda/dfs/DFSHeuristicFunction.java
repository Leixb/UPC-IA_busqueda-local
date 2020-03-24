package busqueda.dfs;

import aima.search.framework.HeuristicFunction;

public class DFSHeuristicFunction implements HeuristicFunction {

    static HeuristicFunction func = new DFSHeuristicFunctionMax(); // Default to Max

    public static void setHeurisitcFunction(HeuristicFunction f) {
        func = f;
    }

    @Override
    public double getHeuristicValue(Object arg0) {
        return func.getHeuristicValue(arg0);
    }
}
