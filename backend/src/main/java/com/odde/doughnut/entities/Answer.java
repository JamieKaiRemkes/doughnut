package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "quiz_answer")
public class Answer extends EntityIdentifiedByIdOnly {
  @Column(name = "answer")
  String spellingAnswer;

  @Column(name = "choice_index")
  Integer choiceIndex;

  @Column(name = "created_at")
  @Setter
  @JsonIgnore
  private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

  @Column(name = "correct")
  @Setter
  @NotNull
  private Boolean correct;

  @JsonIgnore
  String getAnswerDisplay(BareQuestion bareQuestion) {
    if (getChoiceIndex() != null) {
      return bareQuestion.getMultipleChoicesQuestion().getChoices().get(getChoiceIndex());
    }
    return getSpellingAnswer();
  }
}
