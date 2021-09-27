package in.co.codebrains.recommendations.engine.scheduler;

import com.day.cq.commons.jcr.JcrConstants;
import in.co.codebrains.recommendations.engine.config.AEMRecommenderConfig;
import in.co.codebrains.recommendations.engine.exceptions.EmptyIteratorException;
import in.co.codebrains.recommendations.engine.service.RecommendationUtilService;
import in.co.codebrains.recommendations.engine.similarity.SimilarityMatrix;
import in.co.codebrains.recommendations.engine.similarity.SimilarityMatrixGenerator;
import in.co.codebrains.recommendations.engine.util.DataCleaningUtil;
import in.co.codebrains.recommendations.engine.util.GlobalUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Scheduler service which creates a Job for each instance of AEM Recommender Configuration. Job runs on configured
 * schedule/interval and generates the SimilarityMatrix (DotProductMatrix & NodeIdIndexMap) & then stores the serialized
 * objects of DotProductMatrix & NodeIdIndexMap.
 */
@Component(service = RecommendationsGeneratorScheduler.class, immediate = true)
@Designate(ocd = AEMRecommenderConfig.class, factory = true)
public class RecommendationsGeneratorScheduler implements Runnable {

    private AEMRecommenderConfig aemRecommenderConfig;

    @Reference
    private transient Scheduler scheduler;

    @Reference
    private transient SlingSettingsService settingsService;

