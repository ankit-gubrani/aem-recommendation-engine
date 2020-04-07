package in.co.codebrains.recommendations.engine.extraction.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SimpleTokenizer implements Tokenizer {

    private int minTokenSize;

    public SimpleTokenizer(int minTokenSize) {
        this.minTokenSize = minTokenSize;
    }

    public SimpleTokenizer() {
        this(0);
    }

    public String[] getTokens(String document) {

        if (StringUtils.isEmpty(document)) {
            return new String[0];
        }

        String[] tokens = document.trim().split("\\s+");
        List<String> cleanTokens = new ArrayList<>();

        for (String token : tokens) {
            String cleanToken = token.trim().toLowerCase()
                    .replaceAll("[^A-Za-z\']+", "");

            if (cleanToken.length() > minTokenSize) {
                cleanTokens.add(cleanToken);
            }
        }

        return cleanTokens.toArray(new String[0]);
    }

}
