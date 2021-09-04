package in.co.codebrains.recommendations.engine.service;

import in.co.codebrains.recommendations.engine.exceptions.RecommendationEngineNotFoundException;
import in.co.codebrains.recommendations.engine.exceptions.SerializedObjectsNotFound;

import java.util.List;

public interface RecommendationsReaderService {

    List<String> getTopRecommendations(String recommendationEngineName, int topNResults, String nodePath) throws RecommendationEngineNotFoundException, SerializedObjectsNotFound;
}
