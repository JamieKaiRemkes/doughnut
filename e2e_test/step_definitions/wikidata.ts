/// <reference types="cypress" />
/// <reference types="../support" />
// @ts-check

import {
  DataTable,
  Given,
  Then,
  When,
} from '@badeball/cypress-cucumber-preprocessor'
import start, { mock_services } from '../start'

When(
  'I associate the note {string} with wikidata id {string}',
  (title: string, wikiID: string) => {
    start.jumpToNotePage(title).wikidataOptions().associate(wikiID)
  }
)

When(
  'I change the note {string} to associate with wikidata id {string}',
  (title: string, wikiID: string) => {
    start.jumpToNotePage(title).wikidataOptions().reassociationWith(wikiID)
  }
)

When(
  'I need to confirm the association with different label {string}',
  (wikidataTitle: string) => {
    cy.findAllByText(wikidataTitle).should('exist')
    cy.findByRole('button', { name: 'Confirm' }).click()
    start.assumeNotePage().wikidataOptions().hasAssociation()
  }
)

Then(
  "I don't need to confirm the association with different label {string}",
  () => {
    start.assumeNotePage().wikidataOptions().hasAssociation()
  }
)

Given(
  'Wikidata.org has an entity {string} with label {string} and link to wikipedia {string}',
  (wikidataId: string, wikidataTitle: string, wikipediaLink: string) => {
    mock_services
      .wikidata()
      .stubWikidataEntityQuery(wikidataId, wikidataTitle, wikipediaLink)
  }
)

Given(
  'Wikidata.org has an entity {string} with label {string}',
  (wikidataId: string, wikidataTitle: string) => {
    mock_services
      .wikidata()
      .stubWikidataEntityQuery(wikidataId, wikidataTitle, undefined)
  }
)

Given(
  'Wikidata.org entity {string} is a person from {string} and birthday is {string}',
  (wikidataId: string, countryId: string, birthday: string) => {
    mock_services
      .wikidata()
      .stubWikidataEntityPerson(wikidataId, countryId, birthday)
  }
)

Given(
  'Wikidata.org entity {string} is a location at {float}, {float}',
  (wikidataId: string, lat: number, lng: number) => {
    mock_services.wikidata().stubWikidataEntityLocation(wikidataId, lat, lng)
  }
)

Given('The wikidata service is not available', () => {
  // The service should be mocked, but no stubbing is done
})

Then(
  'I should see an error {string} on {string}',
  (message: string, field: string) => {
    cy.expectFieldErrorMessage(field, message)
  }
)

Then(
  'the Wiki association of note {string} should link to {string}',
  (ttile: string, associationUrl: string) => {
    start
      .assumeNotePage(ttile)
      .wikidataOptions()
      .hasAssociation()
      .expectALinkThatOpensANewWindowWithURL(associationUrl)
  }
)

Given(
  'Wikidata search result always has {string} with ID {string}',
  (wikidataLabel: string, wikidataId: string) => {
    mock_services.wikidata().stubWikidataSearchResult(wikidataLabel, wikidataId)
  }
)

When('I search with phrase {string} on Wikidata', (phrase: string) => {
  cy.focused().clear().type(phrase)
  cy.findByRole('button', { name: 'Wikidata Id' }).click()
})

When(
  'I select wikidataID {string} from the Wikidata search result',
  (wikidataID: string) => {
    cy.get('select[name="wikidataSearchResult"]').select(wikidataID)
  }
)

Then(
  'I should see that the {string} becomes {string}',
  (field: string, value: string) => {
    cy.formField(field).fieldShouldHaveValue(value)
  }
)

Then(
  'a map pointing to lat: {string}, lon: {string} is added to the note',
  (latitude: string, longitude: string) => {
    cy.expectAMapTo(latitude, longitude)
  }
)

Given(
  'the Wikidata.org entity {string} is written by authors with ID',
  (wikidataId: string, data: DataTable) => {
    mock_services.wikidata().stubWikidataEntityBook(
      wikidataId,
      data.hashes().map((hash) => hash['Wikidata Id'] ?? '')
    )
  }
)
