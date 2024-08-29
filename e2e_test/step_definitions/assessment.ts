import {
  DataTable,
  Given,
  Then,
  When,
} from '@badeball/cypress-cucumber-preprocessor'
import '../support/string_util'
import start from '../start'

When(
  'I start the assessment on the {string} notebook in the bazaar',
  (notebook: string) => {
    start.navigateToBazaar().selfAssessmentOnNotebook(notebook)
  }
)

When('I start assessment {string} in the bazaar', function (notebook: string) {
  start.navigateToBazaar().selfAssessmentOnNotebook(notebook)
})

When('I click on answer {string}', (answer: string) => {
  start
    .assumeAssessmentPage()
    .assumeQuestionSection()
    .answerWithoutContinuing(answer)
})

When(
  'I do the assessment on {string} in the bazaar with the following answers:',
  function (notebook: string, table: DataTable) {
    start
      .navigateToBazaar()
      .selfAssessmentOnNotebook(notebook)
      .answerQuestionsFromTable(table.hashes())
  }
)

When(
  '{int} subsequent attempts of assessment on the {string} notebook should use {int} questions',
  (attempts: number, notebook: string, minUniqueQuestionsThreshold: number) => {
    const questions: string[] = []
    for (let i = 0; i < attempts; i++) {
      start.navigateToBazaar().selfAssessmentOnNotebook(notebook)
      const question = start.assumeAssessmentPage().assumeQuestionSection()
      question.getStemText().then((stem) => {
        questions.push(stem)
        question.answerFirstOption()
      })
    }
    cy.then(() => {
      const uniqueQuestions = new Set(questions)
      expect(uniqueQuestions.size).to.equal(minUniqueQuestionsThreshold)
    })
  }
)

Then(
  'I should see the score {string} at the end of assessment',
  (expectedScore: string) => {
    start.assumeAssessmentPage().expectEndOfAssessment(expectedScore)
  }
)

Then('I should see error message Not enough questions', () => {
  cy.findByText('Not enough questions').should('be.visible')
})

Then('I should see error message The assessment is not available', () => {
  cy.findByText('The assessment is not available').should('be.visible')
})

Given(
  'OpenAI now refines the question to become:',
  (questionTable: DataTable) => {
    start
      .questionGenerationService()
      .resetAndStubAskingMCQ(questionTable.hashes()[0]!)
  }
)

When(
  'I achieve a score of {int}\\/{int} in the assessment of the notebook {string}',
  (correctAnswers: number, allQuestions: number, notebook: string) => {
    start
      .navigateToBazaar()
      .selfAssessmentOnNotebook(notebook)
      .answerYesNoQuestionsToScore(correctAnswers, allQuestions)
  }
)

When('I pass the assessment on {string}', (notebook: string) => {
  start.navigateToBazaar().selfAssessmentOnNotebook(notebook)
  start.assumeAssessmentPage(notebook).answerYesNoQuestionsToScore(2, 2)
})

Then('I can download a certificate after passing an assessment', () => {
  start.assumeAssessmentPage().expectEndOfAssessment().expectCertificate()
})

Then('I cannot download a certificate after passing an assessment', () => {
  start
    .assumeAssessmentPage()
    .expectEndOfAssessment()
    .expectCertificateCannotBeObtained()
})

Then('I should pass the assessment of {string}', (notebook: string) => {
  start.assumeAssessmentPage(notebook).expectEndOfAssessment().passAssessment()
})

Then('I should not pass the assessment of {string}', (notebook: string) => {
  start
    .assumeAssessmentPage(notebook)
    .expectEndOfAssessment()
    .expectNotPassAssessment()
})

Given(
  'there is a notebook {string} by {string} with {int} questions, shared to the Bazaar',
  (notebook: string, creatorId: string, numberOfQuestion: number) => {
    start
      .testability()
      .injectNumbersNotebookWithQuestions(notebook, numberOfQuestion, creatorId)
  }
)

Given(
  'there is a certified notebook {string} by {string} with 2 questions, shared to the Bazaar',
  (notebook: string, creatorId: string) => {
    start
      .testability()
      .injectNumbersNotebookWithQuestions(notebook, 2, creatorId, true)
  }
)

Then(
  'I should see the result {string} for the notebook {string} in my assessment and certificate history',
  (result: string, notebook: string) => {
    start
      .navigateToAssessmentAndCertificatePage()
      .expectTableWithNumberOfRow(1)
      .checkAttemptResult(notebook, result)
  }
)

When(
  'I should get a certificate of {string} for {string} from {string}',
  (notebook: string, user: string, creator: string) => {
    start
      .assumeAssessmentPage(notebook)
      .expectEndOfAssessment()
      .expectCertificate()
      .expectCertificateFor(notebook)
      .expectCertificateUser(user)
      .expectCertificateCreator(creator)
  }
)

Then(
  'I should not get a certificate of {string} for {string} from {string}',
  (notebook: string) => {
    start
      .assumeAssessmentPage(notebook)
      .expectEndOfAssessment()
      .expectNoCertificate()
  }
)

When('the current date is {string}', (dateString: string) => {
  start.testability().backendTimeTravelToDate(new Date(dateString))
})

When(
  'I should see the original start date {string} on my renewed certificate for {string}',
  (dateString: string, notebook: string) => {
    start
      .assumeAssessmentPage(notebook)
      .expectEndOfAssessment()
      .expectCertificate()
      .expectDate(dateString)
  }
)

Then(
  'I can view the certificate for the notebook {string} in my assessment and certificate history',
  (notebook: string) => {
    start
      .navigateToAssessmentAndCertificatePage()
      .expectCertificate(notebook)
      .expectCertificateFor(notebook)
  }
)

Then(
  'I cannot view the certificate for the notebook {string} in my assessment and certificate history',
  (notebook: string) => {
    start.navigateToAssessmentAndCertificatePage().expectNoCertificate(notebook)
  }
)
Then(
  'it should immediately show {string} as the wrong answer after answering',
  (answer: string) => {
    cy.contains(answer).should('have.class', 'is-incorrect')
    cy.get('.is-incorrect').should('have.length', 1);
  }
)

Then('I answer the question wrongly', () => {
  cy.findByRole('button', { name: 'No' }).click()
})

Then('I submit my feedback: {string}', (feedback: string) => {
  cy.findByRole('button', { name: 'No' }).click()
  cy.findByText('Send feedback').click()
  cy.findByPlaceholderText('Give feedback about the question').type(feedback)
  cy.findByRole('button', { name: 'Submit' }).click()
  cy.findByText('Feedback received successfully').should('be.visible')
})
