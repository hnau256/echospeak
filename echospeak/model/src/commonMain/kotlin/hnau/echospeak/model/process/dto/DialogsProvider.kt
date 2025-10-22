package hnau.echospeak.model.process.dto

import arrow.core.NonEmptyList

interface DialogsProvider {

    suspend fun loadDialogs(): NonEmptyList<Dialog>
}