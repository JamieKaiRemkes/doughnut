import { commonSenseSplit } from 'support/string_util'

export const assimilation = () => {
  const getAssimilateListItemInSidebar = (
    fn: ($el: Cypress.Chainable<JQuery<HTMLElement>>) => void
  ) => cy.get('.main-menu').within(() => fn(cy.get('li[title="Assimilate"]')))

  return {
    expectCount(numberOfNotes: number) {
      getAssimilateListItemInSidebar(($el) => {
        $el.findByText(`${numberOfNotes}`, { selector: '.due-count' })
      })
      return this
    },
    goToAssimilationPage() {
      cy.routerToRoot()
      getAssimilateListItemInSidebar(($el) => {
        $el.click()
      })
      return {
        expectToAssimilateAndTotal(toAssimilateAndTotal: string) {
          const [assimlatedTodayCount, toAssimilateCountForToday, totalCount] =
            toAssimilateAndTotal.split('/')

          cy.get('.daisy-progress-bar').should(
            'contain',
            `Assimilating: ${assimlatedTodayCount}/${toAssimilateCountForToday}`
          )
          // Click progress bar to show tooltip
          cy.get('.daisy-progress-bar').first().click()

          // Check tooltip content
          cy.get('.tooltip-content').within(() => {
            cy.contains(
              `Daily Progress: ${assimlatedTodayCount} / ${toAssimilateCountForToday}`
            )
            cy.contains(
              `Total Progress: ${assimlatedTodayCount} / ${totalCount}`
            )
          })

          // Close tooltip
          cy.get('.tooltip-popup').click()
        },
        assimilate(assimilations: Record<string, string>[]) {
          assimilations.forEach((assimilation) => {
            cy.initialReviewOneNoteIfThereIs(assimilation)
          })
        },
        assimilateNotes(noteTitles: string) {
          return this.assimilate(
            commonSenseSplit(noteTitles, ', ').map((title: string) => {
              return {
                'Review Type': title === 'end' ? 'initial done' : 'single note',
                Title: title,
              }
            })
          )
        },
      }
    },
  }
}
