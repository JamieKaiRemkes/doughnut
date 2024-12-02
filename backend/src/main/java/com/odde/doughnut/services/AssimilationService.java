package com.odde.doughnut.services;

import com.odde.doughnut.controllers.dto.AssimilationCountDTO;
import com.odde.doughnut.entities.MemoryTracker;
import com.odde.doughnut.entities.Note;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.ReviewScope;
import com.odde.doughnut.models.SubscriptionModel;
import com.odde.doughnut.models.TimestampOperations;
import com.odde.doughnut.models.UserModel;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

public class AssimilationService {
  private final UserModel userModel;
  private final ModelFactoryService modelFactoryService;
  private final Timestamp currentUTCTimestamp;
  private final ZoneId timeZone;

  public AssimilationService(
      UserModel user,
      ModelFactoryService modelFactoryService,
      Timestamp currentUTCTimestamp,
      ZoneId timeZone) {
    userModel = user;
    this.modelFactoryService = modelFactoryService;
    this.currentUTCTimestamp = currentUTCTimestamp;
    this.timeZone = timeZone;
  }

  private Stream<Note> getDueNoteToAssimilate(ReviewScope reviewScope, int count) {
    return reviewScope.getUnassimilatedNotes().limit(count);
  }

  private Stream<SubscriptionModel> getSubscriptionModelStream() {
    return userModel.getEntity().getSubscriptions().stream()
        .map(modelFactoryService::toSubscriptionModel);
  }

  public Stream<Note> getNotesToAssimilate() {
    int remainingDailyCount = getRemainingDailyAssimilationCount();
    if (remainingDailyCount <= 0) {
      return Stream.empty();
    }

    List<Integer> assimilatedNoteIdsForToday =
        getNotesAssimilatedToday().stream().map(MemoryTracker::getNote).map(Note::getId).toList();

    return Stream.concat(
            getDueNoteFromSubscription(assimilatedNoteIdsForToday),
            getDueNoteToAssimilate(userModel, remainingDailyCount))
        .limit(remainingDailyCount);
  }

  private Stream<Note> getDueNoteFromSubscription(List<Integer> todaysReviewedNoteIds) {
    return getSubscriptionModelStream()
        .flatMap(
            sub -> getDueNoteToAssimilate(sub, sub.needToLearnCountToday(todaysReviewedNoteIds)));
  }

  private int getRemainingDailyAssimilationCount() {
    return userModel.getEntity().getDailyNewNotesCount() - getAssimilatedCountOfTheDay();
  }

  public AssimilationCountDTO getCounts() {
    AssimilationCounter counter =
        new AssimilationCounter(
            calculateSubscribedCount(),
            userModel.getUnassimilatedNoteCount(),
            getAssimilatedCountOfTheDay(),
            userModel.getEntity().getDailyNewNotesCount());
    return counter.toDTO();
  }

  private int calculateSubscribedCount() {
    return getSubscriptionModelStream()
        .mapToInt(SubscriptionModel::getUnassimilatedNoteCount)
        .sum();
  }

  private int getAssimilatedCountOfTheDay() {
    return getNotesAssimilatedToday().size();
  }

  private List<MemoryTracker> getNotesAssimilatedToday() {
    Timestamp oneDayAgo = TimestampOperations.addHoursToTimestamp(currentUTCTimestamp, -24);
    return userModel.getRecentMemoryTrackers(oneDayAgo).stream()
        .filter(
            p ->
                TimestampOperations.getDayId(p.getAssimilatedAt(), timeZone)
                    == TimestampOperations.getDayId(currentUTCTimestamp, timeZone))
        .filter(p -> !p.getRemovedFromTracking())
        .toList();
  }
}
