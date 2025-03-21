Feature: search note
  As a learner, I want to maintain my newly acquired knowledge in
  notes that linking to each other, so that I can review them in the
  future.

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "Sedation" and details "Put to sleep"
    And I have a notebook with the head note "Sedative" and details "Sleep medicine"
    And there are some notes:
      | Title    | Parent Title |
      | Physical | Sedation     |
      | Magical  | Sedation     |

  @mockBrowserTime
  Scenario Outline: Search at the top level
    Given I visit all my notebooks
    Then I start searching
    Then I should see "<targets>" as targets only when searching "<search key>"
    Examples:
      | search key | targets            |
      | Sed        | Sedation, Sedative |
      | Sedatio    | Sedation           |

  @mockBrowserTime
  Scenario: Search when adding new note
    Given I am creating a note under "Sedation"
    When I type "ph" in the title
    Then I should see "Physical" as the possible duplicate
