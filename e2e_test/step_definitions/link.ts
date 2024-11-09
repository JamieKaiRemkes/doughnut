/// <reference types="cypress" />
/// <reference types="@testing-library/cypress" />
/// <reference types="../support" />
// @ts-check

import { Then, When } from '@badeball/cypress-cucumber-preprocessor'
import NotePath from '../support/NotePath'
import start from '../start'
import { commonSenseSplit } from '../support/string_util'

When('I start searching', () => {
  cy.startSearching()
})

When(
  'I am creating a linking note under note {string}',
  (noteTopic: string) => {
    start.jumpToNotePage(noteTopic).startSearchingAndLinkNote()
  }
)

When(
  'I link note {string} as {string} note {string}',
  (fromNoteTopic: string, linkType: string, toNoteTopic: string) => {
    start
      .jumpToNotePage(fromNoteTopic)
      .startSearchingAndLinkNote()
      .findTarget(toNoteTopic)
      .linkToTargetAs(toNoteTopic, linkType)
  }
)

When(
  'I link top level note {string} as {string} note {string}',
  (fromNoteTopic: string, linkType: string, toNoteTopic: string) => {
    start
      .jumpToNotePage(fromNoteTopic)
      .startSearchingAndLinkNote()
      .findTarget(toNoteTopic)
      .linkTopLevelNoteToTargetAs(toNoteTopic, linkType)
  }
)

When(
  'I move note {string} to be under note {string}',
  (fromNoteTopic: string, toNoteTopic: string) => {
    start
      .jumpToNotePage(fromNoteTopic)
      .startSearchingAndLinkNote()
      .findTarget(toNoteTopic)
      .moveUnder()
  }
)

When(
  'there is {string} link between note {string} and {string}',
  (linkType: string, fromNoteTopic: string, toNoteTopic: string) => {
    start.testability().injectLink(linkType, fromNoteTopic, toNoteTopic)
  }
)

When(
  'I should see {string} as the possible duplicate',
  (noteTopicsAsString: string) => {
    cy.tick(500)
    start
      .assumeNoteTargetSearchDialog()
      .expectExactDropdownTargets(
        commonSenseSplit(noteTopicsAsString, ',').map((i: string) => i.trim())
      )
  }
)

When(
  'I should see {string} as targets only when searching {string}',
  (noteTopicsAsString: string, searchKey: string) => {
    start
      .assumeNoteTargetSearchDialog()
      .findTargetWithinNotebook(searchKey)
      .expectExactLinkTargets(
        commonSenseSplit(noteTopicsAsString, ',').map((i: string) => i.trim())
      )
  }
)

When(
  'I should see {string} as targets only when searching in all my notebooks {string}',
  (noteTopicsAsString: string, searchKey: string) => {
    start
      .assumeNoteTargetSearchDialog()
      .findTarget(searchKey)
      .expectExactLinkTargets(
        commonSenseSplit(noteTopicsAsString, ',').map((i: string) => i.trim())
      )
  }
)

When(
  'I should see note cannot be found when searching in all my notebooks {string}',
  (searchKey: string) => {
    start.assumeNoteTargetSearchDialog().findTarget(searchKey)
    cy.findByText('No matching notes found.').should('be.visible')
  }
)

Then(
  'I should see {string} has link {string} {string}',
  (noteTopic: string, linkType: string, targetNoteTopics: string) => {
    start
      .jumpToNotePage(noteTopic)
      .expectLinkingChildren(linkType, targetNoteTopics)
  }
)

Then(
  'I should see note {notepath} has link {string} {string}',
  (notePath: NotePath, linkType: string, targetNoteTopics: string) => {
    start
      .routerToNotebooksPage()
      .navigateToPath(notePath)
      .expectLinkingChildren(linkType, targetNoteTopics)
  }
)

Then(
  'I should see {string} has no link to {string}',
  (noteTopic: string, targetTitle: string) => {
    start.jumpToNotePage(noteTopic)
    cy.findByText(targetTitle, { selector: 'main *' }).should('not.exist')
  }
)

Then(
  'I change the link from {string} to {string} to {string}',
  (noteTopic: string, targetTitle: string, linkType: string) => {
    start
      .jumpToNotePage(noteTopic)
      .navigateToLinkingChild(targetTitle)
      .changeLinkType(linkType, targetTitle)
  }
)

Then(
  'I change the reference from {string} to {string} to {string}',
  (noteTopic: string, referenceTitle: string, linkType: string) => {
    start
      .jumpToNotePage(noteTopic)
      .navigateToReference(referenceTitle)
      .changeLinkType(linkType, noteTopic)
  }
)

Then('I should be able to delete the link', () => {
  cy.findByRole('button', { name: 'Delete' }).click()
})

Then(
  'I delete the link from {string} to {string}',
  (noteTopic: string, targetTitle: string) => {
    start
      .jumpToNotePage(noteTopic)
      .navigateToLinkingChild(targetTitle)
      .deleteNote()
    start.assumeNotePage(noteTopic) // remain on the same note page
  }
)
