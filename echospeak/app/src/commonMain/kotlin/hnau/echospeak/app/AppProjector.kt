package hnau.echospeak.app

import hnau.common.app.model.app.AppModel
import hnau.common.app.projector.app.AppProjector
import hnau.echospeak.model.RootModel
import hnau.echospeak.projector.RootProjector
import hnau.echospeak.projector.impl
import kotlinx.coroutines.CoroutineScope

fun createAppProjector(
    scope: CoroutineScope,
    model: AppModel<RootModel, RootModel.Skeleton>,
): AppProjector<RootModel, RootModel.Skeleton, RootProjector> = AppProjector(
    scope = scope,
    model = model,
    createProjector = { scope, model ->
        RootProjector(
            scope = scope,
            model = model,
            dependencies = RootProjector.Dependencies.impl(),
        )
    },
    content = { rootProjector ->
        rootProjector.Content()
    }
)