package com.maintenance.maintenance_agent_service.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EvaluationDatasetLoader {

    private static final String DATASET_PATH = "/datasets/evaluation-dataset.json";

    public static List<EvaluationCase> load() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        try (InputStream is = EvaluationDatasetLoader.class.getResourceAsStream(DATASET_PATH)) {
            if (is == null) {
                throw new IllegalStateException("Dataset introuvable sur le classpath : " + DATASET_PATH);
            }
            EvaluationCase.Dataset dataset = mapper.readValue(is, EvaluationCase.Dataset.class);
            return dataset.cases();
        } catch (IOException e) {
            throw new IllegalStateException("Erreur de lecture du dataset d'évaluation", e);
        }
    }
}