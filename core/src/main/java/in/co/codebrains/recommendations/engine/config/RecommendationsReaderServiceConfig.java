package in.co.codebrains.recommendations.engine.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Recommendations Reader Service : Configuration")
public @interface RecommendationsReaderServiceConfig {

    @AttributeDefinition(name = "Always Read Similarity Matrix from Memory",
            description = "If this property is enabled then Similarity Matrix is ready from memory whenever recommendations are requested from RecommendationsReaderService.")
    boolean alwaysReadSimilarityMatrix() default false;
}
