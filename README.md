# Content Based Recommendation Engine in AEM

Recommendation Engine which generates the content based recommendations in AEM. 
It performs the consine similarity on features extracted from data collected from JCR based on properties provided as 
configuration.

## Further Details
Further details can be found here: [a AEM Recommendation Generator](https://www.codebrains.co.in/recommendation-engine)  

Follow this link to go over the [a overall approach](https://www.codebrains.co.in/recommendation-engine) used for implementing AEM Recommendation Generator.

## Configuring Recommendation Engine

* Add a new osgi configuration for factory config named "**AEM Recommendation Engine Configuration Factory**" with following properties for each Recommendation Engine:
    * **Recommendation Engine Name** : Name used for storing the DotProductMatrix & NodeIdIndex Map in JCR. Recommendation Engine Name is used for reading the stored recommendation engine from JCR
    * **Scheduler Expression** : Scheduler Expression for running the RecommendationEngine Generator on regular cadence to generate the recommendations based on newly added data
    * **Data Extraction Query** : JCR 2 query for extracting the data from within JCR, query should search for nodes having properties to be used for features extraction
    * **Properties** : Name of the properties which are used for feature extraction for generating recommendations. 
 
![Configuring Recommendation Engine](https://raw.github.com/ankit-gubrani/aem-recommendation-engine/master/screenshots/ConfiguringRecommendationEngine.png "Configuring Recommendation Engine")

## Reading/Consuming Recommendations in a Sling Model 

![Reading Recommendations](https://raw.github.com/ankit-gubrani/aem-recommendation-engine/master/screenshots/ReadingRecommendationsInSlingModel.png "Reading Recommendations")

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
