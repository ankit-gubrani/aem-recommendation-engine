package in.co.codebrains.recommendations.engine.service;

import in.co.codebrains.recommendations.engine.scheduler.RecommendationsGeneratorScheduler;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = RecommendationUtilService.class)
public class RecommendationUtilService {

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationUtilService.class);
    private static final String SUB_SERVICE_NAME = "aem-recommendation-engine-service";

    public ResourceResolver getResourceResolver() throws LoginException {
        final Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, SUB_SERVICE_NAME);
        return resourceResolverFactory.getServiceResourceResolver(param);
    }
}
