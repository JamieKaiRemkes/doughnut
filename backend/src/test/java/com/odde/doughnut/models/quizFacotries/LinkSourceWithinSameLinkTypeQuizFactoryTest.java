package com.odde.doughnut.models.quizFacotries;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
class LinkSourceWithinSameLinkTypeQuizFactoryTest {
  @Autowired MakeMe makeMe;
  User user;
  Note top;
  Note target;
  Note source;
  Note anotherSource;
  Note sourceTarget;

  @BeforeEach
  void setup() {
    user = makeMe.aUser().please();
    top = makeMe.aNote().creatorAndOwner(user).please();
    target = makeMe.aNote("sauce").under(top).please();
    source = makeMe.aNote("tomato sauce").under(top).please();
    sourceTarget = makeMe.aReification().between(source, target).please();
    Note cheese = makeMe.aNote("Note cheese").under(top).please();
    anotherSource = makeMe.aNote("blue cheese").under(top).linkTo(cheese).please();
  }

  @Test
  void shouldReturnNullIfCannotFindEnoughOptions() {
    makeMe.aReification().between(anotherSource, target).please();
    assertThat(buildLinkTargetQuizQuestion(), is(nullValue()));
  }

  @Nested
  class WhenThereAreMoreThanOneOptions {
    @Test
    void shouldIncludeRightAnswers() {
      PredefinedQuestion predefinedQuestion = buildLinkTargetQuizQuestion();
      assertThat(
          predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getStem(),
          containsString("Which one <em>is immediately a specialization of</em>:"));
      assertThat(
          predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getStem(),
          containsString(target.getTopicConstructor()));
      List<String> options =
          predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getChoices();
      assertThat(anotherSource.getTopicConstructor(), in(options));
      assertThat(
          "tomato <mark title='Hidden text that is matching the answer'>[...]</mark>", in(options));
    }

    @Test
    void shouldIncludeOneLinkFromEachFillingOptions() {
      makeMe.aReification().between(anotherSource, top).please();
      PredefinedQuestion predefinedQuestion = buildLinkTargetQuizQuestion();
      List<String> options =
          predefinedQuestion.getBareQuestion().getMultipleChoicesQuestion().getChoices();
      assertThat(options, hasSize(2));
    }
  }

  private PredefinedQuestion buildLinkTargetQuizQuestion() {
    return makeMe.buildAQuestionForLinkingNote(
        LinkQuestionType.LINK_SOURCE_WITHIN_SAME_LINK_TYPE, sourceTarget, user);
  }
}
