package in.co.codebrains.recommendations.engine.service;

import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;

import java.util.List;

public interface RecommendationsReaderService {

    /**
     * Method to get Top N Recommendation Node paths which are similar to NodePath passed.
     *
     * @param recommendationEngineName  - RecommendationEnginerName from which recommendations should be read
     * @param topNResults               - Number of recommendations/similar results to show
     * @param nodePath                  - Path of node for which similar nodes should be found
     * @return                          - returns list of path to top N recommended nodes similar to currentNode
     * @throws RecommendationEngineNotFoundException - Throws RecommendationEngineNotFoundException if recommendationEngine with given name is not found
     * @throws SerializedObjectsNotFound             - Throws SerializedObjectsNotFound Exception if either "DotProductMatrix" or "NodeIdIndexMap" is not found while reading & deserializing the similarity Matrix
     */
    List<String> getTopRecommendations(String recommendationEngineName, int topNResults, String nodePath) throws RecommendationEngineNotFoundException, SerializedObjectsNotFound;
}
