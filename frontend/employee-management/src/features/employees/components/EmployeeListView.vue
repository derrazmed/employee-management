<template>
  <div class="employee-list">
    <div class="desktop-employee-table">
      <div class="table-responsive">
        <table class="directory-table table table-hover align-middle employee-data-table">
          <colgroup>
            <col class="col-employee" />
            <col class="col-role" />
            <col class="col-department" />
            <col class="col-hire" />
            <col class="col-actions" />
          </colgroup>

          <thead>
            <tr>
              <th class="ps-3">Employee</th>
              <th>Role</th>
              <th>Department</th>
              <th>Hire date</th>
              <th class="text-end pe-3">Actions</th>
            </tr>
          </thead>

          <tbody v-if="loading">
            <tr v-for="n in 6" :key="n" aria-hidden="true">
              <td class="ps-3">
                <span class="skeleton sk-avatar"></span>
              </td>
              <td>
                <span class="skeleton sk-line sk-line-lg"></span>
                <span class="skeleton sk-line sk-line-sm sk-mt"></span>
              </td>
              <td>
                <span class="skeleton sk-line sk-line-md"></span>
              </td>
              <td>
                <span class="skeleton sk-line sk-line-md"></span>
              </td>
              <td>
                <span class="skeleton sk-line sk-line-sm"></span>
              </td>
              <td class="text-end pe-3">
                <span class="skeleton sk-dot"></span>
              </td>
            </tr>
          </tbody>

          <tbody v-else>
            <tr
              v-for="employee in employees"
              :key="employee.id"
              :class="{ 'menu-open': openMenuId === employee.id }"
            >
              <td class="ps-3">
                <div class="employee-identity">
                  <EmployeeAvatar :employee="employee" size="md" />

                  <div class="employee-identity-text">
                    <span class="employee-identity-name">{{ fullName(employee) }}</span>
                    <span class="employee-identity-email" :title="employee.email">
                      {{ employee.email || 'No email address' }}
                    </span>
                  </div>
                </div>
              </td>

              <td>
                <span class="employee-role">{{ employee.jobTitle || '—' }}</span>
              </td>

              <td>
                <span class="employee-department" :title="employee.department">
                  {{ employee.department || '—' }}
                </span>
              </td>

              <td>
                <span class="employee-hire-date">{{ employee.hireDate || '—' }}</span>
              </td>

              <td class="text-end pe-3">
                <button
                  type="button"
                  class="employee-menu-trigger"
                  aria-haspopup="menu"
                  :aria-expanded="openMenuId === employee.id"
                  :aria-label="`Actions for ${fullName(employee)}`"
                  title="Employee actions"
                  @click="emit('toggle-menu', employee, $event)"
                >
                  <i class="bi bi-three-dots-vertical"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="mobile-employee-list" role="list" aria-label="Employees">
      <template v-if="loading">
        <div v-for="n in 4" :key="n" class="mobile-employee-row" aria-hidden="true">
          <div class="mobile-employee-row-main">
            <span class="skeleton sk-avatar sk-avatar-mobile"></span>

            <div class="mobile-employee-info">
              <span class="skeleton sk-line sk-line-lg"></span>
              <span class="skeleton sk-line sk-line-md sk-mt"></span>
              <span class="skeleton sk-line sk-line-sm sk-mt"></span>
            </div>

            <span class="skeleton sk-dot"></span>
          </div>

          <span class="skeleton sk-line sk-line-md sk-ml"></span>
        </div>
      </template>

      <template v-else>
        <article
          v-for="employee in employees"
          :key="employee.id"
          class="mobile-employee-row"
          :class="{ 'menu-open': openMenuId === employee.id }"
          role="listitem"
          @click="openDetails(employee)"
        >
          <div class="mobile-employee-row-main">
            <EmployeeAvatar :employee="employee" size="sm" />

            <div class="mobile-employee-info">
              <div class="mobile-employee-name">{{ fullName(employee) }}</div>
              <div v-if="employee.jobTitle" class="mobile-employee-role">
                {{ employee.jobTitle }}
              </div>
              <div v-if="employee.department" class="mobile-employee-department">
                {{ employee.department }}
              </div>
            </div>

            <button
              type="button"
              class="employee-menu-trigger"
              aria-haspopup="menu"
              :aria-expanded="openMenuId === employee.id"
              :aria-label="`Actions for ${fullName(employee)}`"
              title="Employee actions"
              @click="emit('toggle-menu', employee, $event)"
            >
              <i class="bi bi-three-dots-vertical"></i>
            </button>
          </div>

          <div class="mobile-employee-email">{{ employee.email || 'No email address' }}</div>
        </article>
      </template>
    </div>
  </div>
