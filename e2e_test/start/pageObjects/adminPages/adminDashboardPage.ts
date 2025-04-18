import { adminFineTuningPage } from './adminFineTuningPage'

export function assumeAdminDashboardPage() {
  return {
    goToFailureReportList() {
      this.goToTabInAdminDashboard('Failure Reports')
      cy.findByText('Failure report list')
      return {
        shouldContain(content: string) {
          cy.get('body').should('contain', content)
        },
        checkFailureReportItem(index = 0) {
          cy.get('.failure-report')
            .eq(index)
            .find('input[type="checkbox"]')
            .check()
          return this
        },
        deleteSelected() {
          cy.findByRole('button', { name: 'Delete Selected' }).click()
          return this
        },
        shouldBeEmpty() {
          cy.get('.failure-report').should('not.exist')
          return this
        },
      }
    },

    goToTabInAdminDashboard(tabName: string) {
      cy.findByRole('button', { name: tabName }).click()
    },

    goToFineTuningData() {
      this.goToTabInAdminDashboard('Fine Tuning Data')
      return adminFineTuningPage()
    },

    goToModelManagement() {
      this.goToTabInAdminDashboard('Manage Models')
      return {
        chooseModel(model: string, task: string) {
          cy.findByLabelText(task).select(model)
          cy.findByRole('button', { name: 'Save' }).click()
        },
      }
    },

    goToBazaarManagement() {
      this.goToTabInAdminDashboard('Manage Bazaar')
      return {
        removeFromBazaar(notebook: string) {
          cy.findByText(notebook)
            .parentsUntil('tr')
            .parent()
            .findByRole('button', { name: 'Remove' })
            .click()
          cy.findByRole('button', { name: 'OK' }).click()
          cy.pageIsNotLoading()
        },
      }
    },

    goToAssistantManagement() {
      this.goToTabInAdminDashboard('Manage Assistant')
      return {
        recreate() {
          cy.findByRole('button', {
            name: 'Recreate Default Assistant',
          }).click()
          return {
            expectNewAssistant(newId: string, nameOfAssistant: string) {
              cy.findByLabelText(nameOfAssistant).should('have.value', newId)
            },
          }
        },
      }
    },
    goToCertificationRequestPage() {
      this.goToTabInAdminDashboard('Certification Requests')
      return {
        approve(notebook: string) {
          cy.findByText(notebook)
            .parentsUntil('tr')
            .parent()
            .findByRole('button', { name: 'Approve' })
            .click()
          cy.findByRole('button', { name: 'OK' }).click()
        },
        listContainsExactly(notebooks: string[]) {
          cy.get('tbody tr').should('have.length', notebooks.length)
          notebooks.forEach((notebook) => {
            cy.findByText(notebook).should('exist')
          })
        },
      }
    },
  }
}
