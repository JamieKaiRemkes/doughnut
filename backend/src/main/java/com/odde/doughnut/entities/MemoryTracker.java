package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odde.doughnut.models.TimestampOperations;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "memory_tracker")
public class MemoryTracker extends EntityIdentifiedByIdOnly {
  public static MemoryTracker buildMemoryTrackerForNote(Note note) {
    MemoryTracker entity = new MemoryTracker();
    entity.setNote(note);
    return entity;
  }

  @Override
  public String toString() {
    return "MemoryTracker{" + "id=" + id + '}';
  }

  @ManyToOne
  @JoinColumn(name = "note_id")
  @Getter
  @Setter
  @NotNull
  private Note note;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  @JsonIgnore
  @Getter
  @Setter
  private User user;

  @Column(name = "last_recalled_at")
  @Getter
  @Setter
  private Timestamp lastReviewedAt;

  @Column(name = "next_recall_at")
  @Getter
  @Setter
  @NotNull
  private Timestamp nextReviewAt;

  @Column(name = "onboarded_at")
  @Getter
  @Setter
  private Timestamp initialReviewedAt;

  @Column(name = "repetition_count")
  @Getter
  @Setter
  private Integer repetitionCount = 0;

  @Column(name = "forgetting_curve_index")
  @Getter
  @Setter
  private Integer forgettingCurveIndex = ForgettingCurve.DEFAULT_FORGETTING_CURVE_INDEX;

  @Column(name = "removed_from_tracking")
  @Getter
  @Setter
  private Boolean removedFromReview = false;

  private MemoryTracker() {}

  public boolean isInitialReviewOnSameDay(Timestamp currentTime, ZoneId timeZone) {
    return TimestampOperations.getDayId(getInitialReviewedAt(), timeZone)
        == TimestampOperations.getDayId(currentTime, timeZone);
  }

  public Timestamp calculateNextReviewAt() {
    return TimestampOperations.addHoursToTimestamp(
        getLastReviewedAt(), forgettingCurve().getRepeatInHours());
  }

  private ForgettingCurve forgettingCurve() {
    return new ForgettingCurve(getUser().getSpacedRepetitionAlgorithm(), getForgettingCurveIndex());
  }

  public void reviewFailed(Timestamp currentUTCTimestamp) {
    setForgettingCurveIndex(forgettingCurve().failed());
    setNextReviewAt(TimestampOperations.addHoursToTimestamp(currentUTCTimestamp, 12));
  }

  public void reviewedSuccessfully(Timestamp currentUTCTimestamp) {
    long delayInHours =
        TimestampOperations.getDiffInHours(currentUTCTimestamp, calculateNextReviewAt());

    setForgettingCurveIndex(forgettingCurve().succeeded(delayInHours));

    setLastReviewedAt(currentUTCTimestamp);
    setNextReviewAt(calculateNextReviewAt());
  }
}
