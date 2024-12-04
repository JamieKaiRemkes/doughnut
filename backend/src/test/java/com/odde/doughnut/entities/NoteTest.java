package com.odde.doughnut.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.odde.doughnut.testability.MakeMe;
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
public class NoteTest {

  @Autowired MakeMe makeMe;

  @Test
  void timeOrder() {
    Note parent = makeMe.aNote().please();
    Note note1 = makeMe.aNote().under(parent).please();
    Note note2 = makeMe.aNote().under(parent).please();
    assertThat(parent.getChildren(), containsInRelativeOrder(note1, note2));
  }

  @Test
  void shortDetails() {
    Note note =
        makeMe
            .aNote()
            .details(
                "<strong>this is a very long sentence</strong> that contains very little meaning. The purpose is to test the truncate.")
            .please();
    assertThat(
        note.getNoteTopic().getShortDetails(),
        equalTo("this is a very long sentence that contains very li..."));
  }

  @Test
  void shortDetailsShouldBeNullIfEmpty() {
    Note note = makeMe.aNote().details("").please();
    assertThat(note.getNoteTopic().getShortDetails(), nullValue());
  }

  @Nested
  class TargetNote {
    Note parent;
    Note target;
    Note linkingNote;

    @BeforeEach
    void setup() {
      parent = makeMe.aNote().titleConstructor("parent").please();
      target = makeMe.aNote().please();
      linkingNote = makeMe.aLink().between(parent, target).please();
    }

    @Test
    void replaceParentPlaceholder() {
      assertThat(
          linkingNote.getNoteTopic().getTargetNoteTopic().getTopicConstructor(),
          equalTo(target.getTopicConstructor()));
    }

    @Test
    void linkOfLink() {
      Note linkOfLink = makeMe.aLink().between(parent, linkingNote).please();
      assertThat(
          linkOfLink.getNoteTopic().getTargetNoteTopic().getTargetNoteTopic().getId(),
          equalTo(target.getId()));
    }
  }

  @Nested
  class Image {
    @Test
    void useParentImage() {
      Note parent = makeMe.aNote().imageUrl("https://img.com/xxx.jpg").inMemoryPlease();
      Note child = makeMe.aNote().under(parent).useParentImage().inMemoryPlease();
      assertThat(
          child.getImageWithMask().noteImage, equalTo(parent.getNoteAccessory().getImageUrl()));
    }

    @Test
    void UseParentImageWhenTheUrlIsEmptyString() {
      Note parent = makeMe.aNote().imageUrl("").inMemoryPlease();
      Note child = makeMe.aNote().under(parent).useParentImage().inMemoryPlease();
      assertNull(child.getImageWithMask());
    }
  }

  @Nested
  class NoteBrief {
    @Test
    void shouldIncludeBasicNoteInformation() {
      Note note = makeMe.aNote().titleConstructor("Test Topic").details("Test Details").please();

      Note.NoteBrief brief = note.getNoteBrief();

      assertThat(brief.uri, equalTo("/n" + note.getId()));
      assertThat(brief.topic, equalTo("Test Topic"));
      assertThat(brief.details, equalTo("Test Details"));
      assertThat(brief.contextPath, equalTo(""));
      assertThat(brief.createdAt, notNullValue());
      assertThat(brief.target, nullValue());
    }

    @Test
    void shouldIncludeContextPathWithAncestors() {
      Note grandparent = makeMe.aNote().titleConstructor("Grandparent").please();
      Note parent = makeMe.aNote().titleConstructor("Parent").under(grandparent).please();
      Note note = makeMe.aNote().titleConstructor("Child").under(parent).please();

      Note.NoteBrief brief = note.getNoteBrief();

      assertThat(
          brief.contextPath,
          equalTo(
              "[Grandparent](/n" + grandparent.getId() + ") › [Parent](/n" + parent.getId() + ")"));
      assertThat(brief.topic, equalTo("Child"));
    }
  }
}
