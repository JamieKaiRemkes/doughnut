/// <reference types="cypress" />
/// <reference types="../support" />
// @ts-check

import { Given, Then, When } from '@badeball/cypress-cucumber-preprocessor'
import { DataTable } from '@cucumber/cucumber'
import start from '../start'

Then('I do these initial reviews in sequence:', (data: DataTable) => {
  cy.initialReviewInSequence(data.hashes())
})

Given("It's day {int}, {int} hour", (day: number, hour: number) => {
  start.testability().backendTimeTravelTo(day, hour)
})

Given('I ask to do more repetition', () => {
  cy.repeatMore()
})

Then('I repeat old {string}', (repeatNotes: string) => {
  cy.goAndRepeatReviewNotes(repeatNotes)
})

Then('I repeat more old {string}', (repeatNotes: string) => {
  cy.repeatReviewNotes(repeatNotes)
})

Then(
  'On day {int} I repeat old {string} and initial review new {string}',
  (day: number, repeatNotes: string, initialNotes: string) => {
    start.testability().backendTimeTravelTo(day, 8)
    cy.goAndRepeatReviewNotes(repeatNotes)
    cy.initialReviewNotes(initialNotes)
  }
)

Given('I go to the reviews page', () => {
  cy.routerToReviews()
})

Then('I should see that I have old notes to repeat', () => {
  cy.findByRole('button', { name: 'Start reviewing old notes' })
})

Then('I should see that I have new notes to learn', () => {
  cy.findByRole('button', { name: 'Start reviewing new notes' })
})

Then(
  'On day {int} I should have {string} note for initial review and {string} for repeat',
  (day: number, numberOfInitialReviews: string, numberOfRepeats: string) => {
    start.testability().backendTimeTravelTo(day, 8)
    cy.routerToReviews()
    cy.contains(numberOfInitialReviews, {
      selector: '.doughnut-ring .initial-review',
    })
    cy.routerToReviews()
    cy.findByText(numberOfInitialReviews, {
      selector: '.number-of-initial-reviews',
    })
    cy.findByText(numberOfRepeats, { selector: '.number-of-repeats' })
  }
)

Then('it should move to review page', () => {
  cy.url().should('eq', `${Cypress.config().baseUrl}/reviews`)
})

Then('I initial review {string}', (noteTopic) => {
  cy.initialReviewNotes(noteTopic)
})

Then(
  'I added and learned one note {string} on day {int}',
  (noteTopic: string, day: number) => {
    start.testability().injectNotes([{ Topic: noteTopic }])
    start.testability().backendTimeTravelTo(day, 8)
    cy.initialReviewNotes(noteTopic)
  }
)

Then(
  'I learned one note {string} on day {int}',
  (noteTopic: string, day: number) => {
    start.testability().backendTimeTravelTo(day, 8)
    cy.initialReviewNotes(noteTopic)
  }
)

Then('I am repeat-reviewing my old note on day {int}', (day: number) => {
  start.testability().backendTimeTravelTo(day, 8)
  cy.routerToRepeatReview()
})

Then('I am learning new note on day {int}', (day: number) => {
  start.testability().backendTimeTravelTo(day, 8)
  cy.routerToInitialReview()
})

Then(
  'I set the level of {string} to be {int}',
  (noteTopic: string, level: number) => {
    start.assumeNotePage(noteTopic)
    cy.formField('Level').then(($control) => {
      cy.wrap($control).within(() => {
        cy.findByRole('button', { name: `${level}` }).click()
      })
    })
  }
)

Then('I have selected the choice {string}', (choice: string) => {
  cy.formField(choice).check()
  cy.findByRole('button', { name: 'Keep for repetition' }).click()
})

Then('I choose yes I remember', () => {
  cy.yesIRemember()
})

Then(
  'I should be asked cloze deletion question {string} with options {string}',
  (question: string, options: string) => {
    cy.shouldSeeQuizWithOptions([question], options)
  }
)

