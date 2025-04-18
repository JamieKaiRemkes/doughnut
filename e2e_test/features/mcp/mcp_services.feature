Feature: MCP (Model Context Protocol) Services
  As a note taker, I want my AI clients like Cursor to use the MCP services from
  Doughnut, so that AI can automatically update my notes and fetch information from my
  notes.

  Background:
    Given I am logged in as an existing user
    And I have a MCP Token
    And I connect to an MCP client that connects to Doughnut MCP service with my MCP token

  Scenario Outline: MCP API calls
    When I call the "<api_name>" MCP tool
    Then the response should contain "<expected_response>"

    Examples:
      | api_name        | expected_response                               |
      | getInstruction  | Doughnut is a Personal Knowledge Management tool |
      | getUsername     | Terry                                           |
