package hnau.echospeak.model.utils.compare

import org.apache.commons.text.similarity.LevenshteinDistance
import java.text.Normalizer
import kotlin.math.max

class TextComparator(
    base: String,
) {

    private val base: String? = base.normalizeToNonEmpty()

    fun compare(
        to: String,
    ): Similarity {
        val base = base ?: return Similarity.full
        val toNormalized = to.normalizeToNonEmpty() ?: return Similarity.full

        val maxLength = max(toNormalized.length, base.length)

        val distance = LevenshteinDistance
            .getDefaultInstance()
            .apply(base, toNormalized)
            .toFloat()

        return Similarity(
            factor = 1f - (distance / maxLength),
        )
    }

    companion object {

        private fun String.normalizeToNonEmpty(): String? = Normalizer
            .normalize(this, Normalizer.Form.NFD)
            .filter { ch ->
                when {
                    Character.getType(ch) == Character.NON_SPACING_MARK.toInt() -> false
                    ch == ' ' -> true
                    ch.isLetterOrDigit() -> true
                    else -> false
                }
            }
            .lowercase()
            .trim()
            .takeIf(String::isNotEmpty)
    }
}