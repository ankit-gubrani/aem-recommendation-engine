package in.co.codebrains.recommendations.engine.similarity;

import com.adobe.xfa.Int;
import junitx.framework.ComparableAssert;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class SimilarityMatrixGeneratorTest {

    private SimilarityMatrixGenerator similarityMatrixGenerator;
    private SimilarityMatrix assertSimilarityMatrix;

    private static final String PRODUCT_1 = "_content_we-retail_us_en_products_women_coats_sleek-insulated-coat_jcr:content";
    private static final String PRODUCT_2 = "_content_we-retail_us_en_products_women_coats_sonja-insulated-jacket_jcr:content";
    private static final String PRODUCT_3 = "_content_we-retail_us_en_products_women_shirts_rios-t-shirt_jcr:content";

    @BeforeEach
    void setup() {
        similarityMatrixGenerator = new SimilarityMatrixGenerator();

        Map<String, String> bagOfWordsMap = new HashMap<>();
        // Adding Sample data to BagsOfWordsMap
        bagOfWordsMap.put(PRODUCT_1, "skiing coat winter women");
        bagOfWordsMap.put(PRODUCT_2, "winter others coat women");
        bagOfWordsMap.put(PRODUCT_3, "summer hiking shirt women");
        // Generating Similarity Matrix
        assertSimilarityMatrix = similarityMatrixGenerator.generateSimilarityMatrix(bagOfWordsMap);
    }

    @Test
    @DisplayName("Testing GenerateSimilarityMatrix Method")
    void generateSimilarityMatrixTest() {
        Assertions.assertNotNull(assertSimilarityMatrix);
        LinkedHashMap<String, Integer> nodeIdIndexMap = assertSimilarityMatrix.getNodeIdIndexMap();

        RealMatrix dotProductMatrix = assertSimilarityMatrix.getDotProductMatrix();

        int product1Index = nodeIdIndexMap.get(PRODUCT_1);
        int product2Index = nodeIdIndexMap.get(PRODUCT_2);
        int product3Index = nodeIdIndexMap.get(PRODUCT_2);


        double product1SelfSimilarity = dotProductMatrix.getEntry(product1Index, product1Index);
        double product2SelfSimilarity = dotProductMatrix.getEntry(product2Index, product2Index);
        double product3SelfSimilarity = dotProductMatrix.getEntry(product3Index, product3Index);
        // Asserting Cosine Similarity of each Product is more than 0.95 meaning Cosine Similarities are calculated correctly.
        ComparableAssert.assertGreater(0.95, product1SelfSimilarity);
        ComparableAssert.assertGreater(0.95, product2SelfSimilarity);
        ComparableAssert.assertGreater(0.95, product3SelfSimilarity);
    }
}
