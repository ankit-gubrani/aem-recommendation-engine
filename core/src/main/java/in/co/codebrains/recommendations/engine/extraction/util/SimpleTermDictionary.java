package in.co.codebrains.recommendations.engine.extraction.util;

import java.util.HashMap;
import java.util.Map;

public class SimpleTermDictionary implements Dictionary {

    private int counter;
    private Map<String, Integer> termIndexMap;

    public SimpleTermDictionary() {
        this.counter = 0;
        this.termIndexMap = new HashMap<>();
    }

    public void addTerm(String term) {
        if (!termIndexMap.containsKey(term)) {
            termIndexMap.put(term, counter++);
        }
    }

    public void addTerms(String[] terms) {
        for (String term : terms) {
            this.addTerm(term);
        }
    }

    @Override
    public Integer getTermIndex(String term) {
        return termIndexMap.get(term);
    }

    @Override
    public int getTotalTerms() {
        return counter;
    }
}
