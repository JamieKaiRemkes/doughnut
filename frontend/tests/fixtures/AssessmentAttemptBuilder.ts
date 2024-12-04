import type {
  AssessmentAttempt,
  Notebook,
  RecallPrompt,
} from "@/generated/backend"
import Builder from "./Builder"
import generateId from "./generateId"

class AssessmentAttemptBuilder extends Builder<AssessmentAttempt> {
  private data: Partial<AssessmentAttempt> = {}

  forNotebook(notebook: Notebook) {
    this.data.notebookId = notebook.id
    this.data.notebookTitle = notebook.headNoteTopic.titleOrPredicate

    return this
  }

  withQuestions(questions: RecallPrompt[]) {
    this.data.assessmentQuestionInstances = questions.map((question) => ({
      id: question.id,
      bareQuestion: question.bareQuestion,
      recallPrompt: question,
    }))
    return this
  }
  passed(): AssessmentAttemptBuilder {
    this.data.isPass = true
    return this
  }

  do(): AssessmentAttempt {
    const id = generateId()
    return {
      notebookId: generateId(),
      id,
      submittedAt: "2021-09-01T00:00:00Z",
      notebookTitle: `Notebook ${id}`,
      ...this.data,
    }
  }
}

export default AssessmentAttemptBuilder
