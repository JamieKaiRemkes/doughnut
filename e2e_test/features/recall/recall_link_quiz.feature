Feature: Repetition Link Quiz
  As a learner, I want to use quizzes in my repetition to help and gamify my learning.

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "Space" which skips review
    And there are some notes:
      | Title            | Skip Memory Tracking| Parent Title|
      | Moon             | true       | Space       |
      | Earth            | true       | Space       |
      | Mars             | true       | Space       |
    And there is "a specialization of" link between note "Moon" and "Earth"

  Scenario Outline: "Belongs to" question
    Given It's day 1, 8 hour
    And I assimilate these in sequence:
      | Review Type | Title |
      | single note | Moon  |
    When I am recalling my note on day 2
    Then I should be asked link question "Moon" "a specialization of" with options "Earth, Mars"
    When I choose answer "<answer>"
    Then I should see that my answer <result>

    Examples:
      | answer | result              |
      | Mars   | "Mars" is incorrect |
      # | Earth  | is correct          |

