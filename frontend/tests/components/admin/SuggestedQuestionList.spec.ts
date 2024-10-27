import SuggestedQuestionList from "@/components/admin/SuggestedQuestionList.vue"
import { flushPromises } from "@vue/test-utils"
import { describe, expect, it } from "vitest"
import usePopups from "@/components/commons/Popups/usePopups"
import makeMe from "@tests/fixtures/makeMe"
import helper, { matchByText } from "@tests/helpers"

describe("Edit Suggested Question", () => {
  describe("suggest question for fine tuning AI", () => {
    it("lists the suggestions", async () => {
      const suggestedQuestion = makeMe.aSuggestedQuestionForFineTuning.please()
      const wrapper = helper
        .component(SuggestedQuestionList)
        .withProps({ suggestedQuestions: [suggestedQuestion] })
        .mount()
      expect(wrapper.findAll("tr").length).toEqual(2)
      expect(matchByText(wrapper, /Duplicate/, "button")).not.toBeUndefined()
    })

    describe("with a positive feedback", () => {
      const suggestedQuestion = makeMe.aSuggestedQuestionForFineTuning
        .positive()
        .please()

      it("cannot duplicate good suggestion", async () => {
        const wrapper = helper
          .component(SuggestedQuestionList)
          .withProps({ suggestedQuestions: [suggestedQuestion] })
          .mount()
        expect(matchByText(wrapper, /Duplicate/, "button")).toBeUndefined()
      })

      it("can download chat gpt conversation starter", async () => {
        const wrapper = helper
          .component(SuggestedQuestionList)
          .withProps({ suggestedQuestions: [suggestedQuestion] })
          .mount()
        matchByText(wrapper, /Chat/, "button")!.trigger("click")
        const alertMsg = usePopups().popups.peek()[0]!.message
        expect(alertMsg).toContain(
          suggestedQuestion.preservedQuestion.multipleChoicesQuestion.stem
        )
        expect(alertMsg).toContain(suggestedQuestion.preservedNoteContent)
      })

      it("can delete", async () => {
        const wrapper = helper
          .component(SuggestedQuestionList)
          .withProps({ suggestedQuestions: [suggestedQuestion] })
          .mount()
        helper.managedApi.restFineTuningDataController.delete = vi.fn()
        matchByText(wrapper, /Del/, "button")!.trigger("click")
        usePopups().popups.done(true)
        await flushPromises()
        expect(
          helper.managedApi.restFineTuningDataController.delete
        ).toHaveBeenCalledWith(suggestedQuestion.id)
      })
    })
  })
})