</template>

<script setup>
import EmployeeAvatar from './EmployeeAvatar.vue'

const props = defineProps({
  employees: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  openMenuId: { type: [Number, String], default: null },
})

const emit = defineEmits(['view', 'toggle-menu', 'close-menu'])

const fullName = (employee) =>
  `${employee.firstName || ''} ${employee.lastName || ''}`.trim() || 'Employee'

const openDetails = (employee) => {
  if (props.openMenuId !== null) {
    emit('close-menu')
    return
  }

  emit('view', employee)
}
</script>

<style scoped>
.employee-list {
  min-width: 0;
}

.mobile-employee-list {
  display: none;
}

.desktop-employee-table .employee-data-table {
  table-layout: fixed;
}

.employee-data-table .col-employee {
  width: 34%;
}

.employee-data-table .col-role {
  width: 20%;
}

.employee-data-table .col-department {
  width: 20%;
}

.employee-data-table .col-hire {
  width: 14%;
}

.employee-data-table .col-actions {
  width: 12%;
}

.employee-identity {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.employee-identity-text {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.employee-identity-name {
  overflow: hidden;
  color: var(--color-text);
  font-size: 0.87rem;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-identity-email {
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 0.76rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-role {
  display: block;
  overflow: hidden;
  color: var(--color-text-body);
  font-size: 0.84rem;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-department {
  display: block;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.employee-hire-date {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  white-space: nowrap;
}

.desktop-employee-table tr.menu-open {
  background: var(--color-primary-soft);
}

.skeleton.sk-avatar {
  display: block;
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.skeleton.sk-line {
  display: block;
  height: 0.7rem;
  border-radius: 4px;
}

.skeleton.sk-line-sm {
  width: 52%;
}

.skeleton.sk-line-md {
  width: 72%;
}

.skeleton.sk-line-lg {
  width: 86%;
}

.skeleton.sk-mt {
  margin-top: 7px;
}

.skeleton.sk-ml {
  margin-left: 56px;
}

.skeleton.sk-dot {
  display: inline-block;
  width: 30px;
  height: 30px;
  border-radius: 6px;
}

@media (min-width: 768px) {
  .desktop-employee-table .employee-data-table {
    min-width: 840px;
  }

  .desktop-employee-table .directory-table tbody tr {
    transition: background 0.15s ease;
  }

  .desktop-employee-table .directory-table td:first-child {
    padding-left: 22px;
  }

  .desktop-employee-table .directory-table td:last-child {
    padding-right: 22px;
  }
}

@media (max-width: 767.98px) {
  .desktop-employee-table {
    display: none;
  }

  .mobile-employee-list {
    display: block;
    width: 100%;
  }

  .mobile-employee-row {
    display: flex;
    flex-direction: column;
    gap: 0.7rem;
    padding: 16px;
    border-bottom: 1px solid var(--color-border);
    cursor: pointer;
    transition: background 0.15s ease;
  }

  .mobile-employee-row:last-child {
    border-bottom: 0;
  }

  .mobile-employee-row:hover {
    background: var(--color-hover);
  }

  .mobile-employee-row.menu-open {
    background: var(--color-primary-soft);
  }

  .mobile-employee-row-main {
    display: flex;
    min-width: 0;
    align-items: flex-start;
    gap: 12px;
  }

  .mobile-employee-info {
    flex: 1 1 auto;
    min-width: 0;
    padding-top: 1px;
  }

  .mobile-employee-name {
    overflow-wrap: anywhere;
    color: var(--color-text);
    font-size: 0.95rem;
    font-weight: 700;
    line-height: 1.3;
  }

  .mobile-employee-role {
    margin-top: 3px;
    overflow-wrap: anywhere;
    color: var(--color-text-body);
    font-size: 0.81rem;
    font-weight: 500;
    line-height: 1.35;
  }

  .mobile-employee-department {
    margin-top: 2px;
    overflow-wrap: anywhere;
    color: var(--color-text-secondary);
    font-size: 0.79rem;
    font-weight: 600;
    line-height: 1.35;
  }

  .mobile-employee-email {
    margin-left: 56px;
    overflow-wrap: anywhere;
    color: var(--color-text-secondary);
    font-size: 0.79rem;
    line-height: 1.4;
    word-break: break-word;
  }

  .skeleton.sk-avatar-mobile {
    width: 44px;
    height: 44px;
    flex: 0 0 44px;
  }
}
</style>
