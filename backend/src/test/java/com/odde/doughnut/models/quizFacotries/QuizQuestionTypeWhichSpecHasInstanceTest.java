package com.odde.doughnut.models.quizFacotries;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.nullValue;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.services.LinkQuestionType;
import com.odde.doughnut.testability.MakeMe;
import java.util.List;
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
class WhichSpecHasInstanceQuizFactoryTest {
  @Autowired MakeMe makeMe;
  User user;
  Note top;
  Note target;
  Note source;
  Note anotherSource;
  LinkingNote subjectNote;

  @BeforeEach
  void setup() {
    user = makeMe.aUser().please();
    top = makeMe.aNote("top").creatorAndOwner(user).please();
    target = makeMe.aNote("element").under(top).please();
    source = makeMe.aNote("noble gas").under(top).linkTo(target, LinkType.SPECIALIZE).please();
    anotherSource = makeMe.aNote("non-official name").under(top).please();
    subjectNote = source.getLinks().get(0);
  }

  @Test
  void shouldBeInvalidWhenNoInsatnceOfLink() {
    assertThat(buildQuestion(), nullValue());
  }

  @Nested
  class WhenTheNoteHasInstance {
    @BeforeEach
    void setup() {
      makeMe.theNote(source).linkTo(anotherSource, LinkType.INSTANCE).please();
    }

    @Test
    void shouldBeInvalidWhenNoInsatnceOfLink() {
      assertThat(buildQuestion(), nullValue());
    }

    @Nested
    class WhenTheNoteHasMoreSpecificationSiblings {
      Note metal;

      @BeforeEach
      void setup() {
        metal = makeMe.aNote("metal").under(top).linkTo(target, LinkType.SPECIALIZE).please();
      }

      @Test
      void shouldBeInvalidWhenNoViceReviewPoint() {
        assertThat(buildQuestion(), nullValue());
      }

      @Nested
      class WhenTheSecondLinkHasReviewPoint {

        @BeforeEach
        void setup() {
          Note link = source.getLinks().get(1);

          makeMe.aReviewPointFor(link).by(user).please();
        }

        @Test
        void shouldIncludeRightAnswers() {
          QuizQuestionAndAnswer quizQuestionAndAnswer = buildQuestion();
          assertThat(
              quizQuestionAndAnswer.getMultipleChoicesQuestion().getStem(),
              containsString(
                  "<p>Which one is a specialization of <mark>element</mark> <em>and</em> is an instance of <mark>non-official name</mark>:"));
          List<String> strings = quizQuestionAndAnswer.getMultipleChoicesQuestion().getChoices();
          assertThat("metal", in(strings));
          assertThat(source.getTopicConstructor(), in(strings));
        }

        @Nested
        class PersonAlsoHasTheSameNoteAsInstance {

          @BeforeEach
          void setup() {
            makeMe.theNote(metal).linkTo(anotherSource, LinkType.INSTANCE).please();
          }

          @Test
          void shouldBeInvalid() {
            assertThat(buildQuestion(), nullValue());
          }
        }

        @Nested
        class ChoiceFromInstance {

          @BeforeEach
          void setup() {
            makeMe
                .aNote("something else")
                .under(top)
                .linkTo(anotherSource, LinkType.INSTANCE)
                .please();
          }

          @Test
          void options() {
            QuizQuestionAndAnswer quizQuestionAndAnswer = buildQuestion();
            List<String> strings = quizQuestionAndAnswer.getMultipleChoicesQuestion().getChoices();
            assertThat("something else", in(strings));
          }
        }
      }
    }
  }

  private QuizQuestionAndAnswer buildQuestion() {
    return makeMe.buildAQuestionForLinkingNote(
        LinkQuestionType.WHICH_SPEC_HAS_INSTANCE, subjectNote, user);
  }
}
