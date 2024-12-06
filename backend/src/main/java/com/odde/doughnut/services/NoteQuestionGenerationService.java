package com.odde.doughnut.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odde.doughnut.services.ai.MCQWithAnswer;
import com.odde.doughnut.services.ai.tools.AiToolFactory;
import com.theokanning.openai.assistants.message.MessageRequest;
import java.util.List;
import org.apache.logging.log4j.util.Strings;

public class NoteQuestionGenerationService {
  protected final GlobalSettingsService globalSettingsService;
  private final NotebookAssistantForNoteService notebookAssistantForNoteService;

  public NoteQuestionGenerationService(
      GlobalSettingsService globalSettingsService,
      NotebookAssistantForNoteService notebookAssistantForNoteService) {
    this.globalSettingsService = globalSettingsService;
    this.notebookAssistantForNoteService = notebookAssistantForNoteService;
  }

  public MCQWithAnswer generateQuestion() throws JsonProcessingException {
    MessageRequest message =
        MessageRequest.builder()
            .role("user")
            .content(AiToolFactory.mcqWithAnswerAiTool().getMessageBody())
            .build();

    MCQWithAnswer question =
        notebookAssistantForNoteService
            .createThreadWithNoteInfo1(List.of(message))
            .withTool(AiToolFactory.askSingleAnswerMultipleChoiceQuestion())
            //            .withFileSearch()
            .withModelName(globalSettingsService.globalSettingQuestionGeneration().getValue())
            .run()
            .getRunResult()
            .getAssumedToolCallArgument(MCQWithAnswer.class);
    if (question != null
        && question.getMultipleChoicesQuestion().getStem() != null
        && !Strings.isBlank(question.getMultipleChoicesQuestion().getStem())) {
      return question;
    }
    return null;
  }
}
