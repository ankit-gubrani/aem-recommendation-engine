package in.co.codebrains.recommendations.engine.similarity;

import in.co.codebrains.recommendations.engine.extraction.CountVectorizer;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTermDictionary;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTokenizer;
import in.co.codebrains.recommendations.engine.extraction.util.Tokenizer;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimilarityMatrixGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityMatrixGenerator.class);

    /**
     * Method generating the DotProductMatrix & Map storing NodeId to index map for retriving the data later
     * from DotProductMatrix
     * @param bagOfWordsMap - Bag of words for all nodes or pages
     * @return  returns SimilarityMatrix
     */
    public SimilarityMatrix generateSimilarityMatrix(final Map<String, String> bagOfWordsMap) {
        Tokenizer tokenizer = new SimpleTokenizer();
        SimpleTermDictionary dictionary = new SimpleTermDictionary();
        /*  Creating a separate map to map NodeId to Index which would be used to fetch Recommendations Generated
            from CosineMatrix which stores cosine for each Node/Data Element with all other Nodes/Data Elements */
        LinkedHashMap<String, Integer> nodeIdIndexMap = new LinkedHashMap<>();
        int counter = 0;
        for (Map.Entry<String, String> eachEntry : bagOfWordsMap.entrySet()) {
            // Maintaining a map of Each NodeId to Index which will be used later to get the Similarity array which
            // stores the cosine similarity of each nodeId with given nodeId
            nodeIdIndexMap.put(eachEntry.getKey(), counter++);

            String[] tokens = tokenizer.getTokens(eachEntry.getValue());
            dictionary.addTerms(tokens);
        }
        // Initializing CountVectorizer object
        CountVectorizer vectorizer = new CountVectorizer(dictionary, tokenizer);
        // getting Matrix storing SparseVector for each Node/Data Element
        RealMatrix matrix = vectorizer.getCountMatrix(bagOfWordsMap.values());
        RealMatrix dotProductMatrix = new OpenMapRealMatrix(matrix.getRowDimension(), matrix.getRowDimension());

        for (int row = 0; row < matrix.getRowDimension(); row++) {
            RealVector node1Vector = matrix.getRowVector(row);
            for (int col = 0; col < matrix.getRowDimension(); col++) {
                RealVector node2Vector = matrix.getRowVector(col);

                double cosine = 0.0D;

                /*  Calculating the cosine of the angle between this vector and the argument only if
                    L2 norm (root of the sum of the squared elements) for both vectors is greater than zero. Because
                    cosine is calculated as ::> DOT PRODUCTS OF VECTOR / (L2 norm of V1 * L2 norm of V2) */
                if (node1Vector.getNorm() > 0.0D && node2Vector.getNorm() > 0.0D) {
                    cosine = node1Vector.cosine(node2Vector);
                }

                dotProductMatrix.setEntry(row, col, cosine);
            }
        }

        return new SimilarityMatrix(dotProductMatrix, nodeIdIndexMap);
    }
}
