<template>
  <div class="daisy-container">
    <h2>MCP Token Dialog</h2>
    <div class="daisy-form-control">
      <input
        type="text"
        data-testid="mcp-token"
        :value="token"
        readonly
        class="daisy-input daisy-input-bordered"
      />
      <div class="daisy-flex daisy-gap-2 daisy-mt-4">
        <button
          class="daisy-btn daisy-btn-primary"
          @click="generateToken"
          data-testid="generate"
        >
          Generate
        </button>
        <button
          class="daisy-btn daisy-btn-error"
          @click="deleteToken"
          data-testid="delete"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import useLoadingApi from "@/managedApi/useLoadingApi"
import { inject, type Ref } from "vue"
import type { User } from "@/generated/backend"

const { managedApi } = useLoadingApi()
const user = inject<Ref<User | undefined>>("currentUser")
if (!user?.value?.id) {
  throw new Error("User must be logged in")
}
const userId = user.value.id

const token = ref("")

const fetchTokens = async () => {
  const tokens = await managedApi.restUserController.getUserTokens(userId)
  if (tokens && tokens.length > 0) {
    const firstToken = tokens[0]
    if (firstToken) {
      token.value = firstToken.token
    }
  }
}

const generateToken = async () => {
  const response = await managedApi.restUserController.createUserToken(userId)
  token.value = response.token
}

const deleteToken = async () => {
  await managedApi.restUserController.deleteUserToken(userId)
  token.value = ""
}

onMounted(fetchTokens)
</script>
