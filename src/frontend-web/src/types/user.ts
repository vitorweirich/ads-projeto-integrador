export type User = {
  id: number
  name: string
  email: string
  mfaEnabled: boolean
}

export type AdminUserProjection = {
  totalSize: number
  settings: {
    storageLimitBytes: number
    maxFileRetentionDays: number
    modifiedAt: string
  }
  usedQuota: string
} & User

export type ListUsersAdminResponse = {
  content: AdminUserProjection[]

  totalElements: number
  totalPages: number
  size: number
  number: number
}
