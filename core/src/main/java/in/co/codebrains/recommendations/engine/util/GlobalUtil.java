package in.co.codebrains.recommendations.engine.util;

import org.apache.commons.lang3.StringUtils;

public class GlobalUtil {

    /**
     * Path where serialized DotProductMatrix & NodeIdMap is stored
     */
    public static final String SIMILARITY_MATRIX_SERIALIZATION_STORE = "/var/aem-recommendation-matrix/store";

    /**
     * Name of the file storing DotProductMatrix in JCR
     */
    public static final String DOTPRODUCTMATRIX_FILE_NAME = "DotProductMatrix.ser";

    /**
     * Name of the file storing NodeId-Index Map in JCR
     */
    public static final String NODEID_INDEX_MAP_FILE_NAME = "NodeIdIndexMap.ser";

    /**
     * Name of the file storing NodeId-Index Map in JCR
     */
    public static final String SIMILARITY_MARTIX_UPDATED = "similarity-matrix-update-status";

    /**
     * Utility method which generates the path for Similarity Matrix serialized objects
     * @param recommendationEngineName - Name of the recommendation engine for generating the path without special characters
     * @return
     */
    public static String generateSimilarityMatrixStoragePath(final String recommendationEngineName) {
        return SIMILARITY_MATRIX_SERIALIZATION_STORE + "/" + recommendationEngineName.replaceAll("[^a-zA-Z0-9]", "");
    }

    /*
     * Utility method which converts Node Path into Node ID by replacing / with _
     * @param nodePath
     * @return
     */
    public static String generateNodeId(String nodePath) {
        if (StringUtils.isEmpty(nodePath)){
            return null;
        }

        if (!nodePath.contains("/")) {
            return nodePath;
        }

        return nodePath.replaceAll("/", "_");
    }

    /**
     * Utility method to converts NodeId into NodePath by replacing _ with /
     * @param nodeId
     * @return
     */
    public static String generateNodePath(String nodeId) {
        if (StringUtils.isEmpty(nodeId)){
            return null;
        }

        if (!nodeId.contains("_")) {
            return nodeId;
        }

        return nodeId.replaceAll("_", "/");
    }
}
