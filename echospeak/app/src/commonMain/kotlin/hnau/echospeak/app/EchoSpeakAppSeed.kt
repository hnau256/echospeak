package hnau.echospeak.app

import hnau.common.app.model.app.AppSeed
import hnau.common.app.model.theme.Hue
import hnau.common.app.model.theme.ThemeBrightness
import hnau.echospeak.model.RootModel

fun createEchoSpeakAppSeed(
    defaultBrightness: ThemeBrightness? = null,
    rootModelDependencies: RootModel.Dependencies,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    fallbackHue = Hue(240.0),
    defaultBrightness = defaultBrightness,
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, appContext, skeleton ->
        RootModel(
            scope = scope,
            dependencies = rootModelDependencies,
            skeleton = skeleton,
        )
    },
)