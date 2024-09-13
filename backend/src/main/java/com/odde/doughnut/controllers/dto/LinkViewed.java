package com.odde.doughnut.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.odde.doughnut.entities.Note;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class LinkViewed {
  @Getter
  @Setter
  @NotNull
  @JsonIgnoreProperties("targetNote")
  private List<Note> reverse;

  public boolean notEmpty() {
    return (reverse != null && !reverse.isEmpty());
  }
}
