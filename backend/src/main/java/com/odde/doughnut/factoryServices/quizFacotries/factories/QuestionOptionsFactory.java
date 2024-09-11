package com.odde.doughnut.factoryServices.quizFacotries.factories;

import com.odde.doughnut.entities.Note;
import com.odde.doughnut.entities.QuizQuestionAndAnswer;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionFactory;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionNotPossibleException;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionServant;
import com.odde.doughnut.services.ai.MultipleChoicesQuestion;
import java.util.ArrayList;
import java.util.List;

public abstract class QuestionOptionsFactory extends QuizQuestionFactory {
  protected final Note note;
  protected final QuizQuestionServant servant;

  public QuestionOptionsFactory(Note note, QuizQuestionServant servant) {
    this.note = note;
    this.servant = servant;
  }

  @Override
  public QuizQuestionAndAnswer buildValidQuizQuestion() throws QuizQuestionNotPossibleException {
    QuizQuestionAndAnswer quizQuestionAndAnswer = new QuizQuestionAndAnswer();
    quizQuestionAndAnswer.setNote(note);
    this.findCategoricalLink();
    this.validateBasicPossibility();
    Note answerNote = this.generateAnswer();
    if (answerNote == null) {
      throw new QuizQuestionNotPossibleException();
    }
    List<? extends Note> options = this.generateFillingOptions();
    if (options.size() < this.minimumOptionCount() - 1) {
      throw new QuizQuestionNotPossibleException();
    }
    List<Note> optionsEntities = new ArrayList<>(options);
    optionsEntities.add(answerNote);
    List<Note> shuffled = servant.randomizer.shuffle(optionsEntities);
    quizQuestionAndAnswer.setCorrectAnswerIndex(shuffled.indexOf(answerNote));
    MultipleChoicesQuestion mcq = new MultipleChoicesQuestion();
    mcq.setStem(getStem());
    mcq.setChoices(shuffled.stream().map(this::noteToChoice).toList());
    quizQuestionAndAnswer.setMultipleChoicesQuestion(mcq);
    return quizQuestionAndAnswer;
  }

  public void validateBasicPossibility() throws QuizQuestionNotPossibleException {}

  public void findCategoricalLink() {}

  public abstract Note generateAnswer();

  public abstract List<? extends Note> generateFillingOptions();

  public int minimumOptionCount() {
    return 2;
  }

  public String noteToChoice(Note note) {
    return note.getTopicConstructor();
  }

  protected abstract String getStem();
}
