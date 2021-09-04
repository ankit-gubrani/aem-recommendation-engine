## Progress So Far:

* So far I am able to find similarity between 2 given products and also generate the Matrix which has similarity Score
of each product with all other products. This done by calculating the **`Cosine Angle`** between 
Vector of each product with all other products.
(Steps mention in _[Overall Approach](#Overall-Approach)_ section)

## Pending Items:

* Create a Method to get HTML which shows Matrix of Cosine for all products
* See how to store values in **`dotProductMatrix`** so that most similar products can be extracted easily. 
Because currently we cannot find Similar products directly, we can just find Similarity between 2 products.
* Need to decide if I should calculate & store the recommendations on each product   

## Overall Approach:
1. Request comes to servlet (TestOperationServlet)
2. All the products are searched in AEM
3. Using **DataCleaningUtil.java** `bogOfWords` is generated
    1. Properties to be read are stored in `ProductProperties` enum
    2. `generateBagOfWords()` method returns a map of ProductId mapped to bagOfWords associated to that product.
    **bagOfWords** is just space separated String with different Tags, or other values of 
    properties like Developer, Publisher, jcr:title, cq:tags etc.  
4. `showCountVectorizer()` method from `GenerateRecommendations` is called.
    _**Note:**_ This class is just calling all the required methods from 
    `Tokenizer, Dictionary & CountVectorizer` classes to convert each product with words associated to them into 
    numeric values which can be passed to an Algorithm for getting recommendations.
5. #### Generating recommendations:
6. `Tokenizer` object is created using custom `SimpleTokenizer` implementations
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
    2. `getCountMatrix()` **:** returns RealMatrix 
        1. returns a matrix of Vectors for each Product's bagOfWords
9. `RealMatrix` generated using `getCountMatrix()` method is looped through and:
    1. For Each row i.e (each product's vector) cosine angle is calculated with all the other products
    2. And cosine is stored in a **`dotProductMatrix`** 
10. A separate Map is maintained which maps each ProductId (String) to the Index of product in `RealMatrix` 
from step _**9.**_
11. #### Reading similarity between 2 Products:
12. Now that we have **`dotProductMatrix`** we can simply pass the Index of both products as Row & Col to get
similarity score.

     