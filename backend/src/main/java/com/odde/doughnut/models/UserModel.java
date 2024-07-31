package com.odde.doughnut.models;

import com.odde.doughnut.entities.Note;
import com.odde.doughnut.entities.Notebook;
import com.odde.doughnut.entities.ReviewPoint;
import com.odde.doughnut.entities.User;
import com.odde.doughnut.exceptions.AssessmentAttemptLimitException;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public class UserModel implements ReviewScope {

  @Getter protected final User entity;
  protected final ModelFactoryService modelFactoryService;

  @Value("${constraints.assessment-attempts}")
  private Integer assessmentAttemptsLimit;

  public UserModel(User user, ModelFactoryService modelFactoryService) {
    this.entity = user;
    this.modelFactoryService = modelFactoryService;
  }

  private Authorization getAuthorization() {
    return modelFactoryService.toAuthorization(entity);
  }

  public String getName() {
    return entity.getName();
  }

  public void setAndSaveDailyNewNotesCount(Integer dailyNewNotesCount) {
    entity.setDailyNewNotesCount(dailyNewNotesCount);
    modelFactoryService.save(entity);
  }

  public void setAndSaveSpaceIntervals(String spaceIntervals) {
    entity.setSpaceIntervals(spaceIntervals);
    modelFactoryService.save(entity);
  }

  @Override
  public int getThingsHaveNotBeenReviewedAtAllCount() {
    return modelFactoryService.noteReviewRepository.countByOwnershipWhereThereIsNoReviewPoint(
        entity.getId(), entity.getOwnership().getId());
  }

  @Override
  public Stream<Note> getThingHaveNotBeenReviewedAtAll() {
    return modelFactoryService.noteReviewRepository.findByOwnershipWhereThereIsNoReviewPoint(
        entity.getId(), entity.getOwnership().getId());
  }

  public List<ReviewPoint> getRecentReviewPoints(Timestamp since) {
    return modelFactoryService.reviewPointRepository.findAllByUserAndInitialReviewedAtGreaterThan(
        entity, since);
  }

  public Stream<ReviewPoint> getReviewPointsNeedToRepeat(
      Timestamp currentUTCTimestamp, ZoneId timeZone) {
    final Timestamp timestamp = TimestampOperations.alignByHalfADay(currentUTCTimestamp, timeZone);
    return modelFactoryService.reviewPointRepository
        .findAllByUserAndNextReviewAtLessThanEqualOrderByNextReviewAt(entity.getId(), timestamp);
  }

  int learntCount() {
    return modelFactoryService.reviewPointRepository.countByUserNotRemoved(entity.getId());
  }

  public Reviewing createReviewing(Timestamp currentUTCTimestamp, ZoneId timeZone) {
    return new Reviewing(this, currentUTCTimestamp, timeZone, modelFactoryService);
  }

  boolean isInitialReviewOnSameDay(
      ReviewPoint reviewPoint, Timestamp currentUTCTimestamp, ZoneId timeZone) {
    return reviewPoint.isInitialReviewOnSameDay(currentUTCTimestamp, timeZone);
  }

  public ReviewPoint getReviewPointFor(Note note) {
    if (entity == null) return null;
    return modelFactoryService.reviewPointRepository.findByUserAndNote(
        entity.getId(), note.getId());
  }

  public <T> void assertAuthorization(T object) throws UnexpectedNoAccessRightException {
    getAuthorization().assertAuthorization(object);
  }

  public <T> void assertReadAuthorization(T object) throws UnexpectedNoAccessRightException {
    getAuthorization().assertReadAuthorization(object);
  }

  public void assertAdminAuthorization() throws UnexpectedNoAccessRightException {
    getAuthorization().assertAdminAuthorization();
  }

  public void assertLoggedIn() {
    getAuthorization().assertLoggedIn();
  }

  public void assertAssessmentAttempt(Notebook notebook, Timestamp currentUTCTimestamp)
      throws AssessmentAttemptLimitException {

    int count =
        modelFactoryService.assessmentAttemptRepository
            .countAssessmentAttemptHistoriesByNotebookAndUserAndSubmittedAtBetween(
                notebook,
                entity,
                TimestampOperations.addHoursToTimestamp(currentUTCTimestamp, -24),
                currentUTCTimestamp);
    int limit = assessmentAttemptsLimit == null ? 3 : assessmentAttemptsLimit;
    if (count >= limit) {
      throw new AssessmentAttemptLimitException("");
    }
  }
}
