package com.odde.doughnut.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odde.doughnut.entities.Note;
import com.odde.doughnut.entities.NotebookAiAssistant;
import com.odde.doughnut.services.ai.AssistantThread;
import com.odde.doughnut.services.ai.OpenAiAssistant;
import com.odde.doughnut.services.graphRAG.CharacterBasedTokenCountingStrategy;
import com.odde.doughnut.services.graphRAG.GraphRAGResult;
import com.theokanning.openai.assistants.message.MessageRequest;
import java.util.ArrayList;
import java.util.List;

public class NotebookAssistantForNoteService {
  private final OpenAiAssistant assistantService;
  private final Note note;

  public NotebookAssistantForNoteService(OpenAiAssistant openAiAssistant, Note note) {
    this.assistantService = openAiAssistant;
    this.note = note;
  }

  protected AssistantThread createThreadWithNoteInfo(List<MessageRequest> additionalMessages) {
    List<MessageRequest> messages = new ArrayList<>();
    messages.add(
        MessageRequest.builder().role("assistant").content(note.getNoteDescription()).build());
    if (!additionalMessages.isEmpty()) {
      messages.addAll(additionalMessages);
    }
    return assistantService.createThread(messages, getNotebookAssistantInstructions());
  }

  protected AssistantThread createThreadWithNoteInfo1(List<MessageRequest> additionalMessages) {
    GraphRAGService graphRAGService =
        new GraphRAGService(new CharacterBasedTokenCountingStrategy());
    GraphRAGResult retrieve = graphRAGService.retrieve(note, 5000);
    String prettyString = new ObjectMapper().valueToTree(retrieve).toPrettyString();
    String noteDescription =
        """
        Focus Note and the notes related to it:
        %s
        """
            .formatted(prettyString);
    List<MessageRequest> messages = new ArrayList<>();
    messages.add(MessageRequest.builder().role("assistant").content(noteDescription).build());
    if (!additionalMessages.isEmpty()) {
      messages.addAll(additionalMessages);
    }
    return assistantService.createThread(messages, getNotebookAssistantInstructions());
  }

  public AssistantThread getThread(String threadId) {
    return assistantService.getThread(threadId, getNotebookAssistantInstructions());
  }

  private String getNotebookAssistantInstructions() {
    NotebookAiAssistant notebookAiAssistant = note.getNotebook().getNotebookAiAssistant();
    if (notebookAiAssistant == null) {
      return null;
    }
    return notebookAiAssistant.getAdditionalInstructionsToAi();
  }
}
