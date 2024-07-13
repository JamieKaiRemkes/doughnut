import { screen } from "@testing-library/vue"
import { beforeEach, describe, expect, it, vi, vitest } from "vitest"
import AssessmentHistoryDialog from "@/components/user/AssessmentHistoryDialog.vue"
import helper from "../helpers"

vitest.mock("vue-router", () => ({
  useRouter: () => ({
    currentRoute: {
      value: {},
    },
  }),
}))

describe("assessment history", () => {
  beforeEach(() => {
    const teleportTarget = document.createElement("div")
    teleportTarget.id = "head-status"
    document.body.appendChild(teleportTarget)
    helper.managedApi.restAssessmentController.getAssessmentHistory = vi
      .fn()
      .mockResolvedValue([])
  })

  it("calls API ONCE on mount", async () => {
    helper.component(AssessmentHistoryDialog).render()
    expect(
      helper.managedApi.restAssessmentController.getAssessmentHistory
    ).toBeCalledTimes(1)
  })

  it("indicate the list is empty", async () => {
    helper.component(AssessmentHistoryDialog).render()
    await screen.findByText("No assessment has been done yet")
  })
})
