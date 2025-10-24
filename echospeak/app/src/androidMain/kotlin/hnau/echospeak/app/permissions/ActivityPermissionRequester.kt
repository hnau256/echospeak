package hnau.echospeak.app.permissions

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arrow.core.NonEmptySet
import arrow.core.identity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ActivityPermissionRequester(
    activity: ComponentActivity,
): PermissionRequester {
    private val resultChannel: Channel<Boolean> = Channel(Channel.CONFLATED)

    private val launcher: CompletableDeferred<ActivityResultLauncher<Array<String>>> =
        CompletableDeferred()

    init {
        activity.lifecycleScope.launch {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launcher.complete(
                    activity.registerForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        resultChannel.trySend(
                            result.values.all(::identity)
                        )
                    }
                )
            }
        }
    }

    private val requestPermissionsMutex = Mutex()

    override suspend fun request(
        permissions: NonEmptySet<String>,
    ): Boolean = requestPermissionsMutex.withLock {
        launcher.await().launch(permissions.toTypedArray())
        return resultChannel.receive()
    }
}