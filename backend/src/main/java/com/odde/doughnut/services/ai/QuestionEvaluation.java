package com.odde.doughnut.services.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.odde.doughnut.controllers.dto.QuestionContestResult;
import java.util.Arrays;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionEvaluation {
  @JsonPropertyDescription("Indices of the correct choices. 0-based.")
  @JsonProperty(required = true)
  public int[] correctChoices;

  @JsonPropertyDescription("Whether the question is feasible.")
  @JsonProperty(required = true)
  public boolean feasibleQuestion;

  @JsonPropertyDescription(
      "Explains why the question is not feasible and advises for improvement. Leave empty if the question is feasible.")
  @JsonProperty(required = true)
  public String improvementAdvices;

  private boolean indisputableAnswer(int correctChoiceIndex) {
    return correctChoices != null
        && correctChoices.length == 1
        && correctChoices[0] == correctChoiceIndex;
  }

  public QuestionContestResult getQuestionContestResult(MCQWithAnswer mcqWithAnswer) {
    int correctChoiceIndex = mcqWithAnswer.getCorrectChoiceIndex();
    if (feasibleQuestion && indisputableAnswer(correctChoiceIndex)) {
      QuestionContestResult result = new QuestionContestResult();
      result.advice = "This seems to be a legitimate question. Please answer it.";
      result.rejected = true;
      return result;
    }
    QuestionContestResult result = new QuestionContestResult();
    result.advice = "";
    if (!indisputableAnswer(correctChoiceIndex)) {
      String correctChoicesStr =
          correctChoices == null
              ? "none"
              : Arrays.stream(correctChoices)
                  .mapToObj(
                      i ->
                          i
                              + " (\""
                              + mcqWithAnswer.getMultipleChoicesQuestion().getChoices().get(i)
                              + "\")")
                  .collect(Collectors.joining(", "));

      result.advice =
          "Unclear answer detected. The original question assume one correct choice index (0-based) of "
              + correctChoiceIndex
              + " (\""
              + mcqWithAnswer.getMultipleChoicesQuestion().getChoices().get(correctChoiceIndex)
              + "\"). however, the re-evaluation of the question shows that "
              + correctChoicesStr
              + " are correct to the question.\n"
              + "Please make sure the correct answer is correct and unique.\n\n";
    }
    result.advice += improvementAdvices == null ? "" : improvementAdvices;
    return result;
  }
}
