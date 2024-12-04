package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odde.doughnut.controllers.dto.CircleForUserView;
import com.odde.doughnut.controllers.dto.CircleJoiningByInvitation;
import com.odde.doughnut.controllers.dto.NoteCreationDTO;
import com.odde.doughnut.entities.Circle;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.CircleModel;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.TestabilitySettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestCircleControllerTest {
  @Autowired ModelFactoryService modelFactoryService;

  @Autowired MakeMe makeMe;
  private UserModel userModel;
  RestCircleController controller;
  private TestabilitySettings testabilitySettings = new TestabilitySettings();

  @BeforeEach
  void setup() {
    userModel = makeMe.aUser().toModelPlease();
    controller = new RestCircleController(modelFactoryService, testabilitySettings, userModel);
  }

  @Nested
  class circleIndex {
    @Test
    void itShouldNotAllowNonMemberToSeeACircle() {
      controller =
          new RestCircleController(
              modelFactoryService, testabilitySettings, makeMe.aNullUserModelPlease());
      assertThrows(
          ResponseStatusException.class,
          () -> {
            controller.index();
          });
    }
  }

  @Nested
  class showNoteTest {
    @Test
    void whenTheUserIsNotAMemberOfTheCircle() {
      Circle circle = makeMe.aCircle().please();
      NoteCreationDTO noteCreation = new NoteCreationDTO();
      noteCreation.setNewTitle("new title");
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.createNotebookInCircle(circle, noteCreation));
    }
  }

  @Nested
  class ShowCircle {
    @Test
    void itShouldCircleForUserViewIfAuthorized() throws UnexpectedNoAccessRightException {
      UserModel user = makeMe.aUser().toModelPlease();
      controller = new RestCircleController(modelFactoryService, testabilitySettings, user);

      Circle circle = makeMe.aCircle().please();
      circle.setName("Some circle");

      CircleModel circleModel = modelFactoryService.toCircleModel(circle);
      circleModel.joinAndSave(user.getEntity());

      CircleForUserView expected = new CircleForUserView();
      expected.setId(circle.getId());
      expected.setName(circle.getName());
      expected.setInvitationCode(circle.getInvitationCode());

      CircleForUserView actual = controller.showCircle(circle);

      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getInvitationCode(), actual.getInvitationCode());
    }

    @Test
    void itShouldAskToLoginOfVisitorIsNotLogin() {
      Circle circle = makeMe.aCircle().please();
      controller =
          new RestCircleController(
              modelFactoryService, testabilitySettings, makeMe.aNullUserModelPlease());
      assertThrows(
          ResponseStatusException.class,
          () -> {
            controller.showCircle(circle);
          });
    }

    @Test
    void itShouldNotAllowNonMemberToSeeACircle() {
      Circle circle = makeMe.aCircle().please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> {
            controller.showCircle(circle);
          });
    }
  }

  @Nested
  class JoinCircle {
    @Test
    void validationFailed() {
      CircleJoiningByInvitation entity = new CircleJoiningByInvitation();
      entity.setInvitationCode("short");
      BindException exception =
          assertThrows(BindException.class, () -> controller.joinCircle(entity));
      assertThat(exception.getErrorCount(), equalTo(1));
    }

    @Test
    void userAlreadyInCircle() {
      Circle circle = makeMe.aCircle().hasMember(userModel).please();
      CircleJoiningByInvitation entity = new CircleJoiningByInvitation();
      entity.setInvitationCode(circle.getInvitationCode());
      BindException exception =
          assertThrows(BindException.class, () -> controller.joinCircle(entity));
      assertThat(exception.getErrorCount(), equalTo(1));
    }
  }
}
