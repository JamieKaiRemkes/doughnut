package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odde.doughnut.entities.FailureReport;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.GithubService;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.NullGithubService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
class RestFailureReportControllerTest {
  @Autowired MakeMe makeMe;
  private GithubService githubService = new NullGithubService();

  RestFailureReportController controller(UserModel userModel) {
    return new RestFailureReportController(makeMe.modelFactoryService, githubService, userModel);
  }

  @Test
  void whenNonAdminAccessTheFailureReport() {
    UserModel nonAdmin = makeMe.aUser().toModelPlease();
    FailureReport failureReport = makeMe.aFailureReport().please();
    assertThrows(
        UnexpectedNoAccessRightException.class, () -> controller(nonAdmin).show(failureReport));
  }

  @Nested
  class DeleteFailureReportsTest {
    UserModel admin;
    List<FailureReport> failureReports;

    @BeforeEach
    void setup() {
      admin = makeMe.anAdmin().toModelPlease();

      // Clear all existing failure reports first to ensure test independence
      makeMe.modelFactoryService.failureReportRepository.deleteAll();

      failureReports = new ArrayList<>();
      failureReports.add(makeMe.aFailureReport().please());
      failureReports.add(makeMe.aFailureReport().please());
    }

    @Test
    void adminCanDeleteFailureReports() throws UnexpectedNoAccessRightException {
      List<Integer> idsToDelete =
          failureReports.stream().map(FailureReport::getId).collect(Collectors.toList());

      controller(admin).deleteFailureReports(idsToDelete);

      Iterable<FailureReport> remainingReports = controller(admin).failureReports();
      List<FailureReport> reportList =
          StreamSupport.stream(remainingReports.spliterator(), false).collect(Collectors.toList());
      assertThat(reportList, is(empty()));
    }

    @Test
    void adminCanDeleteOneFailureReport() throws UnexpectedNoAccessRightException {
      List<Integer> idsToDelete = List.of(failureReports.get(0).getId());

      controller(admin).deleteFailureReports(idsToDelete);

      Iterable<FailureReport> remainingReports = controller(admin).failureReports();
      List<FailureReport> reportList =
          StreamSupport.stream(remainingReports.spliterator(), false).collect(Collectors.toList());
      assertThat(reportList, hasSize(1));
      assertThat(reportList.get(0).getId(), equalTo(failureReports.get(1).getId()));
    }

    @Test
    void nonAdminCannotDeleteFailureReports() {
      UserModel nonAdmin = makeMe.aUser().toModelPlease();
      List<Integer> idsToDelete =
          failureReports.stream().map(FailureReport::getId).collect(Collectors.toList());

      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller(nonAdmin).deleteFailureReports(idsToDelete));
    }
  }
}
