package com.odde.doughnut.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.ConversationService;
import com.odde.doughnut.testability.MakeMe;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestConversationMessageControllerTest {

  @Autowired ConversationService conversationService;
  @Autowired MakeMe makeMe;
  private UserModel currentUser;
  RestConversationMessageController controller;

  @Autowired ModelFactoryService modelFactoryService;
  AssessmentQuestionInstance assessmentQuestionInstance;

  @BeforeEach
  void setup() {
    currentUser = makeMe.aUser().toModelPlease();
    controller = new RestConversationMessageController(currentUser, conversationService);
    Notebook notebook = makeMe.aNotebook().please();
    AssessmentAttempt assessmentAttempt =
        makeMe.anAssessmentAttempt(notebook.getCreatorEntity()).withOneQuestion().please();
    assessmentQuestionInstance = assessmentAttempt.getAssessmentQuestionInstances().get(0);
  }

  @Test
  void teststartConversationAboutAssessmentQuestionReturnsOk() {
    String feedback = "This is a feedback";
    controller.startConversationAboutAssessmentQuestion(feedback, assessmentQuestionInstance);
    List<Conversation> conversations =
        (List<Conversation>) modelFactoryService.conversationRepository.findAll();
    assertEquals(1, conversations.size());
  }

  @Test
  void testGetMessageDetailWhenstartConversationAboutAssessmentQuestionReturnsOk() {
    String feedback = "This is a feedback";
    Conversation conversation =
        controller.startConversationAboutAssessmentQuestion(feedback, assessmentQuestionInstance);

    List<Conversation> conversations =
        (List<Conversation>) modelFactoryService.conversationRepository.findAll();

    makeMe.refresh(conversation);
    var conversationDetail = conversation.getConversationMessages();
    assertEquals(1, conversations.size());
    assertEquals(1, conversationDetail.size());
    assertEquals(feedback, conversationDetail.getFirst().getMessage());
  }

  @Test
  void testGetFeedbackThreadsSendFromTheUser() {
    makeMe
        .aConversation()
        .forAnAssessmentQuestionInstance(assessmentQuestionInstance)
        .from(currentUser)
        .please();
    makeMe.aConversation().forAnAssessmentQuestionInstance(assessmentQuestionInstance).please();
    List<Conversation> conversations = controller.getConversationsOfCurrentUser();
    assertEquals(1, conversations.size());
  }

  @Test
  void testGetFeedbackThreadsAsReceiver() {
    makeMe
        .theNotebook(assessmentQuestionInstance.getAssessmentAttempt().getNotebook())
        .owner(currentUser.getEntity())
        .please();
    makeMe.aConversation().forAnAssessmentQuestionInstance(assessmentQuestionInstance).please();
    List<Conversation> conversations = controller.getConversationsOfCurrentUser();
    assertEquals(1, conversations.size());
  }

  @Test
  void testGetFeedbackThreadsAsAMemberOfACircle() {
    Circle myCircle = makeMe.aCircle().hasMember(currentUser.getEntity()).please();
    makeMe
        .theNotebook(assessmentQuestionInstance.getAssessmentAttempt().getNotebook())
        .owner(myCircle)
        .please();
    makeMe.aConversation().forAnAssessmentQuestionInstance(assessmentQuestionInstance).please();
    List<Conversation> conversations = controller.getConversationsOfCurrentUser();
    assertEquals(1, conversations.size());
  }

  @Nested
  class RestConversationMessageControllerUnreadCountTest {
    Conversation conversation;

    @BeforeEach
    void setup() {
      conversation = makeMe.aConversation().from(currentUser).please();
    }

    @Test
    void forLoginUserOnly() {
      controller =
          new RestConversationMessageController(makeMe.aNullUserModelPlease(), conversationService);
      ResponseStatusException exception =
          assertThrows(ResponseStatusException.class, () -> controller.getUnreadConversations());
      assertEquals(HttpStatusCode.valueOf(401), exception.getStatusCode());
    }

    @Test
    void getOneUnreadConversationCountOfCurrentUser() {
      makeMe.aConversationMessage(conversation).sender(currentUser.getEntity()).please();
      makeMe.aConversationMessage(conversation).sender(makeMe.aUser().please()).please();
      int conversations = controller.getUnreadConversations().size();
      assertEquals(1, conversations);
    }

    @Test
    void countMessagesInsteadOfConversations() {
      User sender = makeMe.aUser().please();
      makeMe.aConversationMessage(conversation).sender(sender).please();
      makeMe.aConversationMessage(conversation).sender(sender).please();
      makeMe.aConversationMessage(conversation).sender(sender).please();

      int conversations = controller.getUnreadConversations().size();
      assertEquals(3, conversations);
    }

    @Test
    void zeroUnreadConversationCountForSender() {
      makeMe.aConversationMessage(conversation).sender(currentUser.getEntity()).please();

      int conversations = controller.getUnreadConversations().size();
      assertEquals(0, conversations);
    }

    @Test
    void getZeroUnreadConversationWhenSenderIsCurrentUser() {
      Conversation conversation = makeMe.aConversation().from(currentUser).please();
      makeMe
          .aConversationMessage(conversation)
          .sender(currentUser.getEntity())
          .readByReceiver()
          .please();

      int conversations = controller.getUnreadConversations().size();
      assertEquals(0, conversations);
    }
  }

  @Nested
  class MarkConversationAsRead {
    Conversation conversation;

    @BeforeEach
    void setup() {
      conversation = makeMe.aConversation().from(currentUser).please();
    }

    @Test
    void shouldNotBeAbleToMarkOtherPeopleMsg() {
      Conversation uninvolvedConversation = makeMe.aConversation().please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.markConversationAsRead(uninvolvedConversation));
    }

    @Test
    void testMarkConversationAsReadReduceTheCount() throws UnexpectedNoAccessRightException {
      ConversationMessage msg =
          makeMe.aConversationMessage(conversation).sender(makeMe.aUser().please()).please();
      List<ConversationMessage> messages = controller.markConversationAsRead(conversation);
      assertThat(messages.size()).isEqualTo(0);
    }

    @Test
    void testMarkConversationAsReadByReceiver() throws UnexpectedNoAccessRightException {
      ConversationMessage msg =
          makeMe.aConversationMessage(conversation).sender(makeMe.aUser().please()).please();
      assertEquals(false, msg.getReadByReceiver());
      controller.markConversationAsRead(conversation);
      assertEquals(true, msg.getReadByReceiver());
    }

    @Test
    void testMarkConversationAsReadBySender() throws UnexpectedNoAccessRightException {
      ConversationMessage msg =
          makeMe.aConversationMessage(conversation).sender(currentUser.getEntity()).please();
      controller.markConversationAsRead(conversation);
      assertEquals(false, msg.getReadByReceiver());
    }
  }

  @Nested
  class ReplyToConversation {
    @Test
    void shouldNotBeAbleToReplyToAConversationIAmNotIn() {
      Conversation conversation = makeMe.aConversation().please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.replyToConversation("hi", conversation));
    }

    @Test
    void topicOwnerShouldBeAbleToReply() throws UnexpectedNoAccessRightException {
      String message = "This is a message";
      makeMe
          .theNotebook(assessmentQuestionInstance.getAssessmentAttempt().getNotebook())
          .owner(currentUser.getEntity())
          .please();
      Conversation conversation =
          makeMe
              .aConversation()
              .forAnAssessmentQuestionInstance(assessmentQuestionInstance)
              .please();
      ConversationMessage conversationMessage =
          controller.replyToConversation(message, conversation);
      List<ConversationMessage> conversationMessages =
          (List<ConversationMessage>) modelFactoryService.conversationMessageRepository.findAll();
      assertEquals(1, conversationMessages.size());
      assertEquals(message, conversationMessage.getMessage());
    }

    @Test
    void initiatorShouldBeAbleToReply() throws UnexpectedNoAccessRightException {
      String message = "This is a message";
      Conversation conversation = makeMe.aConversation().from(currentUser).please();
      ConversationMessage conversationMessage =
          controller.replyToConversation(message, conversation);
      assertEquals(message, conversationMessage.getMessage());
    }
  }

  @Nested
  class GetConversationDetails {

    @Test
    void shouldNotBeAbleToSeeAConversationIAmNotIn() {
      Conversation conversation = makeMe.aConversation().please();
      assertThrows(
          UnexpectedNoAccessRightException.class,
          () -> controller.getConversationMessages(conversation));
    }

    @Test
    void testGetMessageThreadsFromConversation() throws UnexpectedNoAccessRightException {
      Conversation conversation = makeMe.aConversation().from(currentUser).please();

      makeMe.aConversationMessage(conversation).please();
      List<ConversationMessage> conversations = controller.getConversationMessages(conversation);
      assertEquals(1, conversations.size());
    }
  }

  @Nested
  class startConversationAboutNoteTests {
    Note note;
    String msg = "This is a feedback sent from note";

    @BeforeEach
    void setup() {
      UserModel noteOwner = makeMe.aUser().toModelPlease();
      note = makeMe.aNote().creatorAndOwner(noteOwner).please();
    }

    @Test
    void shouldStartConversation() {
      controller.startConversationAboutNote(note, msg);
      List<Conversation> conversations =
          (List<Conversation>) modelFactoryService.conversationRepository.findAll();
      assertEquals(1, conversations.size());
      Conversation conversation = conversations.getFirst();
      assertEquals(conversation.getConversationInitiator(), currentUser.getEntity());
    }

    @Test
    void shouldstartConversationAboutAssessmentQuestionToConversation() {
      Conversation conversation = controller.startConversationAboutNote(note, msg);
      makeMe.refresh(conversation);
      List<ConversationMessage> conversationMessages = conversation.getConversationMessages();
      assertEquals(1, conversationMessages.size());
      ConversationMessage message = conversationMessages.getFirst();
      assertEquals(message.getMessage(), msg);
    }
  }

  @Nested
  class ConversationOrderingTests {
    User otherUser;

    @BeforeEach
    void setup() {
      otherUser = makeMe.aUser().please();
    }

    @Test
    void testConversationsOrderedByLastMessageTime() {
      Conversation conv1 = makeMe.aConversation().from(currentUser).please();
      Conversation conv2 = makeMe.aConversation().from(currentUser).please();
      Conversation conv3 = makeMe.aConversation().from(currentUser).please();

      // Add messages with specific timestamps
      makeMe
          .aConversationMessage(conv1)
          .sender(otherUser)
          .createdAt(makeMe.aTimestamp().of(1, 1).please())
          .please();
      makeMe
          .aConversationMessage(conv2)
          .sender(otherUser)
          .createdAt(makeMe.aTimestamp().of(1, 2).please())
          .please();
      makeMe
          .aConversationMessage(conv3)
          .sender(otherUser)
          .createdAt(makeMe.aTimestamp().of(1, 3).please())
          .please();

      List<Conversation> orderedConversations = controller.getConversationsOfCurrentUser();

      assertIterableEquals(List.of(conv3, conv2, conv1), orderedConversations);
    }

    @Test
    void testConversationsOrderedByCreationTimeWhenNoMessages() {
      Conversation conv1 =
          makeMe
              .aConversation()
              .from(currentUser)
              .createdAt(makeMe.aTimestamp().of(1, 1).please())
              .please();
      Conversation conv2 =
          makeMe
              .aConversation()
              .from(currentUser)
              .createdAt(makeMe.aTimestamp().of(1, 2).please())
              .please();
      Conversation conv3 =
          makeMe
              .aConversation()
              .from(currentUser)
              .createdAt(makeMe.aTimestamp().of(1, 3).please())
              .please();

      List<Conversation> orderedConversations = controller.getConversationsOfCurrentUser();

      assertIterableEquals(List.of(conv3, conv2, conv1), orderedConversations);
    }
  }
}
