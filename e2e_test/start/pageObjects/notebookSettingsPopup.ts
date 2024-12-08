const notebookSettingsPopup = () => {
  const clickButton = (name: string) =>
    cy.findByRole('button', { name }).click()
  const assertButtonExists = (name: string) =>
    cy.findByRole('button', { name }).should('exist')
  const assertButtonNotExists = (name: string) =>
    cy.findByRole('button', { name }).should('not.exist')

  return {
    assertNoteHasSettingWithValue(setting: string, value: string) {
      cy.formField(setting).fieldShouldHaveValue(value)
    },

    skipMemoryTracking() {
      cy.formField('Skip Memory Tracking Entirely').check()
      clickButton('Update')
    },
    requestForNotebookApproval() {
      clickButton('Send Request')
    },
    expectNotebookApprovalCanBeRequested() {
      assertButtonExists('Send Request')
    },
    expectNotebookApprovalStatus(status: string) {
      assertButtonNotExists('Send Request')
      cy.findByText(`Approval ${status}`).should('exist')
    },
    updateAssessmentSettings(settings: {
      numberOfQuestion?: number
      certificateExpiry?: string
    }) {
      if (settings.numberOfQuestion !== undefined) {
        cy.formField('Number Of Questions In Assessment').assignFieldValue(
          `${settings.numberOfQuestion}`
        )
      }
      if (settings.certificateExpiry) {
        cy.formField('Certificate Expiry').assignFieldValue(
          `${settings.certificateExpiry}`
        )
      }

      clickButton('Update')
      cy.pageIsNotLoading()
    },
    createCustomizedAssistant() {
      cy.formField('Additional Instruction').type('Please use simple English.')
      cy.findByRole('button', {
        name: 'Create Assistant For Notebook',
      }).click()
      cy.pageIsNotLoading()
    },
    updateAiAssistantInstructions(instruction: string) {
      cy.formField('Additional Instruction').type(instruction)
      clickButton('Update Notebook AI Assistant Settings')
      cy.pageIsNotLoading()
    },
  }
}

export default notebookSettingsPopup