Then(
  'I should be asked image question {string} with options {string}',
  (_imageInQuestion: string, options: string) => {
    cy.shouldSeeQuizWithOptions([], options)
  }
)

Then(
  'I should be asked spelling question {string} from notebook {string}',
  (question: string, notebook: string) => {
    cy.expectBreadcrumb(notebook)
    cy.findByText(question).should('be.visible')
  }
)

Then(
  'I should be asked link question {string} {string} with options {string}',
  (noteTopic: string, linkType: string, options: string) => {
    cy.shouldSeeQuizWithOptions([noteTopic, linkType], options)
  }
)

Then('I type my answer {string}', (answer: string) => {
  cy.replaceFocusedTextAndEnter(answer)
})

Then('I choose answer {string}', (noteTopic: string) => {
  cy.findByRole('button', { name: noteTopic }).click()
})

Then('I should see the information of note {string}', (noteTopic: string) => {
  start.assumeNotePage(noteTopic)
})

Then('I should see that my answer {string} is incorrect', (answer) => {
  cy.findByText(`Your answer \`${answer}\` is incorrect.`)
})

Then('I should see the repetition is finished: {string}', (yesNo) => {
  cy.findByText(
    'You have finished all repetitions for this half a day!'
  ).should(yesNo === 'yes' ? 'exist' : 'not.exist')
})

Then('I should see that my answer is correct', () => {
  start.assumeAnsweredQuestionPage().expectLastAnswerToBeCorrect()
})

Then('I should see that my last answer is correct', () => {
  start
    .assumeAnsweredQuestionPage()
    .goToLastResult()
    .expectLastAnswerToBeCorrect()
})

Then(
  'I should see the review point info of note {string}',
  (noteTopic: string, data: DataTable) => {
    start
      .assumeAnsweredQuestionPage()
      .showReviewPoint(noteTopic)
      .expectReviewPointInfo(data.hashes()[0] ?? {})
  }
)

Then('choose to remove the last review point from reviews', () => {
  start
    .assumeAnsweredQuestionPage()
    .goToLastResult()
    .showReviewPoint()
    .removeReviewPointFromReview()
})

Then('the choice {string} should be correct', (choice: string) => {
  start.assumeQuestionPage().expectChoiceToBe(choice, 'correct')
})

Then('the choice {string} should be incorrect', (choice: string) => {
  start.assumeQuestionPage().expectChoiceToBe(choice, 'incorrect')
})

When(
  "I've got the following question for a note with topic {string}:",
  (noteTopic: string, question: DataTable) => {
    start.stubOpenAIQuestionGenerationAndSeeTheQuestion(
      noteTopic,
      question.hashes()[0] ?? {}
    )
  }
)

When(
  'I have the true false question {string} rated as a good example',
  (questionStem: string) => {
    start.testability().injectSuggestedQuestion(questionStem, true)
  }
)

When(
  'I have the true false question {string} rated as a bad example',
  (questionStem: string) => {
    start.testability().injectSuggestedQuestion(questionStem, false)
  }
)

Then('I should be asked {string}', (expectedQuestionStem: string) => {
  start.assumeQuestionPage(expectedQuestionStem)
})

Then(
  'I should see the question {string} is disabled',
  (questionStem: string) => {
    start.assumeQuestionPage(questionStem).isDisabled()
  }
)

Then(
  'I should see the question {string} is enabled',
  (questionStem: string) => {
    start.assumeQuestionPage(questionStem).isNotDisabled()
  }
)

Then(
  'I suggest the displayed question {string} as a good example',
  (questionStem: string) => {
    start
      .assumeQuestionPage(questionStem)
      .suggestingThisQuestionForFineTuning()
      .suggestingPositiveFeedbackForFineTuning()
  }
)

Then(
  'I suggest the displayed question {string} as a bad example',
  (questionStem: string) => {
    start
      .assumeQuestionPage(questionStem)
      .suggestingThisQuestionForFineTuning()
      .suggestingNegativeFeedbackFineTuningExclusion()
  }
)
