package com.odde.doughnut.controllers.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class ApiError {

  @Getter private final String message;
  @Getter private final Map<String, String> errors;

  @Getter private final ErrorType errorType;

  public enum ErrorType {
    OPENAI_UNAUTHORIZED,
    BINDING_ERROR,
    OPENAI_TIMEOUT,
    OPENAI_SERVICE_ERROR,
    WIKIDATA_SERVICE_ERROR,
    ASSESSMENT_SERVICE_ERROR,
    QUESTION_ANSWER_ERROR,
  };

  public ApiError(String message, ErrorType type) {
    this.errorType = type;
    this.message = message;
    this.errors = new HashMap<>();
  }

  public void add(String field, String message) {
    errors.put(field, message);
  }
}
