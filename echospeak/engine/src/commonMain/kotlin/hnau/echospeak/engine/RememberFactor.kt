package hnau.echospeak.engine

@JvmInline
value class RememberFactor(
    val factor: Float,
) {

    companion object {

        val unknown: RememberFactor =
            RememberFactor(0f)
    }
}