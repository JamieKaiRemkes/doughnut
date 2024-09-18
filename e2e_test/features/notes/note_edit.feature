Feature: Note Edit
  As a learner, I want to edit and undo editing for single user,
  with topic and details only within a session.

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "LeSS in Action" and details "An awesome training"

  Scenario: Edit a note
    And I update note "LeSS in Action" to become:
      | Topic     | Details           |
      | Odd-e CSD | Our best training |
    Then I should see "Odd-e CSD" in the page
    And I should see these notes belonging to the user at the top level of all my notes
      | Topic     | Details           |
      | Odd-e CSD | Our best training |

  Scenario: Edit a note topic and edit details and undo
    Given I update note topic "LeSS in Action" to become "Odd-e CSD"
    And I should see "Odd-e CSD" in the page
    And I update note "Odd-e CSD" details from "An awesome training" to become "A super awesome training"
    And I should see "A super awesome training" in the page
    When I undo "edit details"
    Then I should see "An awesome training" in the page
    When I undo "edit topic" again
    Then I should see "LeSS in Action" in the page
    And there should be no more undo to do

  Scenario: Edit a note's details as markdown
    When I update note "LeSS in Action" details using markdown to become:
    """
    # Odd-e LiA
    ## Our best training

    * Specification by Example
      * Discuss in workshop
      * Conccurent engineering
      * Living documentation
    * Test-Driven Development
    """
    Then I should see the rich content of the note with details:
      | Tag | Content                  |
      | h1  | Odd-e LiA                |
      | h2  | Our best training        |
      | li  | Specification by Example |
