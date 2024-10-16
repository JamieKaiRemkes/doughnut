package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.odde.doughnut.controllers.dto.*;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.TimestampOperations;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.ConversationService;
import com.odde.doughnut.services.httpQuery.HttpClientAdapter;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.MakeMeWithoutDB;
import com.odde.doughnut.testability.TestabilitySettings;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestNoteControllerTests {
  @Autowired ModelFactoryService modelFactoryService;
  @Autowired ConversationService conversationService;

  @Autowired MakeMe makeMe;
  @Mock HttpClientAdapter httpClientAdapter;
  private UserModel userModel;
  RestNoteController controller;
  private final TestabilitySettings testabilitySettings = new TestabilitySettings();

  @BeforeEach
  void setup() {
    userModel = makeMe.aUser().toModelPlease();

    controller =
        new RestNoteController(
            modelFactoryService,
            userModel,
            httpClientAdapter,
            testabilitySettings,
            conversationService);
  }

  private void mockWikidataEntity(String wikidataId, String label)
      throws IOException, InterruptedException {
    if (Strings.isEmpty(wikidataId) || Strings.isEmpty(label)) {
      return;
    }
    Mockito.when(
            httpClientAdapter.getResponseString(
                URI.create(
                    "https://www.wikidata.org/wiki/Special:EntityData/" + wikidataId + ".json")))
        .thenReturn(makeMe.wikidataClaimsJson(wikidataId).labelIf(label).please());
  }

  private void mockWikidataWBGetEntity(String personWikidataId, String value)
      throws IOException, InterruptedException {
    Mockito.when(
            httpClientAdapter.getResponseString(
                URI.create(
                    "https://www.wikidata.org/w/api.php?action=wbgetentities&ids="
                        + personWikidataId
                        + "&format=json&props=claims")))
        .thenReturn(value);
  }

  @Nested
  class showNoteTest {
    @Test
    void shouldNotBeAbleToSeeNoteIDontHaveAccessTo() {
      User otherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(otherUser).please();
      assertThrows(UnexpectedNoAccessRightException.class, () -> controller.show(note));
    }

    @Test
    void shouldReturnTheNoteInfoIfHavingReadingAuth() throws UnexpectedNoAccessRightException {
      User otherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(otherUser).please();
      makeMe.aBazaarNotebook(note.getNotebook()).please();
      final NoteRealm noteRealm = controller.show(note);
      assertThat(noteRealm.getNote().getTopicConstructor(), equalTo(note.getTopicConstructor()));
      assertThat(noteRealm.getFromBazaar(), is(true));
    }

    @Test
    void shouldBeAbleToSeeOwnNote() throws UnexpectedNoAccessRightException {
      Note note = makeMe.aNote().creatorAndOwner(userModel).please();
      final NoteRealm noteRealm = controller.show(note);
      assertThat(noteRealm.getId(), equalTo(note.getId()));
      assertThat(noteRealm.getFromBazaar(), is(false));
    }
  }

  @Nested
  class showStatistics {
    @Test
    void shouldNotBeAbleToSeeNoteIDontHaveAccessTo() {
      User otherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(otherUser).please();
      assertThrows(UnexpectedNoAccessRightException.class, () -> controller.getNoteInfo(note));
    }

    @Test
    void shouldReturnTheNoteInfoIfHavingReadingAuth() throws UnexpectedNoAccessRightException {
      User otherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(otherUser).please();
      makeMe
          .aSubscription()
          .forUser(userModel.getEntity())
          .forNotebook(note.getNotebook())
          .please();
      makeMe.refresh(userModel.getEntity());
      assertThat(controller.getNoteInfo(note).getNote().getId(), equalTo(note.getId()));
    }
  }

  @Nested
  class createNoteTest {
    Note parent;
    NoteCreationDTO noteCreation = new NoteCreationDTO();

    @BeforeEach
    void setup() {
      parent = makeMe.aNote().creatorAndOwner(userModel).please();
      noteCreation.setTopicConstructor("new title");
      noteCreation.setLinkTypeToParent(LinkType.NO_LINK);
    }

    @Test
    void shouldBeAbleToSaveNoteWhenValid()
        throws UnexpectedNoAccessRightException, BindException, InterruptedException, IOException {
      NoteRealm response = controller.createNote(parent, noteCreation).getCreated();
      assertThat(response.getId(), not(nullValue()));
    }

    @Test
    void shouldBeAbleToCreateAThing()
        throws UnexpectedNoAccessRightException, BindException, InterruptedException, IOException {
      long beforeThingCount = makeMe.modelFactoryService.noteRepository.count();
      controller.createNote(parent, noteCreation);
      long afterThingCount = makeMe.modelFactoryService.noteRepository.count();
      assertThat(afterThingCount, equalTo(beforeThingCount + 1));
    }

    @Test
    void shouldBeAbleToSaveNoteWithWikidataIdWhenValid()
        throws UnexpectedNoAccessRightException, BindException, InterruptedException, IOException {
      Mockito.when(httpClientAdapter.getResponseString(any()))
          .thenReturn(new MakeMeWithoutDB().wikidataEntityJson().entityId("Q12345").please());
      noteCreation.setWikidataId("Q12345");
      NoteRealm response = controller.createNote(parent, noteCreation).getCreated();
      assertThat(response.getNote().getWikidataId(), equalTo("Q12345"));
    }

    @Test
    void shouldBeAbleToSaveNoteWithoutWikidataIdWhenValid()
        throws UnexpectedNoAccessRightException, BindException, InterruptedException, IOException {
      NoteRealm response = controller.createNote(parent, noteCreation).getCreated();

      assertThat(response.getNote().getWikidataId(), equalTo(null));
    }

    @Test
    void shouldThrowWhenCreatingNoteWithWikidataIdExistsInAnotherNote() {
      String conflictingWikidataId = "Q123";
      makeMe.aNote().under(parent).wikidataId(conflictingWikidataId).please();
      noteCreation.setWikidataId(conflictingWikidataId);
      BindException bindException =
          assertThrows(BindException.class, () -> controller.createNote(parent, noteCreation));
      assertThat(
          bindException.getMessage(), stringContainsInOrder("Duplicate Wikidata ID Detected."));
    }

    @Nested
    class AddingNoteWithLocationWikidataId {
      String wikidataIdOfALocation = "Q334";
      String lnglat = "1.3'N, 103.8'E";
      String singapore = "Singapore";

      @BeforeEach
      void thereIsAWikidataEntryOfALocation() {
        noteCreation.setWikidataId(wikidataIdOfALocation);
      }

      private void mockApiResponseWithLocationInfo(String locationInfo, String type)
          throws IOException, InterruptedException {
        mockWikidataWBGetEntity(
            wikidataIdOfALocation,
            makeMe.wikidataClaimsJson("Q334").globeCoordinate(locationInfo, type).please());
      }

      @Test
      void shouldPrependLocationInfoWhenAddingNoteWithWikidataId()
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {
        mockApiResponseWithLocationInfo(
            "{\"latitude\":1.3,\"longitude\":103.8}", "globecoordinate");
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();
        assertThat(note.getNote().getDetails(), containsString("Location: " + lnglat));
      }

      @Test
      void shouldPrependLocationInfoWhenAddingNoteWithWikidataIdWithStringValue()
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {
        mockApiResponseWithLocationInfo("\"center of the earth\"", "string");
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();
        assertThat(
            note.getNote().getDetails(), stringContainsInOrder("Location: center of the earth"));
      }
    }

    @Nested
    class AddingNoteWithHumanWikidataId {
      @BeforeEach
      void thereIsAWikidataEntryOfAHuman() {
        noteCreation.setWikidataId("");
      }

      private void mockWikidataHumanEntity(
          String personWikidataId, String birthdayByISO, String countryQId)
          throws IOException, InterruptedException {
        mockWikidataWBGetEntity(
            personWikidataId,
            makeMe
                .wikidataClaimsJson(personWikidataId)
                .asAHuman()
                .countryOfOrigin(countryQId)
                .birthdayIf(birthdayByISO)
                .please());
      }

      @ParameterizedTest
      @CsvSource(
          useHeadersInDisplayName = true,
          delimiter = '|',
          textBlock =
              """
             WikidataId | Birthday from Wikidata | CountryQID | Country Name | Expected Birthday    | Name
            #---------------------------------------------------------------------------------------------
             Q706446    | +1980-03-31T00:00:00Z  |            |              | 31 March 1980        |
             Q4604      | -0552-10-09T00:00:00Z  | Q736936    |              | 09 October 0552 B.C. | Confucius
             Q706446    | +1980-03-31T00:00:00Z  | Q865       | Taiwan       | 31 March 1980        | Wang Chen-ming
             Q706446    |                        | Q865       | Taiwan       |                      |
             Q706446    | +1980-03-31T00:00:00Z  | Q30        | The US of A  |  31 March 1980       |
            """)
      void shouldAddHumanBirthdayAndCountryOfOriginWhenAddingNoteWithWikidataId(
          String wikidataIdOfHuman,
          String birthdayByISO,
          String countryQid,
          String countryName,
          String expectedBirthday)
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {
        mockWikidataHumanEntity(wikidataIdOfHuman, birthdayByISO, countryQid);
        mockWikidataEntity(countryQid, countryName);
        noteCreation.setWikidataId(wikidataIdOfHuman);
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();
        String description = note.getNote().getDetails();
        if (expectedBirthday != null) {
          assertThat(description, containsString(expectedBirthday));
        }
        if (countryName != null) {
          assertThat(description, containsString(countryName));
        }
      }

      @Test
      void shouldAddPersonNoteWithCountryNoteWithWikidataId()
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {

        mockWikidataHumanEntity("Q8337", null, "Q34660");
        mockWikidataEntity("Q34660", "Canada");
        noteCreation.setWikidataId("Q8337");
        noteCreation.setTopicConstructor("Johnny boy");
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();

        assertEquals("Johnny boy", note.getNote().getTopicConstructor());
        assertEquals("Q8337", note.getNote().getWikidataId());
        assertEquals("Canada", note.getNote().getChildren().get(0).getTopicConstructor());
      }
    }

    @Nested
    class AddingBookNoteWithAuthorInformation {
      @BeforeEach
      void setup() throws IOException, InterruptedException {
        mockWikidataEntity("Q34660", "J. K. Rowling");
        mockWikidataEntity("Q12345", "The girl sat next to the window");
        noteCreation.setWikidataId("Q8337");
        noteCreation.setTopicConstructor("Harry Potter");
      }

      @Test
      void shouldAddBookNoteWithAuthorNoteWithWikidataId()
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {
        mockWikidataWBGetEntity(
            "Q8337", makeMe.wikidataClaimsJson("Q8337").asABookWithSingleAuthor("Q34660").please());
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();

        assertEquals("Harry Potter", note.getNote().getTopicConstructor());
        assertEquals("Q8337", note.getNote().getWikidataId());
        assertEquals("J. K. Rowling", note.getNote().getChildren().get(0).getTopicConstructor());
      }

      @Test
      void shouldAddBookNoteWithMultipleAuthorsNoteWithWikidataId()
          throws BindException,
              InterruptedException,
              UnexpectedNoAccessRightException,
              IOException {
        mockWikidataWBGetEntity(
            "Q8337",
            makeMe
                .wikidataClaimsJson("Q8337")
                .asABookWithMultipleAuthors(List.of("Q34660", "Q12345"))
                .please());
        NoteRealm note = controller.createNote(parent, noteCreation).getCreated();

        assertEquals(
            "The girl sat next to the window",
            note.getNote().getChildren().get(1).getTopicConstructor());
      }
    }
  }

  @Nested
  class updateNoteTest {
    Note note;
    NoteAccessoriesDTO noteAccessoriesDTO = new NoteAccessoriesDTO();

    @BeforeEach
    void setup() {
      note = makeMe.aNote("new").creatorAndOwner(userModel).please();
    }

    @Test
    void shouldBeAbleToSaveNoteWhenValid() throws UnexpectedNoAccessRightException, IOException {
      NoteAccessory response = controller.updateNoteAccessories(note, noteAccessoriesDTO);
      assertThat(response.getNote().getId(), equalTo(note.getId()));
    }

    @Test
    void shouldAddUploadedImage() throws UnexpectedNoAccessRightException, IOException {
      noteAccessoriesDTO.setUploadImage(makeMe.anUploadedImage().toMultiplePartFilePlease());
      controller.updateNoteAccessories(note, noteAccessoriesDTO);
      assertThat(note.getNoteAccessory().getImageAttachment(), is(not(nullValue())));
      note.getNoteAccessory().getImageAttachment().getBlob().getData();
    }

    @Test
    void shouldSaveTheBlogData() throws UnexpectedNoAccessRightException, IOException {
      noteAccessoriesDTO.setUploadImage(makeMe.anUploadedImage().toMultiplePartFilePlease());
      controller.updateNoteAccessories(note, noteAccessoriesDTO);
      byte[] data = note.getNoteAccessory().getImageAttachment().getBlob().getData();
      assertThat(data.length, is(68));
    }

    @Test
    void shouldNotRemoveTheImageIfNoNewImageInTheUpdate()
        throws UnexpectedNoAccessRightException, IOException {
      makeMe.theNote(note).withUploadedImage();
      controller.updateNoteAccessories(note, noteAccessoriesDTO);
      assertThat(note.getNoteAccessory().getImageAttachment(), is(not(nullValue())));
    }
  }

  @Nested
  class uploadAudioTest {
    Note note;
    AudioUploadDTO audioUploadDTO = new AudioUploadDTO();

    @BeforeEach
    void setup() {
      note = makeMe.aNote("new").creatorAndOwner(userModel).please();
    }

    @Test
    void shouldPersistAudioToNoteAfterUpload() throws Exception {
      String filename = "podcast.wav";
      audioUploadDTO.setUploadAudioFile(
          new MockMultipartFile(filename, filename, "audio/wav", new byte[] {}));
      controller.uploadAudio(note, audioUploadDTO);
      Note newNote = makeMe.modelFactoryService.noteRepository.findById(note.getId()).get();
      assertEquals(filename, newNote.getNoteAccessory().getAudioAttachment().getName());
    }
  }

  @Nested
  class DeleteNoteTest {
    Note subject;
    Note parent;
    Note child;

    @BeforeEach
    void setup() {
      parent = makeMe.aNote().creatorAndOwner(userModel).please();
      subject = makeMe.aNote().under(parent).please();
      child = makeMe.aNote("child").under(subject).please();
    }

    @Test
    void shouldNotBeAbleToDeleteNoteThatBelongsToOtherUser() {
      User anotherUser = makeMe.aUser().please();
      Note note = makeMe.aNote().creatorAndOwner(anotherUser).please();
      assertThrows(UnexpectedNoAccessRightException.class, () -> controller.deleteNote(note));
    }

    @Test
    void shouldDeleteTheNoteButNotTheUser() throws UnexpectedNoAccessRightException {
      controller.deleteNote(subject);
      assertThat(parent.getChildren(), hasSize(0));
      assertTrue(modelFactoryService.findUserById(userModel.getEntity().getId()).isPresent());
    }

    @Test
    void shouldDeleteTheChildNoteButNotSibling() throws UnexpectedNoAccessRightException {
      makeMe.aNote("silbling").under(parent).please();
      controller.deleteNote(subject);
      assertThat(parent.getChildren(), hasSize(1));
      assertThat(parent.getAllNoneLinkDescendants().toList(), hasSize(1));
    }

    @Nested
    class UndoDeleteNoteTest {
      @Test
      void shouldUndoDeleteTheNote() throws UnexpectedNoAccessRightException {
        controller.deleteNote(subject);
        controller.undoDeleteNote(subject);
        assertThat(parent.getChildren(), hasSize(1));
        assertThat(parent.getAllNoneLinkDescendants().toList(), hasSize(2));
      }

      @Test
      void shouldUndoOnlyLastChange() throws UnexpectedNoAccessRightException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        testabilitySettings.timeTravelTo(timestamp);
        controller.deleteNote(child);

        timestamp = TimestampOperations.addHoursToTimestamp(timestamp, 1);
        testabilitySettings.timeTravelTo(timestamp);
        controller.deleteNote(subject);

        controller.undoDeleteNote(subject);
        assertThat(parent.getAllNoneLinkDescendants().toList(), hasSize(1));
      }
    }
  }

  @Nested
  class UpdateWikidataId {
    Note note;
    Note parent;
    String noteWikidataId = "Q1234";

    @BeforeEach
    void setup() {
      parent = makeMe.aNote().creatorAndOwner(userModel).please();
      note = makeMe.aNote().under(parent).please();
    }

    @Test
    void shouldUpdateNoteWithUniqueWikidataId()
        throws BindException, UnexpectedNoAccessRightException, IOException, InterruptedException {
      WikidataAssociationCreation wikidataAssociationCreation = new WikidataAssociationCreation();
      wikidataAssociationCreation.wikidataId = "Q123";
      controller.updateWikidataId(note, wikidataAssociationCreation);
      Note sameNote = makeMe.modelFactoryService.noteRepository.findById(note.getId()).get();
      assertThat(sameNote.getWikidataId(), equalTo("Q123"));
    }

    @Test
    void shouldNotUpdateWikidataIdIfParentNoteSameWikidataId() {
      makeMe.aNote().under(parent).wikidataId(noteWikidataId).please();

      WikidataAssociationCreation wikidataAssociationCreation = new WikidataAssociationCreation();
      wikidataAssociationCreation.wikidataId = noteWikidataId;
      BindException bindException =
          assertThrows(
              BindException.class,
              () -> controller.updateWikidataId(note, wikidataAssociationCreation));
      assertThat(
          bindException.getMessage(), stringContainsInOrder("Duplicate Wikidata ID Detected."));
    }
  }

  @Nested
  class SendNoteFeedbackTests {
    Note note;
    String feedback = "This is a feedback sent from note";

    @BeforeEach
    void setup() {
      UserModel noteOwner = makeMe.aUser().toModelPlease();
      note = makeMe.aNote().creatorAndOwner(noteOwner).please();
    }

    @Test
    void shouldStartConversation() {
      controller.sendNoteFeedback(note, feedback);
      List<Conversation> conversations =
          (List<Conversation>) modelFactoryService.conversationRepository.findAll();
      assertEquals(1, conversations.size());

      Conversation conversation = conversations.getFirst();
      assertEquals(conversation.getConversationInitiator(), userModel.getEntity());
    }

    @Test
    void shouldSendFeedbackToConversation() {
      controller.sendNoteFeedback(note, feedback);
      List<Conversation> conversations =
          (List<Conversation>) modelFactoryService.conversationRepository.findAll();
      assertEquals(1, conversations.size());

      Conversation conversation = conversations.getFirst();
      List<ConversationMessage> conversationMessages =
          conversationService.getConversionDetailRelatedByConversationId(conversation.getId());
      assertEquals(1, conversationMessages.size());

      ConversationMessage message = conversationMessages.getFirst();
      assertEquals(message.getMessage(), feedback);
    }
  }

  @Nested
  class UpdateReviewSetting {
    Note source;
    Note target;
    Note link;

    @BeforeEach
    void setup() {
      source = makeMe.aNote().creatorAndOwner(userModel).please();
      target = makeMe.aNote().creatorAndOwner(userModel).please();
      link = makeMe.aLink().between(source, target).please();
    }

    @Test
    void shouldUpdateLinkLevel() throws UnexpectedNoAccessRightException {
      @Valid ReviewSetting reviewSetting = new ReviewSetting();
      reviewSetting.setLevel(4);
      controller.updateReviewSetting(source, reviewSetting);
      assertThat(getLevel(link), is(4));
    }

    @Test
    void shouldUpdateReferenceLevel() throws UnexpectedNoAccessRightException {
      @Valid ReviewSetting reviewSetting = new ReviewSetting();
      reviewSetting.setLevel(4);
      controller.updateReviewSetting(target, reviewSetting);
      assertThat(getLevel(link), is(4));
    }

    private static Integer getLevel(Note link) {
      return link.getReviewSetting().getLevel();
    }
  }
}
