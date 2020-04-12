package in.co.codebrains.recommendations.engine;

import in.co.codebrains.recommendations.engine.extraction.CountVectorizer;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTermDictionary;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTokenizer;
import in.co.codebrains.recommendations.engine.extraction.util.Tokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GenerateRecommendations {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRecommendations.class);

    public GenerateRecommendations() {

    }

    public void showCountVectorizer(final Map<String, String> productIdBagOfWordsMap) {

        Tokenizer tokenizer = new SimpleTokenizer();
        SimpleTermDictionary dictionary = new SimpleTermDictionary();
        /*  Creating a separate map to map productId to Index which would be used to fetch CosineSimilarity
            from CosineMatrix which stores cosine for each product with all other products */
        Map<String, Integer> productIdIndexMap = new HashMap<>();
        int counter = 0;

        // HTML FOR DIAGONAL VERIFICATION; Creating a String Builder Object
        StringBuilder dialogVerificationHTMLStr = new StringBuilder();
        dialogVerificationHTMLStr.append("<tr><td></td>");
        for (Map.Entry<String, String> eachEntry : productIdBagOfWordsMap.entrySet()) {
            // HTML FOR DIAGONAL VERIFICATION; Adding Column count for each Product
            dialogVerificationHTMLStr.append("<td>").append(counter).append("</td>");
            productIdIndexMap.put(eachEntry.getKey(), counter++);

            String[] tokens = tokenizer.getTokens(eachEntry.getValue());
            dictionary.addTerms(tokens);
        }
        // HTML FOR DIAGONAL VERIFICATION; Closing Table Row for Column Count row
        dialogVerificationHTMLStr.append("</tr>");
        // Initializing CountVectorizer object
        CountVectorizer vectorizer = new CountVectorizer(dictionary, tokenizer);
        // getting Matrix storing SparseVector for each Product
        RealMatrix matrix = vectorizer.getCountMatrix(productIdBagOfWordsMap.values());
        RealMatrix dotProductMatrix = new OpenMapRealMatrix(matrix.getRowDimension(), matrix.getRowDimension());

        for (int row = 0; row < matrix.getRowDimension(); row++) {
            // HTML FOR DIAGONAL VERIFICATION; opening table row and empty table Data for Index Column
            dialogVerificationHTMLStr.append("<tr><td>").append(row).append("</td>");
            RealVector product1Vector = matrix.getRowVector(row);
            for (int row1 = 0; row1 < matrix.getRowDimension(); row1++) {
                RealVector product2Vector = matrix.getRowVector(row1);

                double cosine = 0.0D;

                /*  Calculating the cosine of the angle between this vector and the argument only if
                    L2 norm (root of the sum of the squared elements) for both vectors is greater than zero. Because
                    cosine is calculated as ::> DOT PRODUCTS OF VECTOR / (L2 norm of V1 * L2 norm of V2) */
                if (product1Vector.getNorm() > 0.0D && product2Vector.getNorm() > 0.0D) {
                    cosine = product1Vector.cosine(product2Vector);
                }

                dotProductMatrix.setEntry(row, row1, cosine);
                // HTML FOR DIAGONAL VERIFICATION; Adding Cosine in the Table Data
                dialogVerificationHTMLStr.append("<td>").append(cosine).append("</td>");
            }
            // HTML FOR DIAGONAL VERIFICATION; closing table row
            dialogVerificationHTMLStr.append("</tr>");
        }

        // --- UNCOMMENT Below code to verify diagonals has 1. ---
        /*LOGGER.info("<-- Use HTML -->");
        LOGGER.info(stringBuilder.toString());
        LOGGER.info("<-- Use HTML -->");*/

        Integer prod1Index = productIdIndexMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product");
        Integer prod2Index = productIdIndexMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_ankit");
        Integer prod3Index = productIdIndexMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommercestage_myprod");

        LOGGER.info("Index of Prod1 - " + prod1Index + " : Product ID : _var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product" );
        LOGGER.info("Index of Prod2 - " + prod2Index + " : Product ID : _var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_ankit" );
        LOGGER.info("Index of Prod3 - " + prod3Index + " : Product ID : _var_commerce_products_aem_recommendations_sample_products_t_te_testcommercestage_myprod" );

        LOGGER.info("Total Rows in Matrix - >>" + matrix.getRowDimension());
        LOGGER.info("Total Columns in Matrix - >>" + matrix.getColumnDimension());

        double prod12Cosine = dotProductMatrix.getEntry(prod1Index, prod2Index);
        double prod13Cosine = dotProductMatrix.getEntry(prod1Index, prod3Index);

        RealVector vector1 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product"));
        RealVector vector2 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_ankit"));
        RealVector vector3 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommercestage_myprod"));
        // Explicitly Getting Cosine for Product 1 & 2, to verify with results in Cosine Matrix
        double product12 = vector1.cosine(vector2);

        LOGGER.info("Cosine Similairty for Product 1 & 2, calculated by explicitly getting Vector for Prod 1 & 3 : " + product12);
        LOGGER.info("Cosine Similairty for Product 1 & 2, calculated in the matrix and read here directly        : " + prod12Cosine);

        // Explicitly Getting Cosine for Product 1 & 3, to verify with results in Cosine Matrix
        double product13 = vector1.cosine(vector3);
        LOGGER.info("Cosine Similairty for Product 1 & 3, calculated by explicitly getting Vector for Prod 1 & 3 : " + product13);
        LOGGER.info("Cosine Similairty for Product 1 & 3, calculated in the matrix and read here directly        : " + prod13Cosine);
    }
}
