package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.services.NoteConstructionService;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "ownership")
public class Ownership {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  @Setter
  private User user;

  @OneToOne
  @JoinColumn(name = "circle_id")
  @Setter
  private Circle circle;

  @OneToMany(mappedBy = "ownership")
  @SQLRestriction(value = "deleted_at is null")
  @JsonIgnore
  @Setter
  private List<Notebook> notebooks = new ArrayList<>();

  public String getOwnerName() {
    if (user != null) return user.getName();
    if (circle != null) return circle.getName();
    return null;
  }

  public boolean ownsBy(User user) {
    if (this.user != null) {
      return this.user.getId().equals(user.getId());
    }
    if (this.circle == null) return false;
    return this.circle.getMembers().contains(user);
  }

  public Note createAndPersistNotebook(
      User user,
      Timestamp currentUTCTimestamp,
      ModelFactoryService modelFactoryService,
      String topicConstructor) {
    final Note note =
        new NoteConstructionService(user, currentUTCTimestamp, modelFactoryService)
            .createNote(null, topicConstructor);
    note.buildNotebookForHeadNote(this, user);
    modelFactoryService.save(note.getNotebook());
    modelFactoryService.save(note);
    return note;
  }
}
