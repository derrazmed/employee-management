<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },

  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['close', 'submit'])

const name = ref('')
const email = ref('')
const password = ref('')
const userType = ref('NORMAL_USER')

const isEditing = ref(false)

watch(
  () => props.user,
  (user) => {
    if (user) {
      isEditing.value = true

      name.value = user.name
      email.value = user.email
      userType.value = user.userType

      password.value = ''
    } else {
      isEditing.value = false

      name.value = ''
      email.value = ''
      password.value = ''
      userType.value = 'NORMAL_USER'
    }
  },
  { immediate: true },
)

const submit = () => {
  const data = {
    name: name.value,
    email: email.value,
    userType: userType.value,
  }

  if (!isEditing.value) {
    data.password = password.value
  }

  emit('submit', data)
}
</script>

<template>
  <div v-if="show" class="modal-backdrop-custom">
    <div class="modal-dialog-custom">
      <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0">
            {{ isEditing ? 'Update User' : 'Create User' }}
          </h5>

          <button type="button" class="btn-close" @click="emit('close')"></button>
        </div>

        <div class="card-body">
          <form @submit.prevent="submit">
            <!-- Name -->
            <div class="mb-3">
              <label class="form-label"> Name </label>

              <input v-model="name" type="text" class="form-control" required />
            </div>

            <!-- Email -->
            <div class="mb-3">
              <label class="form-label"> Email </label>

              <input v-model="email" type="email" class="form-control" required />
            </div>

            <!-- Password -->
            <div v-if="!isEditing" class="mb-3">
              <label class="form-label"> Password </label>

              <input v-model="password" type="password" class="form-control" required />
            </div>

            <!-- User Type -->
            <div class="mb-3">
              <label class="form-label"> User Type </label>

              <select v-model="userType" class="form-select">
                <option value="NORMAL_USER">Normal User</option>

                <option value="SUPER_ADMIN">Super Administrator</option>
              </select>
            </div>

            <div class="d-flex justify-content-end gap-2">
              <button type="button" class="btn btn-secondary" @click="emit('close')">Cancel</button>

              <button type="submit" class="btn btn-primary">
                {{ isEditing ? 'Update' : 'Create' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
