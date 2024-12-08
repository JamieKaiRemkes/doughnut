export function messageCenterIndicator() {
  const getMessageInSidebar = (
    fn: ($el: Cypress.Chainable<JQuery<HTMLElement>>) => void
  ) =>
    cy.get('.sidebar-control').within(() => fn(cy.get('li[title="Messages"]')))

  return {
    expectCount(numberOfNotes: number) {
      getMessageInSidebar(($el) => {
        $el.findByText(`${numberOfNotes}`, { selector: '.unread-count' })
      })
      return this
    },
    expectNoCount() {
      getMessageInSidebar(($el) => {
        $el.get('.unread-count').should('not.exist')
      })
    },
  }
}
