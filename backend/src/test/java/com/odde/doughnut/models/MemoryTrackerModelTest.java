package com.odde.doughnut.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.odde.doughnut.entities.Note;
import com.odde.doughnut.testability.MakeMe;
import java.sql.Timestamp;
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
public class MemoryTrackerModelTest {
  @Autowired MakeMe makeMe;
  UserModel userModel;
  Timestamp day1;

  @BeforeEach
  void setup() {
    userModel = makeMe.aUser().toModelPlease();
    day1 = makeMe.aTimestamp().of(1, 8).fromShanghai().please();
  }

  @Nested
  class InitialReview {

    @Test
    void initialReviewShouldSetBothInitialAndLastReviewAt() {
      Note note = makeMe.aNote().creatorAndOwner(userModel).please();
      ReviewPointModel reviewPoint = makeMe.aReviewPointFor(note).by(userModel).toModelPlease();
      reviewPoint.initialReview(day1, userModel.getEntity());
      assertThat(reviewPoint.getEntity().getInitialReviewedAt(), equalTo(day1));
      assertThat(reviewPoint.getEntity().getLastReviewedAt(), equalTo(day1));
    }
  }
}
