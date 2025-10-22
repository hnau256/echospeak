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
import hnau.echospeak.app.dialogs.ResourcesDialogsProvider
import hnau.echospeak.app.knowfactors.VariantsKnowFactorsRepositoryFactoryRoomImpl
import hnau.echospeak.model.RootModel
import hnau.echospeak.model.impl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppActivity : ComponentActivity() {

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
                )
            ),
        )
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.appModel.model.goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        private val useOnBackPressedDispatcher: Boolean = Build.VERSION.SDK_INT >= 33
    }
}