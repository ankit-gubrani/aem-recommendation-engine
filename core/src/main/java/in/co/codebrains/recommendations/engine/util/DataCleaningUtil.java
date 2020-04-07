package in.co.codebrains.recommendations.engine.util;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import in.co.codebrains.recommendations.engine.exceptions.EmptyIteratorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataCleaningUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCleaningUtil.class);

    private Iterator<Resource> resourceIterator;
    private TagManager tagManager;

    private enum ProductProperties {

        TEST_TAG_PROPERTY_1("cq:tags-testproperty2"),
        TEST_TAG_PROPERTY_2("cq:tags-testproperty1");

        private String value;

        ProductProperties(final String value) {
            this.value = value;
        }

    }

    public DataCleaningUtil(final Iterator<Resource> resourceIterator, final TagManager tagManager) {
        this.resourceIterator = resourceIterator;
        this.tagManager = tagManager;
    }

    /**
     * Utility method which generates the BagOfWords for each product.
     * @return                          returns a map of ProductId & its bag of words
     * @throws EmptyIteratorException   throws exception if ResourceIterator of product resources is empty
     */
    public Map<String, String> generateBagOfWords() throws EmptyIteratorException {
        if (this.resourceIterator == null) {
            throw new EmptyIteratorException("Resource Iterator passed for generating Bags of Words was null");
        }

        Map<String, String> productPropertiesBagsOfWords = new HashMap<>();

        while (this.resourceIterator.hasNext()) {
            Resource productResource = this.resourceIterator.next();
            ValueMap productAttributes = null;
            String productId = null;

            if (productResource != null) {
                productAttributes = productResource.adaptTo(ValueMap.class);
                productId = generateProductId(productResource.getPath());
            }

            if (productAttributes != null && productId != null) {
                StringBuilder bagOfWordsBuilder = new StringBuilder();
                for (ProductProperties productProperty : ProductProperties.values()) {
                    if (!productAttributes.containsKey(productProperty.value)) {
                        break;
                    }
                    // TODO: Use Node API Later to find type of property first. Only String values can be added to Bags of Words.
                    // Reading all properties as String array
                    List<String> propertyValuesAsTagName = getTagName(productAttributes.get(productProperty.value, new String[0]));

                    // Adding each product property to bags of words if its found on product resource
                    bagOfWordsBuilder.append(String.join(" ", propertyValuesAsTagName))
                            .append(" ");
                }
                // Adding each product to ProductId-BagsOfWords Map
                productPropertiesBagsOfWords.put(productId, bagOfWordsBuilder.toString());
            }
        }

        return productPropertiesBagsOfWords;
    }

    private String generateProductId(String productPath) {
        if (StringUtils.isEmpty(productPath)){
            return null;
        }

        if (!productPath.contains("/")) {
            return productPath;
        }

        return productPath.replaceAll("/", "_");
    }

    /*
     * Utility method to get Tag Names from TagIds
     * @param tagIds
     * @return
     */
    private List<String> getTagName(String[] tagIds) {

        if (tagIds == null || this.tagManager == null) {
            return null;
        }

        List<String> tagNames = new ArrayList<>();

        if (tagIds.length == 0) {
            return tagNames;
        }


        for (String tagId : tagIds) {
            Tag tag = tagManager.resolve(tagId);

            if (tag != null) {
                tagNames.add(tag.getName());
            }
        }

        return tagNames;

    }
}
