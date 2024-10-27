import LinkNoteFinalize from "@/components/links/LinkNoteFinalize.vue"
import makeMe from "@tests/fixtures/makeMe"
import helper from "@tests/helpers"

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
})
