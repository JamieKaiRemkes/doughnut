import { routerToMyNotebooksPage } from './myNotebooksPage'
import { assumeAdminDashboardPage } from './adminPages/adminDashboardPage'
import { assumeAssessmentAndCertificateHistoryPage } from './assessmentAndCertificateHistoryPage'
import { messageCenterIndicator } from './messageCenterIndicator'

export const mainMenu = () => {
  routerToMyNotebooksPage()
  cy.pageIsNotLoading()

  return {
    adminDashboard() {
      return this.userOptions().adminDashboard()
    },
    userOptions() {
      cy.findByRole('button', { name: 'Account' }).click()
      return {
        adminDashboard() {
          cy.findByText('Admin Dashboard').click({ force: true })
          return assumeAdminDashboardPage()
        },
        userSettings(userName: string) {
          cy.findByRole('link', { name: `Settings for ${userName}` }).click({
            force: true,
          })
          return {
            changeName(name: string) {
              cy.formField('Name').assignFieldValue(name)
              cy.findByText('Submit').click()
            },
          }
        },
        logout() {
          cy.findByRole('link', { name: 'Logout' }).click({ force: true })
        },
        generateMcpToken() {
          cy.findByRole('link', { name: 'MCP Token' }).click()
          cy.findByRole('button', { name: 'Generate' }).click()
          cy.pageIsNotLoading()
          return cy.findByTestId('mcp-token').invoke('val')
        },
        myAssessmentAndCertificateHistory() {
          cy.findByRole('link', {
            name: 'My Assessments and Certificates',
          }).click({ force: true })
          return assumeAssessmentAndCertificateHistoryPage()
        },
      }
    },
    myMessageCenter() {
      return messageCenterIndicator().go()
    },
  }
}
