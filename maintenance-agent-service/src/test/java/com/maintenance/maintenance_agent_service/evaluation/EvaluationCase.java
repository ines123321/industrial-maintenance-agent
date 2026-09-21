package com.maintenance.maintenance_agent_service.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluationCase(
        String id,
        String category,
        String question,
        String contextSource,
        List<String> expectedKeywords,
        String expectedExactValue,
        String expectedUnit,
        String expectedBehavior,
        String sourceTable,
        String evalMethod,
        String note
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dataset(String description, List<EvaluationCase> cases) {}
}