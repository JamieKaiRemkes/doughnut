package com.odde.doughnut.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;

@MappedSuperclass
public abstract class EntityIdentifiedByIdOnly {
  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  protected Integer id;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityIdentifiedByIdOnly entity = (EntityIdentifiedByIdOnly) o;
    return Objects.equals(id, entity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
