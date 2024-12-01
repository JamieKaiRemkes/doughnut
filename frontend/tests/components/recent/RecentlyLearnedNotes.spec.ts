import RecentlyLearnedNotes from "@/components/recent/RecentlyLearnedNotes.vue"
import { flushPromises } from "@vue/test-utils"
import helper from "@tests/helpers"
import makeMe from "@tests/fixtures/makeMe"

describe("RecentlyLearnedNotes", () => {
  const mockMemoryTrackers = [
    makeMe.aMemoryTracker
      .onboardedAt("2024-01-01T00:00:00Z")
      .removedFromTracking(false)
      .please(),
    makeMe.aMemoryTracker
      .onboardedAt("2024-01-02T00:00:00Z")
      .removedFromTracking(true)
      .please(),
  ]

  beforeEach(() => {
    helper.managedApi.restMemoryTrackerController.getRecentMemoryTrackers =
      vitest.fn().mockResolvedValue(mockMemoryTrackers)
  })

  it("fetches and displays recent memory trackers", async () => {
    const wrapper = helper.component(RecentlyLearnedNotes).withRouter().mount()

    await flushPromises()

    // Verify API was called
    expect(
      helper.managedApi.restMemoryTrackerController.getRecentMemoryTrackers
    ).toBeCalled()

    // Verify memory trackers are displayed
    const rows = wrapper.findAll("tbody tr")
    expect(rows).toHaveLength(2)

    // Verify removed memory tracker has correct styling
    const removedRow = rows[1]
    expect(removedRow?.classes()).toContain("removed")
  })
})