    @Reference
    private transient RecommendationUtilService recommendationUtilService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationsGeneratorScheduler.class);

    @Activate
    @Modified
    protected void activate(AEMRecommenderConfig aemRecommenderConfig) {
        // Setting the scheduling options for scheduler to run the recommendation generator job on regular interval.
        ScheduleOptions scheduleOptions = scheduler.EXPR(aemRecommenderConfig.schedulerExpression());
        scheduleOptions.name(aemRecommenderConfig.recommendationEngineName());
        /* Scheduling RecommendationsGeneratorScheduler Job to generate Recommendations and serializes & stores the
           dotProductMatix in JCR. Only in authoring instance */
        if(this.settingsService.getRunModes().contains("author")) {
            scheduler.schedule(this, scheduleOptions);
        }

        this.aemRecommenderConfig = aemRecommenderConfig;
    }

    @Deactivate
    protected void deactivate(AEMRecommenderConfig aemRecommenderConfig) {
        // Un-scheduling the instance of RecommendationsGeneratorScheduler Job
        scheduler.unschedule(aemRecommenderConfig.recommendationEngineName());
    }

    @Override
    public void run() {
        Session session = null;
        try (ResourceResolver resourceResolver = recommendationUtilService.getResourceResolver()) {
            session = resourceResolver.adaptTo(Session.class);
            NodeIterator extractedData = extractDataNodes(aemRecommenderConfig.dataExtractionQuery(), resourceResolver);
            // Initializing DataCleaningUtil
            DataCleaningUtil dataCleaningUtil = new DataCleaningUtil(extractedData, aemRecommenderConfig.properties(),
                    resourceResolver);
            SimilarityMatrixGenerator similarityMatrixGenerator = new SimilarityMatrixGenerator();
            // Generating BagOfWords
            Map<String, String> bagOfWords = dataCleaningUtil.generateBagOfWords();
            // Generating Similarity Matrix
            SimilarityMatrix similarityMatrix = similarityMatrixGenerator.generateSimilarityMatrix(bagOfWords);
            // Serializing & Storing Similarity Matrix Object in JCR
            serializeStoreSimilarityMatrix(similarityMatrix, resourceResolver, aemRecommenderConfig.recommendationEngineName(), session);
        } catch (LoginException e) {
            LOGGER.error("LoginException occurred while getting service ResourceResolver : ", e);
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException occurred while reading node data for extracted data : ", e);
        } catch (EmptyIteratorException e) {
            LOGGER.error("EmptyIteratorException occurred while extracting the data for the SQL 2 query passed in the configuration : ", e);
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
        }
    }

    /* Utility method which runs the SQL-2 query which has been configured for AEM Recommendation Engine & returns
       the Node-Iterator. */
    private NodeIterator extractDataNodes(String dataExtractionQuery, ResourceResolver resourceResolver) {
        Session session = resourceResolver.adaptTo(Session.class);
        NodeIterator extractedDataNodes = null;

        try {
            Query sql2Query = null;
            if (session != null && StringUtils.isNotEmpty(dataExtractionQuery)) {
                QueryManager queryManager = session.getWorkspace().getQueryManager();
                sql2Query = queryManager.createQuery(dataExtractionQuery, Query.JCR_SQL2);
            }

            if (sql2Query != null) {
                sql2Query.setLimit(Integer.MAX_VALUE);
                QueryResult queryResult = sql2Query.execute();
                extractedDataNodes = queryResult.getNodes();
            }
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException occurred while getting Query Manager from JCR Session : ", e);
        }

        return extractedDataNodes;
    }

    /*
     * Utility Method which serializes the DotProductMatrix & NodeIdIndexMap and stores them in JCR repository under
     * /var directory
     *
     * @param similarityMatrix
     * @param resourceResolver
     * @param recommmenderName
     * @param session
     */
    private void serializeStoreSimilarityMatrix(final SimilarityMatrix similarityMatrix,
                                                 final ResourceResolver resourceResolver, final String recommmenderName,
                                                final Session session) {
        try {
            String similartityMatrixStoragePath = GlobalUtil.generateSimilarityMatrixStoragePath(recommmenderName);
            // Creating Map for storing Recommendation Engine Name as Node properties on Store Root node.
            Map<String, Object> properties = new HashMap<>();
            properties.put(GlobalUtil.ENGINE_NAME_PROPERTY, recommmenderName);
            // Getting or Creating if not present the root node for storing serialized dotProductMatrix & NodeIdIndexMap
            Resource storeRoot = ResourceUtil.getOrCreateResource(resourceResolver, similartityMatrixStoragePath, properties,
                    null, true);

            Node storeRootNode = storeRoot.adaptTo(Node.class);
            Node dotMatrixFile = null, dotMatrixContentNode = null, nodeIdIndexFile = null, nodeIdIndexContentNode = null;

            if (storeRootNode.hasNode(GlobalUtil.DOTPRODUCTMATRIX_FILE_NAME)) {
                // Reading the file store already created.
                dotMatrixFile = storeRootNode.getNode(GlobalUtil.DOTPRODUCTMATRIX_FILE_NAME);
                dotMatrixContentNode = dotMatrixFile.getNode(JcrConstants.JCR_CONTENT);
            } else {
                // Creating the file for DotProductMatrix
                dotMatrixFile = storeRootNode.addNode(GlobalUtil.DOTPRODUCTMATRIX_FILE_NAME, JcrConstants.NT_FILE);
                dotMatrixContentNode = dotMatrixFile.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                dotMatrixContentNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/octet-stream");
            }
            // Serializing the DotProductMatrix object
            ByteArrayOutputStream matrixByteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream matrixObjectOutputStream = new ObjectOutputStream(matrixByteArrayOutputStream);
            matrixObjectOutputStream.writeObject(similarityMatrix.getDotProductMatrix());
            matrixObjectOutputStream.flush();
            matrixObjectOutputStream.close();
            // Storing the Base64 encoded Serialized DotProductMatrix String as JCR Data property
            dotMatrixContentNode.setProperty(JcrConstants.JCR_DATA,
                    Base64.getEncoder().encodeToString(matrixByteArrayOutputStream.toByteArray()));

            if (storeRootNode.hasNode(GlobalUtil.NODEID_INDEX_MAP_FILE_NAME)) {
                // Reading the file store already created.
                nodeIdIndexFile = storeRootNode.getNode(GlobalUtil.NODEID_INDEX_MAP_FILE_NAME);
                nodeIdIndexContentNode = nodeIdIndexFile.getNode(JcrConstants.JCR_CONTENT);
            } else {
                // Creating the file for NodeId-IndexMap
                nodeIdIndexFile = storeRootNode.addNode(GlobalUtil.NODEID_INDEX_MAP_FILE_NAME, JcrConstants.NT_FILE);
                nodeIdIndexContentNode = nodeIdIndexFile.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                nodeIdIndexContentNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/octet-stream");
            }
            // Serializing the NodeId-IndexMap object
            ByteArrayOutputStream mapByteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream mapObjectOutputStream = new ObjectOutputStream(mapByteArrayOutputStream);
            mapObjectOutputStream.writeObject(similarityMatrix.getNodeIdIndexMap());
            mapObjectOutputStream.flush();
            // Storing the Base64 encoded Serialized NodeId-IndexMap String as JCR Data property
            nodeIdIndexContentNode.setProperty(JcrConstants.JCR_DATA,
                    Base64.getEncoder().encodeToString(mapByteArrayOutputStream.toByteArray()));
            /* Setting the Property "similarity-matrix-update-status" as true, to ensure Reader Service reads the
               latest value for DotProductMatrix & NodeIdIndexMap */
            storeRootNode.setProperty(GlobalUtil.SIMILARITY_MARTIX_UPDATED, true);

            session.save();
        } catch (IOException e) {
            LOGGER.error("IOException occurred while serializing the similarity Matrix : ", e);
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException occurred while creating the node hierarchy for similarity matrix for {} : Full exception {}",
                    recommmenderName, e);
        }
    }
}
