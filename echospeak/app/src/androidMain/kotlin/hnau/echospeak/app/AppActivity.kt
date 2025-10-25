package hnau.echospeak.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import hnau.common.app.model.app.AppViewModel
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.echospeak.app.dialogs.ResourcesDialogsProvider
import hnau.echospeak.app.knowfactors.VariantsKnowFactorsRepositoryFactoryRoomImpl
import hnau.echospeak.app.permissions.ActivityPermissionRequester
import hnau.echospeak.app.permissions.WaitingPermissionRequester
import hnau.echospeak.app.recognizer.AndroidSpeechRecognizer
import hnau.echospeak.app.speaker.AndroidSpeaker
import hnau.echospeak.app.translator.AndroidTranslator
import hnau.echospeak.model.RootModel
import hnau.echospeak.model.impl
import hnau.echospeak.model.utils.EchoSpeakConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

class AppActivity : ComponentActivity() {

    private val permissionRequester = ActivityPermissionRequester(this)

    @Suppress("DEPRECATION")
    private val viewModel: AppViewModel<RootModel, RootModel.Skeleton> by viewModels {
        val context = applicationContext
        AppViewModel.factory(
            context = context,
            seed = createEchoSpeakAppSeed(
                rootModelDependencies = RootModel.Dependencies.impl(
                    variantsKnowFactorsRepositoryFactory = VariantsKnowFactorsRepositoryFactoryRoomImpl(
                        context = context,
                    ),
                    dialogsProvider = ResourcesDialogsProvider(context),
                    speakerFactory = AndroidSpeaker.Factory(context),
                    recognizerFactory = AndroidSpeechRecognizer.Factory(
                        applicationContext = context,
                        permissionRequester = WaitingPermissionRequester(
                            applicationContext = context,
                            intermittent = Companion.permissionRequester,
                        )
                    ),
                    config = EchoSpeakConfig(
                        locale = Locale("el", "GR")
                    ),
                    translatorFactory = AndroidTranslator.Factory(),
                )
            ),
        )
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.appModel.model.goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Companion.permissionRequester.value = permissionRequester
        enableEdgeToEdge()
        initOnBackPressedDispatcherCallback()
        val projector = createAppProjector(
            scope = lifecycleScope,
            model = viewModel.appModel,
        )
        setContent {
            projector.Content()
        }
    }

    override fun onDestroy() {
        Companion.permissionRequester.value = null
        super.onDestroy()
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onBackPressed() {
        if (useOnBackPressedDispatcher) {
            super.onBackPressed()
        }
        goBackHandler
            .value
            ?.invoke()
            ?: super.onBackPressed()
    }

    private fun initOnBackPressedDispatcherCallback() {
        if (!useOnBackPressedDispatcher) {
            return
        }
        val callback = object : OnBackPressedCallback(
            enabled = goBackHandler.value != null,
        ) {
            override fun handleOnBackPressed() {
                goBackHandler.value?.invoke()
            }
        }
        lifecycleScope.launch {
            goBackHandler
                .map { it != null }
                .distinctUntilChanged()
                .collect { goBackIsAvailable ->
                    callback.isEnabled = goBackIsAvailable
                }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {

        private val permissionRequester: MutableStateFlow<ActivityPermissionRequester?> =
            null.toMutableStateFlowAsInitial()

        private val useOnBackPressedDispatcher: Boolean = Build.VERSION.SDK_INT >= 33
    }
}