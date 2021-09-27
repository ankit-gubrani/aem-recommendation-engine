package in.co.codebrains.recommendations.engine.service;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import junit.framework.Assert;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class RecommendationUtilServiceTest {

    AemContext aemContext = new AemContext();

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    private RecommendationUtilService recommendationUtilService;
    private ResourceResolver assertResourceResolver;

    @BeforeEach
    public void setup() throws LoginException {
        aemContext.registerService(resourceResolverFactory);
        assertResourceResolver = aemContext.resourceResolver();
        recommendationUtilService = aemContext.registerInjectActivateService(new RecommendationUtilService());
        lenient().when(resourceResolverFactory.getServiceResourceResolver(any())).thenReturn(assertResourceResolver);
    }

    @Test
    @DisplayName("Testing getResourceResolver method")
    void getResourceResolverTest() throws LoginException {
        Assert.assertNotNull(recommendationUtilService.getResourceResolver());
    }


}
