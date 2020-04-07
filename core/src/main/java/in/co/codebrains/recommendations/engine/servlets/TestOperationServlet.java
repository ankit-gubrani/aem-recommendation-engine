package in.co.codebrains.recommendations.engine.servlets;

import com.adobe.cq.commerce.api.CommerceConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.tagging.TagManager;
import in.co.codebrains.recommendations.engine.GenerateRecommendations;
import in.co.codebrains.recommendations.engine.exceptions.EmptyIteratorException;
import in.co.codebrains.recommendations.engine.util.DataCleaningUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/testRecommendations",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.extensions=json"
})
public class TestOperationServlet extends SlingSafeMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestOperationServlet.class);

    private static final String PRODUCTS_ROOT_PATH = "/var/commerce/products/aem_recommendations";

    protected void doGet( SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        ResourceResolver resourceResolver = request.getResourceResolver();
        QueryBuilder queryBuilder = resourceResolver.adaptTo(QueryBuilder.class);
        Session session = resourceResolver.adaptTo(Session.class);
        TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
        Map<String, String> productIdBagOfWordsMap = null;

        try {
            if (queryBuilder != null && session != null) {

                Map<String, String> predicatesMap = new HashMap<>();
                predicatesMap.put("path", PRODUCTS_ROOT_PATH);
                predicatesMap.put("property", CommerceConstants.PN_COMMERCE_TYPE);
                predicatesMap.put("property.value", "product");
                predicatesMap.put("p.limit", "-1");

                Query query = queryBuilder.createQuery(PredicateGroup.create(predicatesMap), session);
                SearchResult searchResult = query.getResult();

                Iterator<Resource> resourceIterator = searchResult.getResources();

                DataCleaningUtil cleaningUtil = new DataCleaningUtil(resourceIterator, tagManager);

                productIdBagOfWordsMap = cleaningUtil.generateBagOfWords();
            }

            if (productIdBagOfWordsMap != null) {
                printBagOfWords(productIdBagOfWordsMap, response.getWriter());
            }

            GenerateRecommendations recommendations = new GenerateRecommendations();

            recommendations.showCountVectorizer(productIdBagOfWordsMap);

        } catch (EmptyIteratorException e) {
            LOGGER.error("EmptyIteratorException occured while generating Bags of Words : " ,e);
        }
    }

    private void printBagOfWords(final Map<String, String> productIdBagOfWordsMap,
                                 final PrintWriter writer) {
        for (Map.Entry<String, String> entry : productIdBagOfWordsMap.entrySet()) {

            if (entry != null) {
                writer.println("Product ID -- " + entry.getKey());
                writer.println("            |--- Bags of Words  ---> " + entry.getValue());
            }
        }
    }

}
