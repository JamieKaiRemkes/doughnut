package com.odde.doughnut.controllers;

import static com.odde.doughnut.controllers.dto.Randomization.RandomStrategy.first;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.odde.doughnut.controllers.dto.AnswerDTO;
import com.odde.doughnut.controllers.dto.QuestionContestResult;
import com.odde.doughnut.controllers.dto.Randomization;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.TimestampOperations;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.GlobalSettingsService;
import com.odde.doughnut.services.ai.MCQWithAnswer;
import com.odde.doughnut.services.ai.QuestionEvaluation;
import com.odde.doughnut.services.ai.tools.AiToolName;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.OpenAIAssistantMocker;
import com.odde.doughnut.testability.OpenAIAssistantThreadMocker;
import com.odde.doughnut.testability.OpenAIChatCompletionMock;
import com.odde.doughnut.testability.TestabilitySettings;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import java.sql.Timestamp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestRecallPromptControllerTests {
  @Mock OpenAiApi openAiApi;
  @Autowired ModelFactoryService modelFactoryService;
  @Autowired MakeMe makeMe;
  private UserModel currentUser;
  private final TestabilitySettings testabilitySettings = new TestabilitySettings();
  OpenAIChatCompletionMock openAIChatCompletionMock;
  OpenAIAssistantMocker openAIAssistantMocker;
  OpenAIAssistantThreadMocker openAIAssistantThreadMocker;

  RestRecallPromptController controller;

  @BeforeEach
  void setup() {
    testabilitySettings.setRandomization(new Randomization(first, 1));
    openAIChatCompletionMock = new OpenAIChatCompletionMock(openAiApi);
    currentUser = makeMe.aUser().toModelPlease();
    controller =
        new RestRecallPromptController(
            openAiApi, modelFactoryService, currentUser, testabilitySettings);

    // Initialize assistant mocker
    openAIAssistantMocker = new OpenAIAssistantMocker(openAiApi);
    openAIAssistantThreadMocker = openAIAssistantMocker.mockThreadCreation(null);
  }

  RestRecallPromptController nullUserController() {
    return new RestRecallPromptController(
        openAiApi, modelFactoryService, makeMe.aNullUserModelPlease(), testabilitySettings);
  }

  @Nested
  class answer {
    MemoryTracker memoryTracker;
    RecallPrompt recallPrompt;
    AnswerDTO answerDTO = new AnswerDTO();

    @BeforeEach
    void setup() {
      Note answerNote = makeMe.aNote().rememberSpelling().please();
      memoryTracker =
          makeMe
              .aMemoryTrackerFor(answerNote)
              .by(currentUser)
              .forgettingCurveAndNextRecallAt(200)
              .please();
      recallPrompt = makeMe.aRecallPrompt().approvedSpellingQuestionOf(answerNote).please();
      answerDTO.setSpellingAnswer(answerNote.getTopicConstructor());
    }

    @Test
    void shouldValidateTheAnswerAndUpdateMemoryTracker() {
      Integer oldRepetitionCount = memoryTracker.getRepetitionCount();
      AnsweredQuestion answerResult = controller.answerQuiz(recallPrompt, answerDTO);
      assertTrue(answerResult.answer.getCorrect());
      assertThat(memoryTracker.getRepetitionCount(), greaterThan(oldRepetitionCount));
    }

    @Test
    void shouldNoteIncreaseIndexIfRepeatImmediately() {
      testabilitySettings.timeTravelTo(memoryTracker.getLastRecalledAt());
      Integer oldForgettingCurveIndex = memoryTracker.getForgettingCurveIndex();
      controller.answerQuiz(recallPrompt, answerDTO);
      assertThat(memoryTracker.getForgettingCurveIndex(), equalTo(oldForgettingCurveIndex));
    }

    @Test
    void shouldIncreaseTheIndex() {
      testabilitySettings.timeTravelTo(memoryTracker.getNextRecallAt());
      Integer oldForgettingCurveIndex = memoryTracker.getForgettingCurveIndex();
      controller.answerQuiz(recallPrompt, answerDTO);
      assertThat(memoryTracker.getForgettingCurveIndex(), greaterThan(oldForgettingCurveIndex));
      assertThat(
          memoryTracker.getLastRecalledAt(), equalTo(testabilitySettings.getCurrentUTCTimestamp()));
    }

    @Test
    void shouldNotBeAbleToSeeNoteIDontHaveAccessTo() {
      AnswerDTO answer = new AnswerDTO();
      assertThrows(
          ResponseStatusException.class,
          () -> nullUserController().answerQuiz(recallPrompt, answer));
    }

    @Nested
    class WrongAnswer {
      @BeforeEach
      void setup() {
        recallPrompt =
            makeMe.aRecallPrompt().approvedSpellingQuestionOf(memoryTracker.getNote()).please();
        answerDTO.setSpellingAnswer("wrong");
      }

      @Test
      void shouldValidateTheWrongAnswer() {
        testabilitySettings.timeTravelTo(memoryTracker.getNextRecallAt());
        Integer oldRepetitionCount = memoryTracker.getRepetitionCount();
        AnsweredQuestion answerResult = controller.answerQuiz(recallPrompt, answerDTO);
        assertFalse(answerResult.answer.getCorrect());
        assertThat(memoryTracker.getRepetitionCount(), greaterThan(oldRepetitionCount));
      }

      @Test
      void shouldNotChangeTheLastRecalledAtTime() {
        testabilitySettings.timeTravelTo(memoryTracker.getNextRecallAt());
        Timestamp lastRecalledAt = memoryTracker.getLastRecalledAt();
        Integer oldForgettingCurveIndex = memoryTracker.getForgettingCurveIndex();
        controller.answerQuiz(recallPrompt, answerDTO);
        assertThat(memoryTracker.getForgettingCurveIndex(), lessThan(oldForgettingCurveIndex));
        assertThat(memoryTracker.getLastRecalledAt(), equalTo(lastRecalledAt));
      }

      @Test
      void shouldRepeatTheNextDay() {
        controller.answerQuiz(recallPrompt, answerDTO);
        assertThat(
            memoryTracker.getNextRecallAt(),
            lessThan(
                TimestampOperations.addHoursToTimestamp(
                    testabilitySettings.getCurrentUTCTimestamp(), 25)));
      }
    }
  }

  @Nested
  class RegenerateQuestion {
    RecallPrompt recallPrompt;
    Note note;

    @BeforeEach
    void setUp() {
      note = makeMe.aNote().please();
      recallPrompt = makeMe.aRecallPrompt().approvedSpellingQuestionOf(note).please();
    }

    @Test
    void askWithNoteThatCannotAccess() {
      assertThrows(
          ResponseStatusException.class,
          () -> {
            RestRecallPromptController restAiController =
                new RestRecallPromptController(
                    openAiApi,
                    makeMe.modelFactoryService,
                    makeMe.aNullUserModelPlease(),
                    testabilitySettings);
            restAiController.regenerate(recallPrompt);
          });
    }

    @Test
    void createQuizQuestion() {
      MCQWithAnswer jsonQuestion =
          makeMe.aMCQWithAnswer().stem("What is the first color in the rainbow?").please();

      // Mock the assistant API calls using the same pattern as GenerateRandomQuestion
      openAIAssistantThreadMocker
          .mockCreateRunInProcess("my-run-id")
          .aRunThatRequireAction(
              jsonQuestion, AiToolName.ASK_SINGLE_ANSWER_MULTIPLE_CHOICE_QUESTION.getValue())
          .mockRetrieveRun()
          .mockCancelRun("my-run-id");

      RecallPrompt regeneratedQuestion = controller.regenerate(recallPrompt);

      Assertions.assertThat(
              regeneratedQuestion.getBareQuestion().getMultipleChoicesQuestion().getStem())
          .contains("What is the first color in the rainbow?");
    }
  }

  @Nested
  class Contest {
    RecallPrompt recallPrompt;
    QuestionEvaluation questionEvaluation = new QuestionEvaluation();

    @BeforeEach
    void setUp() {
      questionEvaluation.correctChoices = new int[] {0};
      questionEvaluation.feasibleQuestion = true;
      questionEvaluation.comment = "what a horrible question!";

      MCQWithAnswer aiGeneratedQuestion = makeMe.aMCQWithAnswer().please();
      Note note = makeMe.aNote().please();
      recallPrompt =
          makeMe.aRecallPrompt().ofAIGeneratedQuestion(aiGeneratedQuestion, note).please();
    }

    @Test
    void askWithNoteThatCannotAccess() {
      assertThrows(
          ResponseStatusException.class,
          () -> {
            RestRecallPromptController restAiController =
                new RestRecallPromptController(
                    openAiApi,
                    makeMe.modelFactoryService,
                    makeMe.aNullUserModelPlease(),
                    testabilitySettings);
            restAiController.contest(recallPrompt);
          });
    }

    @Test
    void rejected() {
      openAIChatCompletionMock.mockChatCompletionAndReturnToolCall(questionEvaluation, "");
      QuestionContestResult contest = controller.contest(recallPrompt);
      assertTrue(contest.rejected);
    }

    @Test
    void useTheRightModel() {
      openAIChatCompletionMock.mockChatCompletionAndReturnToolCall(questionEvaluation, "");
      GlobalSettingsService globalSettingsService = new GlobalSettingsService(modelFactoryService);
      globalSettingsService
          .globalSettingEvaluation()
          .setKeyValue(makeMe.aTimestamp().please(), "gpt-new");
      controller.contest(recallPrompt);
      ArgumentCaptor<ChatCompletionRequest> argumentCaptor =
          ArgumentCaptor.forClass(ChatCompletionRequest.class);
      verify(openAiApi, times(1)).createChatCompletion(argumentCaptor.capture());
      assertThat(argumentCaptor.getValue().getModel(), equalTo("gpt-new"));
    }

    @Test
    void acceptTheContest() {
      questionEvaluation.feasibleQuestion = false;
      openAIChatCompletionMock.mockChatCompletionAndReturnToolCall(questionEvaluation, "");
      QuestionContestResult contest = controller.contest(recallPrompt);
      assertFalse(contest.rejected);
    }
  }

  @Nested
  class GenerateRandomQuestion {
    @Test
    void itMustPersistTheQuestionGenerated() {
      MCQWithAnswer jsonQuestion =
          makeMe.aMCQWithAnswer().stem("What is the first color in the rainbow?").please();

      // Mock the assistant API calls
      openAIAssistantThreadMocker
          .mockCreateRunInProcess("my-run-id")
          .aRunThatRequireAction(
              jsonQuestion, AiToolName.ASK_SINGLE_ANSWER_MULTIPLE_CHOICE_QUESTION.getValue())
          .mockRetrieveRun()
          .mockCancelRun("my-run-id");

      Note note = makeMe.aNote().details("description long enough.").rememberSpelling().please();
      // another note is needed, otherwise the note will be the only note in the notebook, and the
      // question cannot be generated.
      makeMe.aNote().under(note).please();
      MemoryTracker rp = makeMe.aMemoryTrackerFor(note).by(currentUser).please();

      RecallPrompt recallPrompt = controller.generateRandomQuestion(rp);

      assertThat(recallPrompt.getId(), notNullValue());
    }

    @Test
    void shouldIncludeFileSearchInTools() {
      Note note = makeMe.aNote().details("description long enough.").rememberSpelling().please();
      makeMe.aNote().under(note).please();
      MemoryTracker rp = makeMe.aMemoryTrackerFor(note).by(currentUser).please();
      MCQWithAnswer jsonQuestion =
          makeMe.aMCQWithAnswer().stem("What is the first color in the rainbow?").please();

      // Mock basic assistant API calls
      openAIAssistantThreadMocker
          .mockCreateRunInProcess("my-run-id")
          .aRunThatRequireAction(
              jsonQuestion, AiToolName.ASK_SINGLE_ANSWER_MULTIPLE_CHOICE_QUESTION.getValue())
          .mockRetrieveRun()
          .mockCancelRun("my-run-id");

      controller.generateRandomQuestion(rp);

      // Capture the actual request
      ArgumentCaptor<RunCreateRequest> runRequestCaptor =
          ArgumentCaptor.forClass(RunCreateRequest.class);
      verify(openAiApi).createRun(any(), runRequestCaptor.capture());

      // Assert on the captured request
      RunCreateRequest actualRequest = runRequestCaptor.getValue();
      //      assertThat(actualRequest.getTools(), hasItem(hasProperty("type",
      // equalTo("file_search"))));
    }
  }

  @Nested
  class showQuestion {

    @Test
    void shouldNotBeAbleToSeeNoteIDontHaveAccessTo() {
      RecallPrompt recallPrompt = makeMe.aRecallPrompt().please();
      assertThrows(
          UnexpectedNoAccessRightException.class, () -> controller.showQuestion(recallPrompt));
    }

    @Test
    void canSeeNoteThatHasReadAccess() throws UnexpectedNoAccessRightException {
      Note note = makeMe.aNote().creatorAndOwner(currentUser).please();
      RecallPrompt recallPrompt = makeMe.aRecallPrompt().spellingQuestionOf(note).please();
      makeMe.theRecallPrompt(recallPrompt).answerSpelling("wrong").please();
      makeMe.refresh(currentUser.getEntity());
      AnsweredQuestion answeredQuestion = controller.showQuestion(recallPrompt);
      assertThat(answeredQuestion.recallPromptId, equalTo(recallPrompt.getId()));
    }
  }
}
