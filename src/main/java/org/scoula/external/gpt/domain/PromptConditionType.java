package org.scoula.external.gpt.domain;

public enum PromptConditionType {
    POSITIVE("긍정 조건"),
    NEGATIVE("부정 조건");
    
    private final String description;
    
    PromptConditionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}