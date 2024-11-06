import LinkNoteFinalize from "@/components/links/LinkNoteFinalize.vue"
import makeMe from "@tests/fixtures/makeMe"
import helper from "@tests/helpers"
import { vi } from "vitest"

describe("LinkNoteFinalize", () => {
  it("going back", async () => {
    const note = makeMe.aNoteRealm.please()
    const wrapper = helper
      .component(LinkNoteFinalize)
      .withStorageProps({
        note,
        targetNoteTopic: note.note.noteTopic,
      })
      .mount()
    await wrapper.find(".go-back-button").trigger("click")
    expect(wrapper.emitted().goBack).toHaveLength(1)
  })

  it.skip("creates link and moves note when moveUnder is true", async () => {
    const note = makeMe.aNoteRealm.please()
    const storedApi = {
      linkNoteFinalize: vi.fn().mockResolvedValue(undefined),
      moveNote: vi.fn().mockResolvedValue(undefined),
    }
    helper.managedApi.restLinkController.linkNoteFinalize =
      storedApi.linkNoteFinalize
    helper.managedApi.restLinkController.moveNote = storedApi.moveNote
    const wrapper = helper
      .component(LinkNoteFinalize)
      .withStorageProps({
        note,
        targetNoteTopic: note.note.noteTopic,
      })
      .mount()

    await wrapper.find("input[type='checkbox']").setValue(true)
    await wrapper.find("button.btn-primary").trigger("click")

    expect(storedApi.linkNoteFinalize).toHaveBeenCalled()
    expect(storedApi.moveNote).toHaveBeenCalled()
  })
})
