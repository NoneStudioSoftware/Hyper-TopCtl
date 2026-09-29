package io.github.hypertopctl.ui.util

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.roundToInt

/**
 * Renders an Android [Drawable] (app icons) as a Compose [Painter] without pulling in
 * Accompanist. App icons are static, so drawable invalidation callbacks are not wired up.
 */
class DrawablePainter(private val drawable: Drawable) : Painter() {

    override val intrinsicSize: Size
        get() {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            return if (width > 0 && height > 0) Size(width.toFloat(), height.toFloat()) else Size.Unspecified
        }

    override fun DrawScope.onDraw() {
        // Draw into the layout-sized rect: drawing with intrinsic bounds would clip large
        // drawables (e.g. AdaptiveIconDrawable's 108dp canvas) to the top-left corner.
        drawable.setBounds(
            0,
            0,
            size.width.roundToInt().coerceAtLeast(0),
            size.height.roundToInt().coerceAtLeast(0),
        )
        drawIntoCanvas { it.nativeCanvas.let(drawable::draw) }
    }
}
