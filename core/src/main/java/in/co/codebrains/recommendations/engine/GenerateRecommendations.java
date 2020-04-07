package in.co.codebrains.recommendations.engine;

import in.co.codebrains.recommendations.engine.extraction.CountVectorizer;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTermDictionary;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTokenizer;
import in.co.codebrains.recommendations.engine.extraction.util.Tokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GenerateRecommendations {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRecommendations.class);

    public GenerateRecommendations() {

    }

    public void showCountVectorizer(final Map<String, String> productIdBagOfWordsMap) {

        Tokenizer tokenizer = new SimpleTokenizer();
        SimpleTermDictionary dictionary = new SimpleTermDictionary();

        for (Map.Entry<String, String> eachEntry : productIdBagOfWordsMap.entrySet()) {

            String[] tokens = tokenizer.getTokens(eachEntry.getValue());
            dictionary.addTerms(tokens);
        }

        CountVectorizer vectorizer = new CountVectorizer(dictionary, tokenizer);



        for (Map.Entry<String, String> eachEntry : productIdBagOfWordsMap.entrySet()) {

            RealVector productVector = vectorizer.getCountVector(productIdBagOfWordsMap.get(eachEntry.getKey()));

            for (Map.Entry<String, String> eachEntry2 : productIdBagOfWordsMap.entrySet()) {
                if (!StringUtils.equals(eachEntry.getKey(), eachEntry.getKey())) {

                }
            }
        }

        RealMatrix matrix = vectorizer.getCountMatrix(productIdBagOfWordsMap.values());



        RealVector vector1 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product"));
        RealVector vector2 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_ankit"));
        RealVector vector3 = vectorizer.getCountVector(productIdBagOfWordsMap.get("_var_commerce_products_aem_recommendations_sample_products_t_te_testcommercestage_myprod"));


        double product12 = vector1.cosine(vector2);
        double product11 = vector1.cosine(vector1);
        double product22 = vector2.cosine(vector2);

        LOGGER.info("Product _var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product AND  _var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_ankit" + product12);

        LOGGER.info("Cosine similarity 11 -> " + product11);
        LOGGER.info("Cosine similarity 22 -> " + product22);

        double product13 = vector1.cosine(vector3);
        LOGGER.info("Product _var_commerce_products_aem_recommendations_sample_products_t_te_testcommerce_product AND  _var_commerce_products_aem_recommendations_sample_products_t_te_testcommercestage_myprod" + product13);


        for (int i = 0; i < vector1.getDimension(); i++) {
            double entry = vector1.getEntry(i);

            if (entry > 0) {
                LOGGER.info("i == " + i + " Value" + vector1.getEntry(i));

            }
            LOGGER.info("Each Token for vector 1 ->" + vector1.getEntry(i));
        }

        LOGGER.info(":::::::::::::::::::: Below is the Matrix ::::::::::::::::::::" );



        if (matrix != null) {
            LOGGER.info("" + matrix.getNorm());

            double intersection = 0.0D;
            double similarity = 0.0D;
            for (int i = 0; i < matrix.getRowDimension();i++) {
                intersection += Math.min(matrix.getEntry(i, 0), matrix.getEntry(i, 0));
            }
            LOGGER.info("intersection -->" + intersection);
            if (intersection > 0.0D) {
                double union = matrix.getNorm() + matrix.getNorm() - intersection;
                similarity = intersection / union;
            }
            LOGGER.info("similarity" + similarity);
        }


        LOGGER.info(":::::::::::::::::::: ::::::::::::::::::: ::::::::::::::::::::" );


    }
}
