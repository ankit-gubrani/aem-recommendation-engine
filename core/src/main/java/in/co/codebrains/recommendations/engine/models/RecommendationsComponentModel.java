package in.co.codebrains.recommendations.engine.models;

import com.day.cq.wcm.api.Page;
import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;
import in.co.codebrains.recommendations.engine.service.RecommendationsReaderService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RecommendationsComponentModel {

    private RecommendationsReaderService recommendationsReaderService;

    private static final String CURRENT_PAGE_RECOMMENDATION_TYPE = "currentPage";
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationsComponentModel.class);

    @SlingObject
    private SlingHttpServletRequest slingRequest;

    @ScriptVariable
    private Page currentPage;

    @ValueMapValue
    @Default(values = StringUtils.EMPTY)
    private String recommendationEngineName;

    @ValueMapValue
    @Default(intValues = 3)
    private int numberOfRecommendations;

    @ValueMapValue
    @Default(values = CURRENT_PAGE_RECOMMENDATION_TYPE)
    private String recommendationsType;

    @ValueMapValue
    private String recommendationsForPath;

    private List<Resource> topNRecommendations;

    private List<Map<String, String>> recommendations;

    @Inject
    public RecommendationsComponentModel(final RecommendationsReaderService recommendationsReaderService) {
        this.recommendationsReaderService = recommendationsReaderService;
    }

    @PostConstruct
    protected void init() {
        recommendations = new ArrayList<>();
        String nodePath = "";

        if (StringUtils.equals(recommendationsType, CURRENT_PAGE_RECOMMENDATION_TYPE) && currentPage != null) {
            nodePath = currentPage.getContentResource().getPath();
        } else if (StringUtils.isNotEmpty(recommendationsForPath)) {
            nodePath = recommendationsForPath;
        }

        if (StringUtils.isNotEmpty(nodePath) && StringUtils.isNotEmpty(recommendationEngineName) && numberOfRecommendations > 0) {
            try {
                //topNRecommendations = recommendationsReaderService.getTopRecommendations(recommendationEngineName, numberOfRecommendations, nodePath);
                topNRecommendations = recommendationsReaderService.getTopRecommendationsAsResouce(recommendationEngineName,
                        numberOfRecommendations, nodePath, slingRequest.getResourceResolver());

                for (Resource eachResource : topNRecommendations) {
                    ValueMap properties = eachResource.adaptTo(ValueMap.class);
                    Map<String, String> recommendationProperties = new HashMap<>();

                    recommendationProperties.put("title", properties.get("jcr:title", String.class));
                    recommendationProperties.put("thumbnail", properties.get("image/fileReference", String.class));
                    recommendationProperties.put("path", eachResource.getParent().getPath());

                    recommendations.add(recommendationProperties);
                }

            } catch (RecommendationEngineNotFoundException e) {
                LOGGER.error("RecommendationEngineNotFoundException occurred while reading recommendations : ", e);
            } catch (SerializedObjectsNotFound e) {
                LOGGER.error("SerializedObjectsNotFound occurred while reading recommendations : ", e);
            }
        }
    }

    public String getRecommendationEngineName() {
        return recommendationEngineName;
    }

    public int getNumberOfRecommendations() {
        return numberOfRecommendations;
    }

    public String getRecommendationsType() {
        return recommendationsType;
    }

    public String getRecommendationsForPath() {
        return recommendationsForPath;
    }

    public List<Resource> getTopNRecommendations() {
        return topNRecommendations;
    }

    public List<Map<String, String>> getRecommendations() {
        return recommendations;
    }
}
