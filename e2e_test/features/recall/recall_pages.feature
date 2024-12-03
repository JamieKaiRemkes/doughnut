Feature: Review Pages
  As a learner, I want to review my notes and links so that I have fresh memory.

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "English" which skips review
    And there are some notes:
      | Topic    | Details         | Image Url   | Parent Topic |
      | Sedition | Incite violence |             | English      |
      | Sedation | Put to sleep    |             | English      |
      | Sedative | Sleep medicine  | a_slide.jpg | English      |
    And there is "similar to" link between note "Sedition" and "Sedation"

  Scenario: Different review pages for different notes
    * I assimilate these in sequence:
      | Review Type  | Topic    | Additional Info             |
      | single note  | Sedition | Incite violence             |
      | single note  | Sedation | Put to sleep                |
      | image note   | Sedative | Sleep medicine; a_slide.jpg |
      | link         | Sedition | similar to; Sedation        |
      | initial done |          |                             |

  Scenario: Count of recall and assimilate notes
    Given It's day 1, 8 hour
    And I assimilate these in sequence:
      | Review Type | Topic    |
      | single note | Sedition |
    When It's day 2, 9 hour
    And I go to the recalls page
    Then I should see that I have old notes to repeat
    And I should see that I have 3 new notes to assimilate

