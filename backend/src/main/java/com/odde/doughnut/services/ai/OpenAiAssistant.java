package com.odde.doughnut.services.ai;

import com.odde.doughnut.services.openAiApis.OpenAiApiHandler;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import java.util.List;

public final class OpenAiAssistant {
  private final OpenAiApiHandler openAiApiHandler;
  private final String assistantId;

  public OpenAiAssistant(OpenAiApiHandler openAiApiHandler, String assistantId) {
    this.openAiApiHandler = openAiApiHandler;
    this.assistantId = assistantId;
  }

  public AssistantThread getThread(String threadId) {
    return new AssistantThread(assistantId, threadId, openAiApiHandler);
  }

  public AssistantThread createThread(List<MessageRequest> additionalMessages) {
    ThreadRequest threadRequest = ThreadRequest.builder().messages(additionalMessages).build();
    String threadId = openAiApiHandler.createThread(threadRequest).getId();
    return getThread(threadId);
  }
}
