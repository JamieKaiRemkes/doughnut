package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.dto.AnswerDTO;
import com.odde.doughnut.controllers.dto.QuestionContestResult;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.RecallQuestionService;
import com.odde.doughnut.testability.TestabilitySettings;
import com.theokanning.openai.client.OpenAiApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recall-prompts")
class RestRecallPromptController {
  private final UserModel currentUser;

  @Resource(name = "testabilitySettings")
  private final TestabilitySettings testabilitySettings;

  private final RecallQuestionService recallQuestionService;

  public RestRecallPromptController(
      @Qualifier("testableOpenAiApi") OpenAiApi openAiApi,
      ModelFactoryService modelFactoryService,
      UserModel currentUser,
      TestabilitySettings testabilitySettings) {
    this.currentUser = currentUser;
    this.testabilitySettings = testabilitySettings;
    this.recallQuestionService =
        new RecallQuestionService(
            openAiApi, modelFactoryService, testabilitySettings.getRandomizer());
  }

  @PostMapping("/generate-question")
  @Transactional
  public RecallPrompt generateQuestion(
      @RequestParam(value = "note") @Schema(type = "integer") Note note) {
    currentUser.assertLoggedIn();
    return recallQuestionService.generateAQuestionOfRandomType(note, currentUser.getEntity());
  }

  @GetMapping("/{memoryTracker}/random-question")
  @Transactional
  public RecallPrompt generateRandomQuestion(
      @PathVariable("memoryTracker") @Schema(type = "integer") MemoryTracker memoryTracker) {
    currentUser.assertLoggedIn();
    return recallQuestionService.generateAQuestionOfRandomType(
        memoryTracker.getNote(), currentUser.getEntity());
  }

  @PostMapping("/{recallPrompt}/regenerate")
  @Transactional
  public RecallPrompt regenerate(
      @PathVariable("recallPrompt") @Schema(type = "integer") RecallPrompt recallPrompt) {
    currentUser.assertLoggedIn();
    return recallQuestionService.generateAQuestionOfRandomType(
        recallPrompt.getPredefinedQuestion().getNote(), currentUser.getEntity());
  }

  @PostMapping("/{recallPrompt}/contest")
  @Transactional
  public QuestionContestResult contest(
      @PathVariable("recallPrompt") @Schema(type = "integer") RecallPrompt recallPrompt) {
    currentUser.assertLoggedIn();
    return recallQuestionService.contest(recallPrompt);
  }

  @PostMapping("/{recallPrompt}/answer")
  @Transactional
  public AnsweredQuestion answerQuiz(
      @PathVariable("recallPrompt") @Schema(type = "integer") RecallPrompt recallPrompt,
      @Valid @RequestBody AnswerDTO answerDTO) {
    currentUser.assertLoggedIn();

    return recallQuestionService.answerQuestion(
        recallPrompt,
        answerDTO,
        currentUser.getEntity(),
        testabilitySettings.getCurrentUTCTimestamp());
  }

  @GetMapping(path = "/{recallPrompt}")
  @Transactional
  public AnsweredQuestion showQuestion(
      @PathVariable("recallPrompt") @Schema(type = "integer") RecallPrompt recallPrompt)
      throws UnexpectedNoAccessRightException {
    currentUser.assertReadAuthorization(recallPrompt);
    return recallPrompt.getAnsweredQuestion();
  }
}
