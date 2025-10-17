package hnau.echospeak.app

import hnau.common.app.model.app.AppSeed
import hnau.common.app.model.theme.Hue
import hnau.common.app.model.theme.ThemeBrightness
import hnau.echospeak.model.RootModel
import hnau.echospeak.model.impl

fun createEchoSpeakAppSeed(
    defaultBrightness: ThemeBrightness? = null,
): AppSeed<RootModel, RootModel.Skeleton> = AppSeed(
    fallbackHue = Hue(240.0),
    defaultBrightness = defaultBrightness,
    skeletonSerializer = RootModel.Skeleton.serializer(),
    createDefaultSkeleton = { RootModel.Skeleton() },
    createModel = { scope, appContext, skeleton ->
        RootModel(
            scope = scope,
            dependencies = RootModel.Dependencies.impl(),
            skeleton = skeleton,
        )
    },
)