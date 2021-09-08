package in.co.codebrains.recommendations.engine.util;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import in.co.codebrains.recommendations.engine.exceptions.EmptyIteratorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.*;

public class DataCleaningUtil {

    private NodeIterator nodeIterator;
    private String[] properties;
    private ResourceResolver resourceResolver;
    private TagManager tagManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCleaningUtil.class);

    public DataCleaningUtil(final NodeIterator nodeIterator, final String[] properties, final ResourceResolver resourceResolver) {
        this.nodeIterator = nodeIterator;
        this.properties = properties;
        this.resourceResolver = resourceResolver;
        // Initializing the TagManager
        initializeTagManager();
    }

    private void initializeTagManager() {
        if(resourceResolver != null) {
            tagManager = resourceResolver.adaptTo(TagManager.class);
        }
    }

    /**
     * Utility method which generates the BagOfWords for each Node.
     * @return                          returns a map of Node ID & its bag of words
     * @throws EmptyIteratorException   throws exception if NodeIterator is empty
     * @throws RepositoryException      throws exception if any issue is encountered during any Node operation
     */
    public Map<String, String> generateBagOfWords() throws EmptyIteratorException, RepositoryException {
        if (this.nodeIterator == null) {
            throw new EmptyIteratorException("Resource Iterator passed for generating Bags of Words was null");
        }

        Map<String, String> productPropertiesBagsOfWords = new HashMap<>();

        while (this.nodeIterator.hasNext()) {
            Node eachNode = this.nodeIterator.nextNode();
            String nodeId = null;

            nodeId = GlobalUtil.generateNodeId(eachNode.getPath());
            StringBuilder bagOfWordsBuilder = new StringBuilder();

            for (String propertyConfig : properties) {
                String propertyName = propertyConfig;
                String propertyType = "";
                // Checking if authors/user has configured the Type of property as well or not.
                if (propertyConfig.contains("=")) {
                    propertyName = propertyConfig.split("=")[0];
                    propertyType = propertyConfig.split("=")[1];
                }

                if (!eachNode.hasProperty(propertyName))
                    continue;

                Property property = eachNode.getProperty(propertyName);
                // If author has configured any property as Tag then using tagManager Tag Name is added to Bag of words
                if (StringUtils.equalsIgnoreCase(propertyType, "Tag")) {
                    // Checking if property is an array or single valued and reading those as array
                    Value[] tagsIds = (property.isMultiple()) ? property.getValues() : new Value[]{property.getValue()};
                    // Converting Tag IDs into Tag Names
                    Set<String> propertyValuesAsTagName = getTagName(tagsIds);
                    // Adding each configured property to bags of words if its found on the Node
                    bagOfWordsBuilder.append(String.join(StringUtils.SPACE, propertyValuesAsTagName))
                            .append(StringUtils.SPACE);
                } else {
                    // Checking if property is an array or single valued and reading those as array
                    Value[] propertyValues = (property.isMultiple()) ? property.getValues() : new Value[]{property.getValue()};
                    // Adding the non-tag property to the bag of words
                    for (Value eachProperty : propertyValues) {
                        // Removing empty spaces, special characters & converting text to lower case
                        bagOfWordsBuilder.append(StringUtils.SPACE).append(eachProperty.getString().
                                replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
                    }
                }
            }
            productPropertiesBagsOfWords.put(nodeId, bagOfWordsBuilder.toString());
        }

        return productPropertiesBagsOfWords;
    }

    /*
     * Utility method to get Tag Names from TagIds
     * @param tagIds
     * @return Returns a set of Tag Names to ensure only unique tag names are picked.
     */
    private Set<String> getTagName(Value[] tagIds) throws RepositoryException {
        if (tagIds == null || this.tagManager == null) {
            return null;
        }

        Set<String> tagNames = new HashSet<>();

        if (tagIds.length == 0) {
            return tagNames;
        }
        // Resolving each tag into Tag object to read the TagName and adding it to the set.
        for (Value tagId : tagIds) {
            Tag tag = tagManager.resolve(tagId.getString());

            if (tag != null) {
                // Removing empty spaces & converting text to lower case
                tagNames.add(tag.getTitle().replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
            }
        }
        return tagNames;
    }
}
