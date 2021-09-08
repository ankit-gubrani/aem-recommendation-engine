package in.co.codebrains.recommendations.engine.similarity;

import org.apache.commons.math3.linear.RealMatrix;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Model class which contains the DotProductMatrix & NodeIdIndexMap.
 * Recommendations are generated in the form of SimilarityMatrix and is later used while reading the recommendations.
 */
public class SimilarityMatrix  implements Serializable {

    private RealMatrix dotProductMatrix;
    private LinkedHashMap<String, Integer> nodeIdIndexMap;

    public SimilarityMatrix(RealMatrix dotProductMatrix, LinkedHashMap<String, Integer> nodeIdIndexMap) {
        this.dotProductMatrix = dotProductMatrix;
        this.nodeIdIndexMap = nodeIdIndexMap;
    }

    public RealMatrix getDotProductMatrix() {
        return dotProductMatrix;
    }

    public LinkedHashMap<String, Integer> getNodeIdIndexMap() {
        return nodeIdIndexMap;
    }
}
