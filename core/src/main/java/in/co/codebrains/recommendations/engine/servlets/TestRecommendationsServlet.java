package in.co.codebrains.recommendations.engine.servlets;

import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;
import in.co.codebrains.recommendations.engine.service.RecommendationsReaderService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/testRecommendations",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.extensions=json"
})
public class TestRecommendationsServlet extends SlingSafeMethodsServlet {

    @Reference
    private RecommendationsReaderService recommendationsReaderService;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        StringBuilder builder = new StringBuilder();
        try {
            String recommendationEngineName = request.getParameter("recommendationEngineName");
            Integer numberOfRecommendations = Integer.parseInt(request.getParameter("numberOfRecommendations"));
            String nodePath = request.getParameter("nodePath");

            List<String> topNRecommendations = recommendationsReaderService.getTopRecommendations(recommendationEngineName,
                    numberOfRecommendations, nodePath);

            if (topNRecommendations != null) {
                for (String eachRecommendation : topNRecommendations) {
                    builder.append(eachRecommendation).append("\n");
                }
            }
        } catch (RecommendationEngineNotFoundException e) {
            e.printStackTrace();
        } catch (SerializedObjectsNotFound serializedObjectsNotFound) {
            System.out.println("SerializedObjectsNotFound occurred");
            serializedObjectsNotFound.printStackTrace();
        }
        response.getWriter().println(builder.toString());
    }

}
