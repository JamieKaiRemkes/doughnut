package com.odde.doughnut.services;

import static org.junit.jupiter.api.Assertions.*;

import com.odde.doughnut.controllers.dto.Randomization;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.ApiException;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.TestabilitySettings;
import com.odde.doughnut.testability.builders.NoteBuilder;
import java.util.HashSet;
import java.util.Set;
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
public class AssessmentServiceTests {
  @Autowired MakeMe makeMe;
  private UserModel currentUser;
  private AssessmentService service;
  private final TestabilitySettings testabilitySettings = new TestabilitySettings();

  @BeforeEach
  void setup() {
    testabilitySettings.timeTravelTo(makeMe.aTimestamp().please());
    currentUser = makeMe.aUser().toModelPlease();
    service = new AssessmentService(makeMe.modelFactoryService, testabilitySettings);
  }

  @Nested
  class assessmentQuestionOrderTest {
    private Notebook notebook;
    private Note topNote;
    private int representativeNumberOfAttempts = 10;

    Set<Integer> performAssessments(int numberOfAttempts) {
      Set<Integer> questionIds = new HashSet<>();
      for (int i = 0; i < numberOfAttempts; i++) {
        AssessmentAttempt assessment = service.generateAssessment(notebook);
        Integer questionId =
            assessment.getAssessmentQuestionInstances().get(0).getReviewQuestionInstance().getId();
        questionIds.add(questionId);
      }
      return questionIds;
    }

    @BeforeEach
    void setup() {
      testabilitySettings.setRandomization(new Randomization(Randomization.RandomStrategy.seed, 1));
      topNote = makeMe.aHeadNote("OnlineAssessment").creatorAndOwner(currentUser).please();
      notebook = topNote.getNotebook();
      notebook.getNotebookSettings().setNumberOfQuestionsInAssessment(1);
    }

    @Test
    void shouldPickRandomNotesForAssessment() {
      makeMe.theNote(topNote).withNChildrenThat(10, NoteBuilder::hasAnApprovedQuestion).please();

      Set<Integer> questionIds = performAssessments(representativeNumberOfAttempts);

      assertTrue(questionIds.size() > 1, "Expected questions from different notes.");
    }

    @Test
    void shouldPickRandomQuestionsFromTheSameNote() {
      makeMe
          .theNote(topNote)
          .withNChildrenThat(1, noteBuilder -> noteBuilder.hasApprovedQuestions(10))
          .please();
      Set<Integer> questionIds = performAssessments(representativeNumberOfAttempts);
      assertTrue(questionIds.size() > 1, "Expected questions from the same note.");
    }
  }

  @Nested
  class generateOnlineAssessmentTest {
    private Notebook notebook;
    private Note topNote;

    @BeforeEach
    void setup() {
      topNote = makeMe.aHeadNote("OnlineAssessment").creatorAndOwner(currentUser).please();
      notebook = topNote.getNotebook();
      notebook.getNotebookSettings().setNumberOfQuestionsInAssessment(5);
    }

    @Test
    void shouldReturn5QuestionsWhenThereAreMoreThan5NotesWithQuestions() {
      makeMe.theNote(topNote).withNChildrenThat(5, NoteBuilder::hasAnApprovedQuestion).please();
      AssessmentAttempt assessment = service.generateAssessment(notebook);
      assertEquals(5, assessment.getAssessmentQuestionInstances().size());
    }

    @Test
    void shouldThrowExceptionWhenThereAreNotEnoughQuestions() {
      makeMe.theNote(topNote).withNChildrenThat(4, NoteBuilder::hasAnApprovedQuestion).please();
      assertThrows(ApiException.class, () -> service.generateAssessment(notebook));
    }

    @Test
    void shouldGetOneApprovedQuestionFromEachNoteOnly() {
      makeMe.theNote(topNote).withNChildrenThat(5, NoteBuilder::hasAnUnapprovedQuestion).please();
      assertThrows(ApiException.class, () -> service.generateAssessment(notebook));
    }
  }
}
