package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class NotebookSettings {
  @Column(name = "skip_review_entirely")
  Boolean skipReviewEntirely = false;

  @Column(name = "number_of_questions_in_assessment")
  Integer numberOfQuestionsInAssessment;

  @JsonIgnore
  public void update(NotebookSettings value) {
    setSkipReviewEntirely(value.getSkipReviewEntirely());
    setNumberOfQuestionsInAssessment(value.getNumberOfQuestionsInAssessment());
  }
}
