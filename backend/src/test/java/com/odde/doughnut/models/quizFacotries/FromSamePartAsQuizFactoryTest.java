package com.odde.doughnut.models.quizFacotries;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
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
class FromSamePartAsQuizFactoryTest {
  @Autowired MakeMe makeMe;
  User user;
  Note top;
  Note perspective;
  Note subjective;
  Note objective;
  Note ugly;
  Note pretty;
  Note tall;
  Note subjectivePerspective;
  Note uglySubjective;

  @BeforeEach
  void setup() {
    user = makeMe.aUser().please();
    top = makeMe.aNote("top").creatorAndOwner(user).please();
    perspective = makeMe.aNote("perspective").under(top).please();
    subjective = makeMe.aNote("subjective").under(top).please();
    objective = makeMe.aNote("objective").under(top).please();
    ugly = makeMe.aNote("ugly").under(top).please();
    pretty = makeMe.aNote("pretty").under(top).please();
    tall = makeMe.aNote("tall").under(top).please();
    subjectivePerspective =
        makeMe.aReification().between(subjective, perspective, LinkType.PART).please();
    makeMe.aReification().between(objective, perspective, LinkType.PART).please();
    uglySubjective = makeMe.aReification().between(ugly, subjective, LinkType.TAGGED_BY).please();
  }

  @Test
  void shouldBeInvalidWhenNoCousin() {
    assertThat(buildQuestion(), nullValue());
  }

  @Nested
  class WhenThereIsAnCousin {
    Note cousin;

    @BeforeEach
    void setup() {
      cousin = makeMe.aReification().between(pretty, subjective, LinkType.TAGGED_BY).please();
    }

    @Test
    void shouldBeInvalidWhenNoFillingOptions() {
      assertThat(buildQuestion(), nullValue());
    }

    @Nested
    class WhenThereIsFillingChoice {

      @BeforeEach
      void setup() {
        makeMe.aReification().between(tall, objective, LinkType.TAGGED_BY).please();
      }

      @Test
      void shouldBeInvalidWhenNoViceMemoryTracker() {
        assertThat(buildQuestion(), nullValue());
      }

      @Nested
      class WhenThereIsViceMemoryTracker {
        @BeforeEach
        void setup() {
          makeMe.aMemoryTrackerFor(cousin).by(user).please();
        }

        @Test
        void shouldIncludeRightAnswersAndFillingOptions() {
          PredefinedQuestion predefinedQuestion = buildQuestion();
          assertThat(
              predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getStem(),
              containsString(
                  "<p>Which one <mark>is tagged by</mark> the same part of <mark>perspective</mark> as:"));
          assertThat(
              predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getStem(),
              containsString(ugly.getTopicConstructor()));
          List<String> strings =
              predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getChoices();
          assertThat(pretty.getTopicConstructor(), in(strings));
          assertThat(tall.getTopicConstructor(), in(strings));
          assertThat(ugly.getTopicConstructor(), not(in(strings)));
        }
      }
    }
  }

  private PredefinedQuestion buildQuestion() {
    return makeMe.buildAQuestionForLinkingNote(
        LinkQuestionType.FROM_SAME_PART_AS, uglySubjective, user);
  }
}
