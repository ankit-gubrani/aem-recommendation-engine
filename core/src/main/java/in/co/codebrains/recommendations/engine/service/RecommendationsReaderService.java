package in.co.codebrains.recommendations.engine.service;

import com.day.cq.wcm.api.Page;
import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

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
    List<String> getTopRecommendations(String recommendationEngineName, int topNResults, String nodePath)
            throws RecommendationEngineNotFoundException, SerializedObjectsNotFound;

    /**
     * Method to get Top N Recommendations as Sling Resource which are similar to NodePath passed.
     *
     * @param recommendationEngineName  - RecommendationEnginerName from which recommendations should be read
     * @param topNResults               - Number of recommendations/similar results to show
     * @param nodePath                  - Path of node for which similar nodes should be found
     * @return                          - returns list of path to top N recommended nodes similar to currentNode
     * @throws RecommendationEngineNotFoundException - Throws RecommendationEngineNotFoundException if recommendationEngine with given name is not found
     * @throws SerializedObjectsNotFound             - Throws SerializedObjectsNotFound Exception if either "DotProductMatrix" or "NodeIdIndexMap" is not found while reading & deserializing the similarity Matrix
     */
    List<Resource> getTopRecommendationsAsResouce(String recommendationEngineName, int topNResults, String nodePath,
                                                  ResourceResolver resourceResolver) throws RecommendationEngineNotFoundException, SerializedObjectsNotFound;

    /**
     * Method to get Top N Recommendations as Page which are similar to NodePath passed.
     * Note: This method returns the list of pages which contains the recommended resources.
     *
     * @param recommendationEngineName  - RecommendationEnginerName from which recommendations should be read
     * @param topNResults               - Number of recommendations/similar results to show
     * @param nodePath                  - Path of node for which similar nodes should be found
     * @return                          - returns list of path to top N recommended nodes similar to currentNode
     * @throws RecommendationEngineNotFoundException - Throws RecommendationEngineNotFoundException if recommendationEngine with given name is not found
     * @throws SerializedObjectsNotFound             - Throws SerializedObjectsNotFound Exception if either "DotProductMatrix" or "NodeIdIndexMap" is not found while reading & deserializing the similarity Matrix
     */
    public List<Page> getTopRecommendationsAsPage(String recommendationEngineName, int topNResults, String nodePath,
                                                  ResourceResolver resourceResolver) throws RecommendationEngineNotFoundException, SerializedObjectsNotFound;
}
