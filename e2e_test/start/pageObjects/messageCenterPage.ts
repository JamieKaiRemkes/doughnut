import { systemSidebar } from './systemSidebar'

export const assumeMessageCenterPage = () => {
  cy.findByText('Message Center').should('be.visible')

  return {
    expectConversation(subject: string, partner: string) {
      cy.findByText(subject).should('be.visible')
      cy.findByText(partner).should('be.visible')
      return this
    },
    expectMessageDisplayAtUserSide(message: string) {
      cy.findByText(message)
        .parents('.justify-content-end')
        .should('be.visible')
      return this
    },
    expectMessageDisplayAtOtherSide(message: string) {
      cy.findByText(message)
        .parent()
        .should('be.visible')
        .and('not.have.class', 'justify-content-end')
      return this
    },
    conversation(conversationSubject: string) {
      cy.findByText(conversationSubject).parent().should('be.visible').click()
      return {
        expectMessage(message: string) {
          cy.findByText(message).should('be.visible')
          return this
        },
        reply(message: string) {
          cy.get('textarea').type(message)
          cy.findByRole('button', { name: 'Send message' }).click()
          cy.findByText(message).should('be.visible')
          return this
        },
      }
    },
  }
}

export const navigateToMessageCenter = () => {
  return systemSidebar().userOptions().myMessageCenter()
}
