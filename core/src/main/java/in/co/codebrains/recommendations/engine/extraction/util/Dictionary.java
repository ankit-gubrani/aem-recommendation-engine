package in.co.codebrains.recommendations.engine.extraction.util;

public interface Dictionary {

    /**
     * Method which retuns the location in a vector of the term.
     * @param term
     * @return
     */
    Integer getTermIndex(String term);

    /**
     * Method which returns the number of terms in the dictionary. That could be used for
     * creating vectors as well as a method that returns the index of particular term.
     * @return
     */
    int getTotalTerms();
}
