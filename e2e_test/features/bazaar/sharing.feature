Feature: Bazaar sharing
  There should be a bazaar in Doughnut that people can share their public notes,
  and others can subscribe to their notes.

  Background:
    Given I am logged in as an existing user

  Scenario: Contributing To Bazaar
    Given I have a notebook with head note "Shape" and notes:
      | Title    | Details                   | Parent Title |
      | Square   | four equal straight sides | Shape        |
      | Triangle | three sides shape         | Shape        |
    When I choose to share my notebook "Shape"
    Then I should see "Shape" shared in the Bazaar

