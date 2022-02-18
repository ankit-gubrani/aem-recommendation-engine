# Content Based Recommendation Engine in AEM

Recommendation Engine which generates the content based recommendations in AEM. 
It performs the consine similarity on features extracted from data collected from JCR based on properties provided as 
configuration.

## Further Details
Further details can be found here: [a AEM Recommendation Generator](https://www.codebrains.co.in/recommendation-engine)  

Follow this link to go over the [a overall approach](https://www.codebrains.co.in/recommendation-engine) used for implementing AEM Recommendation Generator.

## Configuring Recommendation Engine

* Add a new system user with name '**recommendations-generator**' then assign following permissions:
  * Read permissions for content hierarchy where data extraction query will run
  * Write permission for /var hierarchy to store serialized DotProductMatrix
* Add a new osgi configuration for factory config named "**AEM Recommendation Engine Configuration Factory**" with following properties for each Recommendation Engine:
    * **Recommendation Engine Name** : Name used for storing the DotProductMatrix & NodeIdIndex Map in JCR. Recommendation Engine Name is used for reading the stored recommendation engine from JCR
    * **Scheduler Expression** : Scheduler Expression for running the RecommendationEngine Generator on regular cadence to generate the recommendations based on newly added data
    * **Data Extraction Query** : JCR 2 query for extracting the data from within JCR, query should search for nodes having properties to be used for features extraction
    * **Properties** : Name of the properties which are used for feature extraction for generating recommendations. 
 
![Configuring Recommendation Engine](https://raw.github.com/ankit-gubrani/aem-recommendation-engine/master/screenshots/ConfiguringRecommendationEngine.png "Configuring Recommendation Engine")

* Add the oak Index for `Data Extraction Query` added in above step to ensure DotProductMatrix is generated in most performant manner.

**Note:** If you see following error - `Deserialization not allowed for class` while reading then make sure `com.adobe.cq.deserfw.impl.DeserializationFirewallImpl` is configured to white list following classes:
  * org.apache.commons.math3.linear.OpenMapRealMatrix
  * org.apache.commons.math3.util.OpenIntToDoubleHashMap

## Reading/Consuming Recommendations in a Sling Model 

![Reading Recommendations](https://raw.github.com/ankit-gubrani/aem-recommendation-engine/master/screenshots/ReadingRecommendationsInSlingModel.png "Reading Recommendations")

## Sample We Retail Recommendation Engine

Sample Recommendation Engine config gets installed for We Retail project along with project installation.
Sample recommendations can be seen by hitting below servlet:

`http://<HOST>:<PORT>/bin/testRecommendations?recommendationEngineName=We_Retail_Produts_Sample_Engine&numberOfRecommendations=3&nodePath=/content/we-retail/us/en/products/men/coats/portland-hooded-jacket/jcr:content`

**Note:** Please make sure to add system user before hitting above servlet & also update the 
cron expression as currently it's scheduled to run every hour, so engine would wait for 1 hour 
before generating the recommendations for the first time. 

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with

    mvn clean install -PautoInstallPackage

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish

Or alternatively

    mvn clean install -PautoInstallPackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Testing

There are three levels of testing contained in the project:

* unit test in core: this show-cases classic unit testing of the code contained in the bundle. To test, execute:

    mvn clean test
