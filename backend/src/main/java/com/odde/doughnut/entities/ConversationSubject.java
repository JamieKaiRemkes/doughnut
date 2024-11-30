package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ConversationSubject {
  @ManyToOne
  @JoinColumn(name = "assessment_question_instance_id", referencedColumnName = "id")
  private AssessmentQuestionInstance assessmentQuestionInstance;

  @ManyToOne
  @JoinColumn(name = "note_id", referencedColumnName = "id")
  private Note note;

  @ManyToOne
  @JoinColumn(name = "recall_prompt_id", referencedColumnName = "id")
  @JsonIgnore
  private ReviewQuestionInstance reviewQuestionInstance;

  @JsonIgnore
  public boolean isEmpty() {
    return assessmentQuestionInstance == null && note == null && reviewQuestionInstance == null;
  }

  public AnsweredQuestion getAnsweredQuestion() {
    if (reviewQuestionInstance == null) {
      return null;
    }
    return reviewQuestionInstance.getAnsweredQuestion();
  }
}
