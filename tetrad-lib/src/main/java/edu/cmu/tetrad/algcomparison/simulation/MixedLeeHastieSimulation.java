package edu.cmu.tetrad.algcomparison.simulation;

import edu.cmu.tetrad.algcomparison.DataType;
import edu.cmu.tetrad.algcomparison.Parameters;
import edu.cmu.tetrad.algcomparison.Simulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.GeneralizedSemIm;
import edu.cmu.tetrad.sem.GeneralizedSemPm;
import edu.pitt.csb.mgm.MixedUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jdramsey on 6/4/16.
 */
public class MixedLeeHastieSimulation implements Simulation {
    private Graph dag;
    private DataSet dataSet;
    private int numDataSets = 0;

    public MixedLeeHastieSimulation(int numDataSets) {
        this.numDataSets = numDataSets;
    }

    public DataSet getDataSet(int index, Parameters parameters) {
        this.dag = GraphUtils.randomGraphRandomForwardEdges(
                parameters.getInt("numMeasures"), parameters.getInt("numLatents"),
                parameters.getInt("numEdges"),
                parameters.getInt("maxDegree"),
                parameters.getInt("maxIndegree"),
                parameters.getInt("maxOutdegree"),
                parameters.getInt("connected") == 1);

        HashMap<String, Integer> nd = new HashMap<>();

        List<Node> nodes = dag.getNodes();

        Collections.shuffle(nodes);

        for (int i = 0; i < nodes.size(); i++) {
            if (i < nodes.size() * parameters.getDouble("percentDiscreteForMixedSimulation") * 0.01) {
                nd.put(nodes.get(i).getName(), parameters.getInt("numCategories"));
            } else {
                nd.put(nodes.get(i).getName(), 0);
            }
        }

        Graph graph = MixedUtils.makeMixedGraph(dag, nd);

        GeneralizedSemPm pm = MixedUtils.GaussianCategoricalPm(graph, "Split(-1.5,-.5,.5,1.5)");
        GeneralizedSemIm im = MixedUtils.GaussianCategoricalIm(pm);

        DataSet ds = im.simulateDataAvoidInfinity(parameters.getInt("sampleSize"), false);
        this.dataSet = MixedUtils.makeMixedData(ds, nd);

        return this.dataSet;
    }

    @Override
    public Graph getTrueGraph() {
        return dag;
    }

    public DataSet getData() {
        return dataSet;
    }

    public String getDescription() {
        return "Lee & Hastie simulation";
    }

    @Override
    public int getNumDataSets() {
        return numDataSets;
    }

    @Override
    public DataType getDataType(Parameters parameters) {
        double percent = parameters.getDouble("percentDiscreteForMixedSimulation");

        if (percent == 0) {
            return DataType.Continuous;
        } else if (percent == 100) {
            return DataType.Discrete;
        } else {
            return DataType.Mixed;
        }
    }
}
