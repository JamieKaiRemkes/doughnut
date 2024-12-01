Feature: Editing when memory tracker onboarding

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "hard"
    And I have a notebook with the head note "easy"

  Scenario: Update note
    When I am learning new note on day 1
    Then I can change the topic "hard" to "harder"

  Scenario: Update recall setting
    Given I am learning new note on day 1
    When I set the level of "hard" to be 2
    Then I learned one note "easy" on day 1
    And I learned one note "hard" on day 2


