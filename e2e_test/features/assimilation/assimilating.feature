Feature: Assimilating
    As a learner, I want to recall my notes. When user's memory of a note is tracked for the first time,
    there should be a daily max and I should be able to give some of my initial impression
    of the note, and/or choose to skip it for any further recalls.

  Background:
      Given I am logged in as an existing user
      And my daily new notes to review is set to 2
      And there are notes from Note 1 to Note 5

  Scenario: First day of review
      Given It's day 1, 8 hour
      Then I assimilate these in sequence:
          | Review Type   | Title      |
          | single note   | Note 1     |
          | single note   | Note 2     |
          | initial done  |            |
      Given It's day 1, 9 hour
      Then I assimilate these in sequence:
          | Review Type   | Title      |
          | initial done  |            |

  Scenario: First day review only part of the daily number
      Given It's day 1, 8 hour
      Then I assimilate these in sequence:
          | Review Type   | Title      |
          | single note   | Note 1     |
      Given It's day 1, 9 hour
      Then I assimilate these in sequence:
          | Review Type   | Title      |
          | single note   | Note 2     |
          | initial done  |            |

  Scenario: Skip review
      Given It's day 1, 8 hour
      When I assimilate these in sequence:
          | Review Type   | Title      | Skip  |
          | single note   | Note 1     | yes   |
          | single note   | Note 2     | no    |
          | single note   | Note 3     | no    |
          | initial done  |            |       |

