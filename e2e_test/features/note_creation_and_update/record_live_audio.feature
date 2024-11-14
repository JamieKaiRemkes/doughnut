@usingMockedOpenAiService
Feature: Recording a live audio and append to note details
  As a learner, I want to create a note to capture the audio of a live event and append it to the note details

  Background:
    Given I am logged in as an existing user
    And I have a notebook with the head note "Data Structure Lecture" and details "This is class 1."
    And the OpenAI transcription service will return the following srt transcript:
      """
      00:00:00,000 --> 00:00:01,000
      its talk about dada struct day.

      """
    And OpenAI assistant will reply below for user messages in a non-stream run:
      | response                | arguments                 |
      | complete note details   |"Let's talk about data structure today."|
    And the browser is mocked to give permission to record audio

  Scenario: Record audio of a live event
    Given I start recording audio for the note "Data Structure Lecture"
    And the browser records audio input from the microphone as in "lecture.wav"
    When I stop recording audio
    Then the note details on the current page should be "This is class 1.Let's talk about data structure today."

  Scenario: Download the audio file to local machine
    Given I start recording audio for the note "Data Structure Lecture"
    And the browser records audio input from the microphone as in "lecture.wav"
    When I stop recording audio
    Then I must be able to download the audio file to my local machine and it matches the size 123

  @mockBrowserTime
  Scenario: Record long lecture and continuous converting
    Given I start recording audio for the note "Data Structure Lecture"
    And the browser records audio input from the microphone as in "lecture.wav"
    When it is 2 minutes later in the browser
    Then the note details on the current page should be "This is class 1.Let's talk about data structure today."
    When I stop recording audio
    Then the note details on the current page should be "This is class 1.Let's talk about data structure today."
