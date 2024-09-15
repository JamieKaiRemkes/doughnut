package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.NotebookCertificateApprovalService;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.TestabilitySettings;
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
class RestNotebookCertificateApprovalControllerTest {
  @Autowired ModelFactoryService modelFactoryService;

  @Autowired MakeMe makeMe;
  private UserModel userModel;
  RestNotebookCertificateApprovalController controller;
  private TestabilitySettings testabilitySettings = new TestabilitySettings();

  @BeforeEach
  void setup() {
    userModel = makeMe.aUser().toModelPlease();
    controller =
        new RestNotebookCertificateApprovalController(
            modelFactoryService, userModel, testabilitySettings);
  }

  @Nested
  class getNotebookApproval {
    @Test
    void shouldNotBeAbleToGetApprovalForNotebookThatBelongsToOtherUser() {
      User anotherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(anotherUser).please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.getApprovalForNotebook(note.getNotebook()));
    }

    @Test
    void approvalStatusShouldBeNullIfNotExist() throws UnexpectedNoAccessRightException {
      Note note = makeMe.aNote().creatorAndOwner(userModel).please();
      NotebookCertificateApproval approvalForNotebook =
          controller.getApprovalForNotebook(note.getNotebook());
      assertThat(approvalForNotebook, nullValue());
    }

    @Test
    void success() throws UnexpectedNoAccessRightException {
      Note note = makeMe.aNote().creatorAndOwner(userModel).please();
      makeMe.modelFactoryService.notebookService(note.getNotebook()).requestNotebookApproval();
      makeMe.refresh(note.getNotebook());
      NotebookCertificateApproval approvalForNotebook =
          controller.getApprovalForNotebook(note.getNotebook());
      assertThat(approvalForNotebook.getNotebook(), equalTo(note.getNotebook()));
    }
  }

  @Nested
  class requestNotebookApproval {
    @Test
    void shouldNotBeAbleToRequestApprovalForNotebookThatBelongsToOtherUser() {
      User anotherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(anotherUser).please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.requestApprovalForNotebook(note.getNotebook()));
    }

    @Test
    void approvalStatusShouldBePendingAfterRequestingApproval()
        throws UnexpectedNoAccessRightException {
      Note note = makeMe.aNote().creatorAndOwner(userModel).please();
      controller.requestApprovalForNotebook(note.getNotebook());
      makeMe.refresh(note.getNotebook());
      assertFalse(note.getNotebook().isCertifiable());
    }
  }

  @Nested
  class getAllPendingRequestNotebooks {
    private Notebook notebook;
    private NotebookCertificateApprovalService approval;

    @BeforeEach
    void setup() {
      UserModel userModel = makeMe.anAdmin().toModelPlease();
      notebook = makeMe.aNotebook().creatorAndOwner(userModel).please();
      controller =
          new RestNotebookCertificateApprovalController(
              modelFactoryService, userModel, testabilitySettings);
      approval = makeMe.modelFactoryService.notebookService(notebook).requestNotebookApproval();
      makeMe.refresh(notebook);
    }

    @Test
    void shouldReturnPendingRequestNotebooks() throws UnexpectedNoAccessRightException {
      var result = controller.getAllPendingRequest();
      assertThat(result, hasSize(1));
    }

    @Test
    void shouldNotReturnApprovedNotebooks() throws UnexpectedNoAccessRightException {
      approval.approve(makeMe.aTimestamp().please());
      makeMe.refresh(notebook);
      var result = controller.getAllPendingRequest();
      assertThat(result, hasSize(0));
    }

    @Test
    void shouldApproveNoteBook() throws UnexpectedNoAccessRightException {
      Notebook result = controller.approve(approval.getApproval()).getNotebook();
      assertTrue(result.isCertifiable());
    }
  }
}
