Feature: Message Center with Unread Message Count
    As a user, I want to see the number of unread messages in the message center.

  Background:
    Given there is a notebook with head note "Rocket Science" from user "a_trainer" shared to the Bazaar
    When "old_learner" start a conversation about the note "Rocket Science" with a message "Hi"

  Scenario: Message receiver should have 1 unread message while sender has none
    Then there should be no unread message for the user "old_learner"
    And "a_trainer" should have 1 unread messages

  Scenario: The receiver's reply should increase the unread count of the sender
    Given I am re-logged in as "a_trainer"
    When I reply "Hi. What can I do for you?" to the conversation "Rocket Science"
    Then "old_learner" should have 1 unread messages

  Scenario: The message is read by the receiver
    Given I am re-logged in as "a_trainer" and reload the page
    When I read the conversation with "Old Learner" for the topic "Rocket Science" in the message center
    Then I should have no unread messages

  Scenario: An already read message is read again
    Given I am re-logged in as "old_learner" and reload the page
    When I read the conversation with "A Trainer" for the topic "Rocket Science" in the message center
    Then I should have no unread messages
