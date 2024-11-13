package com.odde.doughnut.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odde.doughnut.services.ai.tools.AiToolName;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.OpenAIChatCompletionMock;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.assistants.assistant.Tool;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.function.FunctionDefinition;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiServiceFactoryAssistantsTest {

  private AiAdvisorWithStorageService aiAdvisorServiceWithStorage;
  @Autowired MakeMe makeMe;
  @Mock private OpenAiApi openAiApi;
  OpenAIChatCompletionMock openAIChatCompletionMock;

  @BeforeEach
  void Setup() {
    MockitoAnnotations.openMocks(this);
    openAIChatCompletionMock = new OpenAIChatCompletionMock(openAiApi);
    aiAdvisorServiceWithStorage =
        new AiAdvisorWithStorageService(
            new AiServiceFactory(openAiApi), makeMe.modelFactoryService);
  }

  @Nested
  class CreateAssistants {
    AssistantRequest assistantRequest;

    @BeforeEach
    void captureTheRequest() {
      Assistant item = new Assistant();
      item.setId("1234");
      when(openAiApi.createAssistant(ArgumentMatchers.any())).thenReturn(Single.just(item));
      aiAdvisorServiceWithStorage.recreateDefaultAssistants(makeMe.aTimestamp().please());
      ArgumentCaptor<AssistantRequest> captor = ArgumentCaptor.forClass(AssistantRequest.class);
      verify(openAiApi).createAssistant(captor.capture());
      assistantRequest = captor.getValue();
    }

    @Test
    void getAiSuggestion_givenAString_returnsAiSuggestionObject() {
      assertThat(assistantRequest.getName(), is("Note details completion"));
      assertThat(assistantRequest.getInstructions(), containsString("PKM system"));
      assertThat(assistantRequest.getTools(), hasSize(2));
    }

    @Test
    void parameters() {
      Tool tool = assistantRequest.getTools().get(0);
      assertThat(tool.getType(), is("function"));
      assertThat(tool, instanceOf(FunctionTool.class));
      FunctionTool functionTool = (FunctionTool) tool;
      FunctionDefinition functionDefinition = (FunctionDefinition) functionTool.getFunction();
      assertThat(functionDefinition.getName(), is(AiToolName.COMPLETE_NOTE_DETAILS.getValue()));
      //      assertThat(functionDefinition.getStrict(), is(true));
    }
  }
}
