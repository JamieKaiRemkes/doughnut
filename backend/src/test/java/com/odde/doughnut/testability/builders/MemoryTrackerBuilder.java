package com.odde.doughnut.testability.builders;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.models.MemoryTrackerModel;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.EntityBuilder;
import com.odde.doughnut.testability.MakeMe;
import java.sql.Timestamp;

public class MemoryTrackerBuilder extends EntityBuilder<MemoryTracker> {

  public MemoryTrackerBuilder(MemoryTracker memoryTracker, MakeMe makeMe) {
    super(makeMe, memoryTracker);
    initiallyReviewedOn(makeMe.aTimestamp().of(0, 0).please());
  }

  public MemoryTrackerBuilder by(UserModel userModel) {
    return by(userModel.getEntity());
  }

  public MemoryTrackerBuilder by(User user) {
    entity.setUser(user);
    return this;
  }

  public MemoryTrackerBuilder initiallyReviewedOn(Timestamp reviewTimestamp) {
    entity.setInitialReviewedAt(reviewTimestamp);
    entity.setLastRecalledAt(reviewTimestamp);
    entity.setNextRecallAt(reviewTimestamp);
    return this;
  }

  public MemoryTrackerBuilder afterNthStrictRepetition(Integer repetitionDone) {
    for (int i = 0; i < repetitionDone; i++) {
      entity.reviewedSuccessfully(entity.getNextRecallAt());
    }
    return this;
  }

  public MemoryTrackerModel toModelPlease() {
    return makeMe.modelFactoryService.toMemoryTrackerModel(please());
  }

  @Override
  protected void beforeCreate(boolean needPersist) {}

  public MemoryTrackerBuilder forgettingCurveAndNextRecallAt(int value) {
    entity.setForgettingCurveIndex(value);
    entity.setNextRecallAt(entity.calculateNextRecallAt());
    return this;
  }

  public MemoryTrackerBuilder removedFromTracking() {
    entity.setRemovedFromTracking(true);
    return this;
  }

  public MemoryTrackerBuilder nextRecallAt(Timestamp timestamp) {
    entity.setNextRecallAt(timestamp);
    return this;
  }
}
