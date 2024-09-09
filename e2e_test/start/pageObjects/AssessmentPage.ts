import { CertificatePopup } from './CertificatePopup'

const assumeWrongAnswerPage = () => {
  return {
    continueAssessment() {
      cy.findByRole('button', { name: 'Continue' }).click().pageIsNotLoading()
      cy.get('.current-choice').should('have.length', 0)
    },
    sendFeedback(feedback: string) {
      cy.findByText('Send feedback').click()
      cy.findByPlaceholderText('Give feedback about the question').type(
        feedback
      )
      cy.findByRole('button', { name: 'Submit' }).click()
      cy.findByText('Feedback received successfully').should('be.visible')
    },
    highlightCurrentChoice(choice: string) {
      cy.contains(choice).should('have.class', 'current-choice')
      cy.get('.current-choice').should('have.length', 1)
    },
  }
}

const assumeQuestionSection = () => {
  return {
    getQuestionSection() {
      return cy.get('[data-test="question-section"]')
    },
    getStemText() {
      return this.getQuestionSection()
        .get('[data-test="stem"]')
        .first()
        .invoke('text')
    },
    answerFirstOption() {
      return this.getQuestionSection().get('button').first().click()
    },
    answerFromTable(answersTable: Record<string, string>[]) {
      return this.getStemText().then((stem) => {
        const row = answersTable.find((row) => row.Question === stem)
        if (!row) {
          throw new Error(`No answer found for question: ${stem}`)
        }
        if (row.AnswerCorrect === 'true') {
          this.answer(row.Answer!)
        } else {
          this.answerIncorrectAndContinue(row.Answer!)
        }
      })
    },
    answer(answer: string) {
      cy.findByText(answer).click().pageIsNotLoading()
      return this
    },
    answerIncorrectAndContinue(answer: string) {
      this.answerIncorrectly(answer).continueAssessment()
      return this
    },
    answerIncorrectly(answer: string) {
      this.answer(answer)
      return assumeWrongAnswerPage()
    },
  }
}

const endOfAssessment = () => {
  const findCertificateButton = () =>
    cy.findByRole('button', { name: 'View Certificate' })

  return {
    passAssessment() {
      cy.findByText('You have passed the assessment.').should('be.visible')
    },
    expectNotPassAssessment() {
      cy.findByText('You have not passed the assessment.').should('be.visible')
    },
    expectCertificate() {
      findCertificateButton().click()
      return CertificatePopup()
    },
    expectNoCertificate() {
      findCertificateButton().should('not.exist')
    },
    expectCertificateCannotBeObtained() {
      findCertificateButton().should('have.class', 'disabled')
    },
  }
}

export const assumeAssessmentPage = (notebook?: string) => {
  if (notebook) {
    cy.findByRole('heading', { name: `Assessment For ${notebook}` })
  }

  return {
    assumeQuestionSection,
    assumeWrongAnswerPage,
    answerQuestionsFromTable(answersTable: Record<string, string>[]) {
      Cypress._.times(answersTable.length, () => {
        this.assumeQuestionSection().answerFromTable(answersTable)
      })
      cy.pageIsNotLoading()
    },
    answerYesNoQuestionsToScore(correctAnswers: number, allQuestions: number) {
      for (let i = 0; i < correctAnswers; i++) {
        this.assumeQuestionSection().answer('Yes')
      }
      for (let i = correctAnswers; i < allQuestions; i++) {
        this.assumeQuestionSection().answerIncorrectAndContinue('No')
      }
    },
    expectEndOfAssessment(expectedScore?: string) {
      if (expectedScore) {
        cy.contains(expectedScore)
      }
      return endOfAssessment()
    },
  }
}
