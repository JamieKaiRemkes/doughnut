import Quiz from "@/components/review/Quiz.vue"
import { flushPromises } from "@vue/test-utils"
import { beforeEach, describe, it, vi } from "vitest"
import makeMe from "../fixtures/makeMe"
import helper from "../helpers"

describe("repeat page", () => {
  const reviewQuestionInstance = makeMe.aReviewQuestionInstance.please()
  const mockedRandomQuestionCall = vi
    .fn()
    .mockResolvedValue(reviewQuestionInstance)

  beforeEach(() => {
    vi.resetAllMocks()
    vi.useFakeTimers()
    helper.managedApi.restNoteController.show1 = vi
      .fn()
      .mockResolvedValue(makeMe.aNote.please())
    helper.managedApi.restReviewPointController.show = vi
      .fn()
      .mockResolvedValue(makeMe.aReviewPoint.please())
    helper.managedApi.silent.restReviewPointController.generateRandomQuestion =
      mockedRandomQuestionCall
  })

  const mountPage = async (reviewPoints: number[], eagerFetchCount: number) => {
    const wrapper = helper
      .component(Quiz)
      .withStorageProps({
        reviewPoints,
        currentIndex: 0,
        eagerFetchCount,
      })
      .mount()
    await flushPromises()
    return wrapper
  }

  describe('repeat page with "just review" quiz', () => {
    it("fetch the first 1 question when mount", async () => {
      await mountPage([1, 2, 3], 1)
      expect(mockedRandomQuestionCall).toHaveBeenCalledWith(1)
    })

    it("fetch the first 3 question when mount", async () => {
      await mountPage([111, 222, 333, 444], 3)
      expect(mockedRandomQuestionCall).nthCalledWith(1, 111)
      expect(mockedRandomQuestionCall).nthCalledWith(2, 222)
      expect(mockedRandomQuestionCall).nthCalledWith(3, 333)
    })

    it("does not fetch question 2 again after prefetched", async () => {
      const wrapper = await mountPage([1, 2, 3, 4], 2)
      expect(mockedRandomQuestionCall).toBeCalledTimes(2)
      await wrapper.setProps({ currentIndex: 1 })
      expect(mockedRandomQuestionCall).toHaveBeenCalledWith(3)
    })
  })
})
