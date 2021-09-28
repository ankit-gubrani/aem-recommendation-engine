## Overall Approach:
1. `RecommendationsGeneratorScheduler` runs on regular interval & starts the process for generating recommentations  
2. Data extraction begins by running the query passed by users via "[a AEM Recommendation Engine](https://github.com/ankit-gubrani/aem-recommendation-engine#configuring-recommendation-engine)" OSGi Config  
3. Using **DataCleaningUtil.java** `bogOfWords` is generated
    1. Properties to be read are passed in the DataCleaningUtil constructor.
    2. `generateBagOfWords()` method returns a map of ProductId mapped to bagOfWords associated to that product.
    **bagOfWords** is just space separated String with different Tags, or other values of 
    properties like jcr:title, cq:tags etc.  
4. `showCountVectorizer()` method from `GenerateRecommendations` is called.
    _**Note:**_ This class is just calling all the required methods from 
    `Tokenizer, Dictionary & CountVectorizer` classes to convert each product with words associated to them into 
    numeric values which can be passed to an Algorithm for getting recommendations.
5. #### Generating recommendations:
6. `Tokenizer` object is created using custom `SimpleTokenizer` implementation
    1. `SimpleTokenizer` provides a method to generate Array of tokens from `BagOfWords` liked to each product
7. `Dictionary` object is initialized using custom `SimpleTermDictionary` implementation
    1. `SimpleTermDictionary` maintains a _**Vocabulary**_ 
    2. `SimpleTermDictionary` provides methods:
        1. `getTermIndex()`: returns the Index (Number given to each term in Dictionary) of given term. 
        2. `getTotalTerms()`: returns the total number of terms/items in `Vocabulary`
8. `CountVectorizer` Object is initialized with `dictionary & tokenizer` objects initialized in previous steps.
 `CountVectorizer` provides 2 methods:
    1. `getCountVector()` **:** which returns the SparseVector of the current product's bagOfWords.
        1. returns `RealVector` object
        2. `SparseVector` of size of `Dictionary's` Vocabulary size 
        3. `SparseVector` which has represents a word as 1 in the array while 0 if that word from vocabulary is not 
        part of Products BagOfWords  
    2. `getCountMatrix()` **:** returns `RealMatrix` 
        1. returns a matrix of Vectors for each Product's bagOfWords
9. In `SimilarityMatrixGenerator` class's `generateSimilarityMatrix()` method `RealMatrix` generated using `getCountMatrix()` method is looped through and:
    1. For Each row i.e (each node's vector) cosine angle is calculated with all the other nodes/items
    2. And cosine is stored in a **`dotProductMatrix`**
    3. Finally SimilarityMatrix Object is created which contains **`dotProductMatrix`** and **`NodeIdIndexMap`** (mapping nodeId to Index, which is used for reading recommendations from dotProductMatrix) 
10. A separate Map is maintained which maps each ProductId (String) to the Index of product in `RealMatrix` 
from step _**9.**_
11. **`SimilarityMatrix`** object is serialized & stored in JCR under `/var`, which can be later read while getting the similarities. And Recommendation generation is an heavy operation, RecommendationsGeneratorScheduler can generate & update the SimilarityMatrix after fixed interval.       
13. #####`RecommendationsReaderService` can be used for reading the recommendations from SimilarityMatrix serialized & stored within JCR. 
**Note**: There can be several RecommendationEngines configured in any AEM instance for generating different recommendations for different pages, products or items.

     