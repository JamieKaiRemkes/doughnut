@ignore
Feature: Normal Mode in Race Game

  @normal_mode
  Scenario: Move car when dice number is even
    Given the car at position 3
    And the round count is 2
    When the dice outcome is 4
    Then the car should move to position 5
    And the round count becomes 3
  
  @normal_mode
  Scenario: Move car when dice number is odd
    Given the car at position 3
    And the round count is 2
    When the dice outcome is 5
    Then the car should move to position 4
    And the round count becomes 3

  @normal_mode
  Scenario: Reset the game
    Given the car at position 21
    And the round count is 10
    When the player reset the game
    Then the car should move to position 0
    And the round count becomes 1
