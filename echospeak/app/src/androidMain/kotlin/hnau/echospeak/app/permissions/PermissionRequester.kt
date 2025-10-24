package hnau.echospeak.app.permissions

import arrow.core.NonEmptySet

interface PermissionRequester {

    suspend fun request(
        permissions: NonEmptySet<String>,
    ): Boolean
}