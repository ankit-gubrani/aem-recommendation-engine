package in.co.codebrains.recommendations.engine.extraction;

import in.co.codebrains.recommendations.engine.extraction.util.Dictionary;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTermDictionary;
import in.co.codebrains.recommendations.engine.extraction.util.SimpleTokenizer;
import in.co.codebrains.recommendations.engine.extraction.util.Tokenizer;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Collection;

public class CountVectorizer {

    private Dictionary dictionary;
    private Tokenizer tokenizer;

    public CountVectorizer(Dictionary dictionary, Tokenizer tokenizer) {
        this.dictionary = dictionary;
        this.tokenizer = tokenizer;
    }

    public CountVectorizer() {
        this(new SimpleTermDictionary(), new SimpleTokenizer());
    }

    public RealVector getCountVector(String document) {
        RealVector vector = new OpenMapRealVector(this.dictionary.getTotalTerms());
        String[] tokens = tokenizer.getTokens(document);

        for (String token : tokens) {
            // Getting Index of the token from Dictionary
            Integer tokenIndex = this.dictionary.getTermIndex(token);

            if (tokenIndex != null) {
                vector.addToEntry(tokenIndex, 1);
            }
        }
        return vector;
    }

    public RealMatrix getCountMatrix(Collection<String> documents) {

        int rowDimensions = documents.size();
        int colDimensions = this.dictionary.getTotalTerms();

        // OpenMapRealMatrix can be initialized only if rowDimensions & colDimensions both are 1 or greater
        if (rowDimensions < 1 || colDimensions < 1) {
            return null;
        }

        RealMatrix matrix = new OpenMapRealMatrix(rowDimensions, colDimensions);

        int counter = 0;

        for (String document : documents) {
            matrix.setRowVector(counter++, this.getCountVector(document));
        }

        return matrix;
    }
}
