package in.co.codebrains.recommendations.engine.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "AEM Recommendation Engine Configuration Factory")
public @interface AEMRecommenderConfig {

    @AttributeDefinition(name = "Recommendation Engine Name", description = "Name of the Recommendation Engine which will be used while reading the cached/stored recommendation map.")
    String recommendationEngineName();

    @AttributeDefinition(name = "Scheduler Expression", description = "Scheduler for AEM Recommendation Engine to run on regular interval.")
    String schedulerExpression() default "0 2 * * * ?";

    @AttributeDefinition(name = "Data Extraction Query", description = "Query to extract the data from AEM, pass a query for finding the JCR nodes which stores properties for generating recommendations.")
    String dataExtractionQuery();

    @AttributeDefinition(name = "Properties", description = "Pass the property names followed by property types in following pattern : <PROPERTY_NAME>=<PROPERTY_TYPE>. Supported types: String, Tag")
    String[] properties();
}
