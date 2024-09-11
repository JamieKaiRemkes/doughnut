package com.odde.doughnut.models.quizFacotries;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionFactory;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionNotPossibleException;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionServant;
import com.odde.doughnut.factoryServices.quizFacotries.factories.DescriptionLinkTargetQuizFactory;
import com.odde.doughnut.models.randomizers.NonRandomizer;
import com.odde.doughnut.testability.MakeMe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DescriptionLinkTargetQuizFactoryTest {

  @Autowired MakeMe makeMe;
  User user;
  Note top;
  Note target;
  Note source;
  Note anotherSource;
  ReviewPoint reviewPoint;
  LinkingNote subjectNote;

  @BeforeEach
  void setup() {
    user = makeMe.aUser().please();
    top = makeMe.aNote().creatorAndOwner(user).please();
    target = makeMe.aNote("rome").under(top).please();
    source =
        makeMe
            .aNote("saying")
            .details("Rome is not built in a day")
            .under(top)
            .linkTo(target)
            .please();
    subjectNote = source.getLinks().get(0);
    reviewPoint = makeMe.aReviewPointFor(subjectNote).by(user).inMemoryPlease();
    anotherSource = makeMe.aNote("pompeii").under(top).please();
  }

  @Nested
  class WhenTheNoteDoesnotHaveDescription {
    @BeforeEach
    void setup() {
      makeMe.theNote(source).details("").please();
    }

    @Test
    void shouldBeInvalid() {
      assertThat(buildQuestion(), nullValue());
    }
  }

  @Test
  void shouldIncludeRightAnswers() {
    QuizQuestionAndAnswer quizQuestionAndAnswer = buildQuestion();
    assertThat(
        quizQuestionAndAnswer.getMultipleChoicesQuestion().getStem(),
        containsString(
            "<p>The following descriptions is a specialization of:</p><p><mark title='Hidden text that is matching the answer'>[...]</mark> is not built in a day</p>\n"));
  }

  @Test
  void shouldIncludeMasks() {
    makeMe.theNote(source).titleConstructor("token").details("token /.").please();
    QuizQuestionAndAnswer quizQuestionAndAnswer = buildQuestion();
    assertThat(
        quizQuestionAndAnswer.getMultipleChoicesQuestion().getStem(),
        containsString("<mark title='Hidden text that is matching the answer'>[...]</mark> /."));
  }

  @Nested
  class Answer {
    @Test
    void correct() {
      AnsweredQuestion answerResult =
          makeMe
              .anAnswerViewedByUser()
              .validQuestionOfType(getQuizQuestionFactory())
              .choiceIndex(1)
              .inMemoryPlease();
      assertTrue(answerResult.correct);
    }
  }

  private QuizQuestionAndAnswer buildQuestion() {
    try {
      return getQuizQuestionFactory().buildValidQuizQuestion();
    } catch (QuizQuestionNotPossibleException e) {
      return null;
    }
  }

  private QuizQuestionFactory getQuizQuestionFactory() {
    QuizQuestionServant servant =
        new QuizQuestionServant(user, new NonRandomizer(), makeMe.modelFactoryService);
    return new DescriptionLinkTargetQuizFactory(subjectNote, servant);
  }
}
