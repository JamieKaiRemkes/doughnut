package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odde.doughnut.factoryServices.quizFacotries.PredefinedQuestionFactory;
import com.odde.doughnut.factoryServices.quizFacotries.PredefinedQuestionServant;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "linking_note")
@PrimaryKeyJoinColumn(name = "note_id")
public class LinkingNote extends Note {
  private LinkingNote() {}

  public static LinkingNote createLink(
      User user, Note parentNote, Timestamp currentUTCTimestamp, String topicConstructor) {
    final LinkingNote note = new LinkingNote();
    note.initialize(user, parentNote, currentUTCTimestamp, topicConstructor);
    return note;
  }

  @JsonIgnore
  public Stream<LinkingNote> getSiblingLinksOfSameLinkType(User user) {
    return targetNoteViewer(user)
        .linksOfTypeThroughReverse(getLinkType())
        .filter(l -> !l.equals(this));
  }

  @JsonIgnore
  public List<Note> getLinkedSiblingsOfSameLinkType(User user) {
    return getSiblingLinksOfSameLinkType(user).map(Note::getParent).toList();
  }

  @Override
  public List<PredefinedQuestionFactory> getPredefinedQuestionFactories(
      PredefinedQuestionServant servant) {
    return Arrays.stream(getLinkType().getQuestionTypes())
        .map(t -> t.factoryForLinkingNote.apply(this, servant))
        .toList();
  }
}
