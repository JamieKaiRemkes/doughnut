package com.odde.doughnut.factoryServices.quizFacotries.factories;

import com.odde.doughnut.algorithms.ClozedString;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.factoryServices.quizFacotries.PredefinedQuestionServant;
import java.util.List;

public class LinkSourceWithinSameLinkTypePredefinedFactory extends QuestionOptionsFactory {
  protected final Note link;
  private List<LinkingNote> cachedFillingOptions = null;

  public LinkSourceWithinSameLinkTypePredefinedFactory(
      Note note, PredefinedQuestionServant servant) {
    super(note, servant);
    this.link = note;
  }

  @Override
  public List<LinkingNote> generateFillingOptions() {
    if (cachedFillingOptions == null) {
      cachedFillingOptions =
          servant.chooseFromCohortAvoidSiblings(link).stream()
              .flatMap(n -> servant.randomizer.chooseOneRandomly(n.getLinks()).stream())
              .toList();
    }
    return cachedFillingOptions;
  }

  @Override
  public Note generateAnswer() {
    return link;
  }

  @Override
  public String noteToChoice(Note note) {
    Note source = note.getParent();
    Note target = note.getTargetNote();
    return ClozedString.htmlClozedString(source.getTopicConstructor())
        .hide(target.getNoteTitle())
        .clozeTitle();
  }

  @Override
  public String getStem() {
    return "Which one <em>is immediately "
        + link.getLinkType().label
        + "</em>:"
        + "<br><br>"
        + "<strong>"
        + link.getTargetNote().getTopicConstructor()
        + "</strong>";
  }
}
