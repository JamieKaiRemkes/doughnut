package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.odde.doughnut.controllers.dto.AssimilationCountDTO;
import com.odde.doughnut.controllers.dto.InitialInfo;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.TestabilitySettings;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssimilationControllerTests {
  @Autowired private ModelFactoryService modelFactoryService;
  @Autowired private MakeMe makeMe;
  private UserModel currentUser;
  private final TestabilitySettings testabilitySettings = new TestabilitySettings();

  private AssimilationController controller;

  @BeforeEach
  void setup() {
    currentUser = makeMe.aUser().toModelPlease();
    controller = new AssimilationController(modelFactoryService, currentUser, testabilitySettings);
  }

  AssimilationController nullUserController() {
    return new AssimilationController(
        modelFactoryService, makeMe.aNullUserModelPlease(), testabilitySettings);
  }

  @Nested
  class Assimilating {
    @Test
    void assimilating() {
      Note n = makeMe.aNote().creatorAndOwner(currentUser).please();
      assertThat(n.getId(), notNullValue());
      List<Note> memoryTrackerWithRecallSettings = controller.assimilating("Asia/Shanghai");
      assertThat(memoryTrackerWithRecallSettings, hasSize(1));
    }

    @Test
    void notLoggedIn() {
      assertThrows(
          ResponseStatusException.class, () -> nullUserController().assimilating("Asia/Shanghai"));
    }
  }

  @Nested
  class CreateInitialReviewPoint {
    @Test
    void create() {
      InitialInfo info = new InitialInfo();
      assertThrows(ResponseStatusException.class, () -> nullUserController().assimilate(info));
    }

    @Test
    void shouldCreateTwoMemoryTrackersWhenRememberSpellingIsTrue() {
      Note note = makeMe.aNote().creatorAndOwner(currentUser).please();
      note.getRecallSetting().setRememberSpelling(true);
      modelFactoryService.noteRepository.save(note);

      InitialInfo initialInfo = new InitialInfo();
      initialInfo.noteId = note.getId();

      controller.assimilate(initialInfo);

      List<MemoryTracker> memoryTrackers =
          modelFactoryService.memoryTrackerRepository.findLast100ByUser(
              currentUser.getEntity().getId());
      assertThat(
          memoryTrackers.stream().filter(mt -> mt.getNote().getId().equals(note.getId())).count(),
          equalTo(2L));
      assertThat(memoryTrackers.stream().filter(mt -> mt.getSpelling()).count(), equalTo(1L));
      assertThat(memoryTrackers.stream().filter(mt -> !mt.getSpelling()).count(), equalTo(1L));
    }
  }

  @Nested
  class GetAssimilationCount {
    @Test
    void shouldReturnAssimilationCountsForLoggedInUser() {
      // Create a note that needs assimilation
      Note note = makeMe.aNote().creatorAndOwner(currentUser).please();
      assertThat(note.getId(), notNullValue());

      AssimilationCountDTO counts = controller.getAssimilationCount("Asia/Shanghai");

      assertThat(counts.getDueCount(), equalTo(1));
    }

    @Test
    void shouldThrowExceptionWhenUserNotLoggedIn() {
      assertThrows(
          ResponseStatusException.class,
          () -> nullUserController().getAssimilationCount("Asia/Shanghai"));
    }
  }
}
