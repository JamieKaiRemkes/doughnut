package com.odde.doughnut.services.ai.tools;

import com.theokanning.openai.function.FunctionDefinition;
import lombok.Getter;

public class InstructionAndSchema {
  @Getter private final FunctionDefinition functionDefinition;
  @Getter private String messageBody;

  public InstructionAndSchema(String message, AiTool aiTool) {
    this.messageBody = message;
    this.functionDefinition = aiTool.getFunctionDefinition();
  }
}
