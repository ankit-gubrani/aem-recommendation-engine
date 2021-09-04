package in.co.codebrains.recommendations.engine.service.impl;

import com.day.cq.commons.jcr.JcrConstants;
import in.co.codebrains.recommendations.engine.config.RecommendationsReaderServiceConfig;
import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;
import in.co.codebrains.recommendations.engine.service.RecommendationUtilService;
import in.co.codebrains.recommendations.engine.service.RecommendationsReaderService;
import in.co.codebrains.recommendations.engine.similarity.SimilarityMatrix;
import in.co.codebrains.recommendations.engine.util.GlobalUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.OpenIntToDoubleHashMap;
import org.apache.commons.math3.util.Pair;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.*;
import java.util.*;

@Component(immediate = true, service = RecommendationsReaderService.class)
@Designate(ocd = RecommendationsReaderServiceConfig.class)
public class RecommendationsReaderServiceImpl implements RecommendationsReaderService {

    private Map<String, SimilarityMatrix> similarityMatrixMap = new HashMap<>();
    private boolean readSimilarityMatrixFromMemory = false;

    @Reference
    private transient RecommendationUtilService recommendationUtilService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationsReaderServiceImpl.class);

    @Activate
    @Modified
    protected void activate(RecommendationsReaderServiceConfig readerServiceConfig) {
        readSimilarityMatrixFromMemory = readerServiceConfig.alwaysReadSimilarityMatrix();
    }

    @Override
    public List<String> getTopRecommendations(String recommendationEngineName, int topNResults, String nodePath) throws RecommendationEngineNotFoundException, SerializedObjectsNotFound {
        try (ResourceResolver resourceResolver = recommendationUtilService.getResourceResolver()) {
            if (readSimilarityMatrixFromMemory) {
                SimilarityMatrix similarityMatrix = readSimilarityMatrix(recommendationEngineName, resourceResolver);
                similarityMatrixMap.put(recommendationEngineName, similarityMatrix);
            } else if (isSimilartyMatrixUpdated(recommendationEngineName, resourceResolver) ||
                    !similarityMatrixMap.containsKey(recommendationEngineName)) {
                SimilarityMatrix similarityMatrix = readSimilarityMatrix(recommendationEngineName, resourceResolver);
                similarityMatrixMap.put(recommendationEngineName, similarityMatrix);
            }
        } catch (LoginException e) {
            LOGGER.error("LoginException occurred while getting ResourceResolver for service user : ", e);
        }
        // Getting the NodeId for NodePath passed, for which recommendations are to be generated.
        String nodeId = GlobalUtil.generateNodeId(nodePath);
        return findTopRecommendations(similarityMatrixMap.get(recommendationEngineName), nodeId, topNResults);
    }

    /*
     * Utility method which reads the Serialized SimilarityMatrix object stored under "/var/aem-recommendation-matrix"
     * for any given RecommendationEngineName and also updates the "similarity-matrix-update-status" property to false
     * root node for RecommendationEngine.
     *
     * @param recommendationEngineName
     * @param resourceResolver
     * @return
     * @throws SerializedObjectsNotFound
     * @throws RecommendationEngineNotFoundException
     */
    private SimilarityMatrix readSimilarityMatrix(String recommendationEngineName, ResourceResolver resourceResolver) throws SerializedObjectsNotFound, RecommendationEngineNotFoundException {
        SimilarityMatrix similarityMatrix = null;
        try {
            String cachedRecommendationPath = GlobalUtil.generateSimilarityMatrixStoragePath(recommendationEngineName);

            Resource recommendationEngineStorageRoot = resourceResolver.getResource(cachedRecommendationPath);

            if (recommendationEngineStorageRoot == null) {
                throw new RecommendationEngineNotFoundException("Recommendation Engined not found at path : " + cachedRecommendationPath);
            }

            Resource dotProductMatrixResource = recommendationEngineStorageRoot.getChild(GlobalUtil.DOTPRODUCTMATRIX_FILE_NAME);
            Resource nodeIdIndexMapResource = recommendationEngineStorageRoot.getChild(GlobalUtil.NODEID_INDEX_MAP_FILE_NAME);

            if (dotProductMatrixResource == null || nodeIdIndexMapResource == null ||
                    !dotProductMatrixResource.hasChildren() || !nodeIdIndexMapResource.hasChildren()) {
                throw new SerializedObjectsNotFound("Serialized DotProductMatrix OR NodeIdIndexMap not found");
            }
            // Reading the JCR DATA for Serialized DotProductMatrix
            Resource dotProductMatrixContent = dotProductMatrixResource.getChild(JcrConstants.JCR_CONTENT);
            Node dotProductMatrixContentNode = dotProductMatrixContent.adaptTo(Node.class);

            // De-Serializing DotProductMatrix object
            InputStream inputStream = dotProductMatrixContentNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
            // This is to ensure OpenIntToDoubleHashMap is present in class loader and does not cause any issues while deserializing
            OpenIntToDoubleHashMap openIntToDoubleHashMap = new OpenIntToDoubleHashMap();
            // Decoding DotProductMatrix as Byte array
            byte [] deocdedDotProductMatrixData = Base64.getDecoder().decode(IOUtils.toByteArray(dotProductMatrixContentNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream()));
            ObjectInputStream dotProductMatrixOIS = new ObjectInputStream(new ByteArrayInputStream(deocdedDotProductMatrixData));
            RealMatrix dotProductMatrix = (RealMatrix) dotProductMatrixOIS.readObject();
            dotProductMatrixOIS.close();

            // Reading the JCR DATA for Serialized NodeIdIndexMap
            Resource nodeIdIndexMapContent = nodeIdIndexMapResource.getChild(JcrConstants.JCR_CONTENT);
            Node nodeIdIndexMapContentNode = nodeIdIndexMapContent.adaptTo(Node.class);
            // De-Serializing NodeIdIndexMap object
            byte [] deocdedNodeIdIndexMapData = Base64.getDecoder().decode(IOUtils.toByteArray(nodeIdIndexMapContentNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream()));
            ObjectInputStream nodeIdIndexMapOIS = new ObjectInputStream(new ByteArrayInputStream(deocdedNodeIdIndexMapData));
            LinkedHashMap<String, Integer> nodeIdIndexMap = (LinkedHashMap) nodeIdIndexMapOIS.readObject();
            nodeIdIndexMapOIS.close();

            similarityMatrix = new SimilarityMatrix(dotProductMatrix, nodeIdIndexMap);

            // Updating the recommendationEngineStorageRoot node & setting "similarity-matrix-update-status" property
            // as false because it is read once from storage.
            ModifiableValueMap recommendationEngineStorageRootProperties = recommendationEngineStorageRoot.adaptTo(ModifiableValueMap.class);
            recommendationEngineStorageRootProperties.put(GlobalUtil.SIMILARITY_MARTIX_UPDATED, false);
            resourceResolver.commit();
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException occurred while reading JCR DATA from DotProductMatrix Node : ", e);
        } catch (IOException e) {
            LOGGER.error("IOException occurred while de-serializing the Similarity Matrix Objects : ", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("ClassNotFoundException occurred while de-serializing the Similarity Matrix Objects : ", e);
        }

        return similarityMatrix;
    }

    /* Utility method to check if SimilarityMatrix was updated by
     *
     * @param recommendationEngineName
     * @param resourceResolver
     * @return
     * @throws RecommendationEngineNotFoundException
     */
    private boolean isSimilartyMatrixUpdated(String recommendationEngineName, ResourceResolver resourceResolver) throws RecommendationEngineNotFoundException {
        String cachedRecommendationPath = GlobalUtil.generateSimilarityMatrixStoragePath(recommendationEngineName);
        Resource recommendationEngineStorageRoot = resourceResolver.getResource(cachedRecommendationPath);
        if (recommendationEngineStorageRoot == null) {
            throw new RecommendationEngineNotFoundException("Recommendation Engined not found at path : " + cachedRecommendationPath);
        }
        // Getting the properties of recommendationEngineStorageRoot node.
        ValueMap recommendationEngineStorageRootProperties = recommendationEngineStorageRoot.adaptTo(ValueMap.class);
        return recommendationEngineStorageRootProperties.get(GlobalUtil.SIMILARITY_MARTIX_UPDATED, false);
    }

    /*
     *
     * @param similarityMatrix
     * @param nodeId
     * @param numberOfRecommendations
     * @return
     */
    private List<String> findTopRecommendations(SimilarityMatrix similarityMatrix, String nodeId, int numberOfRecommendations) {
        List<String> topRecommendations = new ArrayList<>(numberOfRecommendations);
        if (similarityMatrix == null) {
            return null;
        }
        // Reading DotProductMatrix & NodeIdIndexMap
        LinkedHashMap<String, Integer> nodeIdIndexMap = similarityMatrix.getNodeIdIndexMap();
        RealMatrix dotProductMatrix = similarityMatrix.getDotProductMatrix();
        /*  Creating List of Map Entry to fetch the NodeId based on Index as NodeIdIndex map can provide index from
            nodeId. But while reading top N recommendations from Vector storing CosineSimilarity we would just have
            index of node with its corresponding consineSimilarity, this list would help us fetch the Node Id from
            index. */
        List<Map.Entry<String, Integer>> nodeIndexToNodeId = new ArrayList<Map.Entry<String, Integer>>(nodeIdIndexMap.entrySet());
        // MaxHeap for getting sorting the double array storing the cosine similarity
        PriorityQueue<Pair<Double, String>>  maxHeap = new PriorityQueue<>(Comparator.comparing(Pair::getKey, Comparator.naturalOrder()));
        // Getting the Index for NodeId to read the Similarity Array from DotProductMatrix
        int nodeIdIndex = nodeIdIndexMap.getOrDefault(nodeId, -1);
        // Node Id does not exists in the nodeIdIndexMap hence returning null
        if (nodeIdIndex == -1) {
            return null;
        }
        // Getting the Array which stores Similarity Matrix for current Node with all other Nodes
        double[] similarityArray = dotProductMatrix.getRow(nodeIdIndex);
        // Iterating through similarity Array storing cosine similarity of current NodeId with other Nodes.
        for (int i = 0; i < similarityArray.length; i++) {
            /* Making sure we are NOT reading cosine similarity of NodeId for which we are finding the top N
               recommendations with its own as cosine similarity as that would be 1 or 0.9999. */
            if (i != nodeIdIndex) {
                maxHeap.add(new Pair<>(similarityArray[i], nodeIndexToNodeId.get(i).getKey()));

                if (maxHeap.size() > numberOfRecommendations)
                    maxHeap.poll();
            }
        }

        while(!maxHeap.isEmpty()) {
            Pair<Double, String> eachRecommendation = maxHeap.poll();
            topRecommendations.add(GlobalUtil.generateNodePath(eachRecommendation.getSecond()));
        }

        return topRecommendations;
    }
}
