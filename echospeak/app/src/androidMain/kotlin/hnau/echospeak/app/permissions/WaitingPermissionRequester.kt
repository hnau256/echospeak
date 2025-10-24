package hnau.echospeak.app.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import arrow.core.NonEmptySet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class WaitingPermissionRequester(
    private val applicationContext: Context,
    private val intermittent: StateFlow<PermissionRequester?>,
) : PermissionRequester {

    override suspend fun request(
        permissions: NonEmptySet<String>,
    ): Boolean {

        val alreadyGranted = permissions.all { permission ->
            val checkResult = ContextCompat.checkSelfPermission(applicationContext, permission)
            checkResult == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
            return true
        }

        return intermittent
            .filterNotNull()
            .first()
            .request(permissions)
    }
}