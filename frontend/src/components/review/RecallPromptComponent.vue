<template>
  <div class="daisy-relative">
    <QuestionDisplay
      v-bind="{
        bareQuestion: recallPrompt.bareQuestion,
      }"
      @answer="submitAnswer($event)"
      :key="recallPrompt.id"
      :disabled="isLoading"
    />

    <div v-if="isLoading" class="daisy-absolute daisy-inset-0 daisy-bg-base-100/80 daisy-flex daisy-justify-center daisy-items-center daisy-z-50">
      <span class="daisy-loading daisy-loading-spinner daisy-loading-lg"></span>
    </div>

    <div v-if="error" class="daisy-alert daisy-alert-error daisy-mt-4">
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue"
import type { PropType } from "vue"
import type { AnswerDTO, RecallPrompt } from "@/generated/backend"
import useLoadingApi from "@/managedApi/useLoadingApi"
import usePopups from "../commons/Popups/usePopups"
import QuestionDisplay from "../review/QuestionDisplay.vue"

const { managedApi } = useLoadingApi()
const { popups } = usePopups()

const isLoading = ref(false)
const error = ref("")

const props = defineProps({
  recallPrompt: {
    type: Object as PropType<RecallPrompt>,
    required: true,
  },
})

const emits = defineEmits(["answered"])

const submitAnswer = async (answerData: AnswerDTO) => {
  isLoading.value = true
  error.value = ""

  try {
    const answerResult = await managedApi.restRecallPromptController.answerQuiz(
      props.recallPrompt.id,
      answerData
    )

    emits("answered", answerResult)
  } catch (e) {
    error.value = "Failed to submit answer. Please try again."
    await popups.alert(
      "This memory tracker doesn't exist any more or is being skipped now. Moving on to the next memory tracker..."
    )
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* All styles can be removed as they're replaced by Daisy UI classes */
</style>
