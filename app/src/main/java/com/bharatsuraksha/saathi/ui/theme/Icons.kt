package com.bharatsuraksha.saathi.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-built icons on a 24dp grid with consistent 2dp round strokes. No
 * material-icons-extended, no emoji.
 */
private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

private fun ImageVector.Builder.line(width: Float = 2f, pathBuilder: PathBuilder.() -> Unit) =
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder
    )

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
    arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
    close()
}

/** A jaali-lattice shield. */
val JyotiShield: ImageVector by lazy {
    icon("JyotiShield") {
        line {
            moveTo(12f, 2.5f)
            lineTo(20f, 5.6f)
            lineTo(20f, 11.2f)
            curveTo(20f, 16.6f, 16.6f, 20.1f, 12f, 21.5f)
            curveTo(7.4f, 20.1f, 4f, 16.6f, 4f, 11.2f)
            lineTo(4f, 5.6f)
            close()
        }
        line(1f) { moveTo(8f, 7.4f); lineTo(8f, 16.4f) }
        line(1f) { moveTo(12f, 6.2f); lineTo(12f, 18.6f) }
        line(1f) { moveTo(16f, 7.4f); lineTo(16f, 16.4f) }
    }
}

/** A diya flame over its bowl. */
val JyotiFlame: ImageVector by lazy {
    icon("JyotiFlame") {
        line {
            moveTo(12f, 2.2f)
            curveTo(15.4f, 6.2f, 16.9f, 8.4f, 16.9f, 11.6f)
            curveTo(16.9f, 15f, 14.7f, 17.4f, 12f, 17.4f)
            curveTo(9.3f, 17.4f, 7.1f, 15f, 7.1f, 11.6f)
            curveTo(7.1f, 9.7f, 8.1f, 8.3f, 9.3f, 7.1f)
            curveTo(9.5f, 8.9f, 10.3f, 9.9f, 11.3f, 10.3f)
            curveTo(11.1f, 7.9f, 11.5f, 4.9f, 12f, 2.2f)
            close()
        }
        line {
            moveTo(4.6f, 19.6f)
            curveTo(7.6f, 22.1f, 16.4f, 22.1f, 19.4f, 19.6f)
        }
    }
}

/** Two links of a chain. */
val JyotiLink: ImageVector by lazy {
    icon("JyotiLink") {
        line {
            moveTo(10f, 14f)
            lineTo(14f, 10f)
        }
        line {
            moveTo(8.6f, 11.4f)
            lineTo(6.8f, 13.2f)
            arcToRelative(3.2f, 3.2f, 0f, false, false, 4.5f, 4.5f)
            lineTo(13.1f, 15.9f)
        }
        line {
            moveTo(15.4f, 12.6f)
            lineTo(17.2f, 10.8f)
            arcToRelative(3.2f, 3.2f, 0f, false, false, -4.5f, -4.5f)
            lineTo(10.9f, 8.1f)
        }
    }
}

/** A chat bubble. */
val JyotiChat: ImageVector by lazy {
    icon("JyotiChat") {
        line {
            moveTo(4.5f, 5.5f)
            lineTo(19.5f, 5.5f)
            curveTo(20.3f, 5.5f, 21f, 6.2f, 21f, 7f)
            lineTo(21f, 15.5f)
            curveTo(21f, 16.3f, 20.3f, 17f, 19.5f, 17f)
            lineTo(11f, 17f)
            lineTo(7f, 20.5f)
            lineTo(7f, 17f)
            lineTo(4.5f, 17f)
            curveTo(3.7f, 17f, 3f, 16.3f, 3f, 15.5f)
            lineTo(3f, 7f)
            curveTo(3f, 6.2f, 3.7f, 5.5f, 4.5f, 5.5f)
            close()
        }
        line(1.6f) { moveTo(8f, 9.5f); lineTo(16f, 9.5f) }
        line(1.6f) { moveTo(8f, 13f); lineTo(13f, 13f) }
    }
}

/** A telephone handset. */
val JyotiCall: ImageVector by lazy {
    icon("JyotiCall") {
        line {
            moveTo(6.2f, 3.5f)
            lineTo(9f, 3.5f)
            lineTo(10.6f, 8f)
            lineTo(8.6f, 9.6f)
            curveTo(9.6f, 12.2f, 11.8f, 14.4f, 14.4f, 15.4f)
            lineTo(16f, 13.4f)
            lineTo(20.5f, 15f)
            lineTo(20.5f, 17.8f)
            curveTo(20.5f, 19.4f, 19.1f, 20.6f, 17.5f, 20.4f)
            curveTo(10.4f, 19.5f, 4.5f, 13.6f, 3.6f, 6.5f)
            curveTo(3.4f, 4.9f, 4.6f, 3.5f, 6.2f, 3.5f)
            close()
        }
    }
}

/** A camera with a lens. */
val JyotiCamera: ImageVector by lazy {
    icon("JyotiCamera") {
        line {
            moveTo(4f, 7.5f)
            lineTo(8f, 7.5f)
            lineTo(9.4f, 5.2f)
            lineTo(14.6f, 5.2f)
            lineTo(16f, 7.5f)
            lineTo(20f, 7.5f)
            curveTo(20.8f, 7.5f, 21.5f, 8.2f, 21.5f, 9f)
            lineTo(21.5f, 17.5f)
            curveTo(21.5f, 18.3f, 20.8f, 19f, 20f, 19f)
            lineTo(4f, 19f)
            curveTo(3.2f, 19f, 2.5f, 18.3f, 2.5f, 17.5f)
            lineTo(2.5f, 9f)
            curveTo(2.5f, 8.2f, 3.2f, 7.5f, 4f, 7.5f)
            close()
        }
        line(1.8f) { circle(12f, 13f, 3.4f) }
    }
}

/** A family: two adults and a child. */
val JyotiFamily: ImageVector by lazy {
    icon("JyotiFamily") {
        line(1.8f) { circle(7f, 6.6f, 2.6f) }
        line(1.8f) { circle(17f, 7.6f, 2.2f) }
        line(1.8f) { circle(12f, 13.6f, 2.0f) }
        line {
            moveTo(3f, 19.5f)
            curveTo(3f, 16.4f, 4.8f, 14.6f, 7f, 14.6f)
            curveTo(8.3f, 14.6f, 9.3f, 15.2f, 10f, 16.2f)
        }
        line {
            moveTo(21f, 19.5f)
            curveTo(21f, 16.8f, 19.5f, 15.2f, 17f, 15.2f)
            curveTo(15.8f, 15.2f, 14.9f, 15.7f, 14.2f, 16.5f)
        }
        line(1.8f) {
            moveTo(8.6f, 19.5f)
            curveTo(8.6f, 17.6f, 10f, 16.4f, 12f, 16.4f)
            curveTo(14f, 16.4f, 15.4f, 17.6f, 15.4f, 19.5f)
        }
    }
}

/** An open book. */
val JyotiBook: ImageVector by lazy {
    icon("JyotiBook") {
        line {
            moveTo(12f, 6.6f)
            curveTo(10.2f, 4.9f, 7.4f, 4.4f, 3.5f, 4.9f)
            lineTo(3.5f, 18.2f)
            curveTo(7.4f, 17.7f, 10.2f, 18.2f, 12f, 19.9f)
            curveTo(13.8f, 18.2f, 16.6f, 17.7f, 20.5f, 18.2f)
            lineTo(20.5f, 4.9f)
            curveTo(16.6f, 4.4f, 13.8f, 4.9f, 12f, 6.6f)
            close()
        }
        line(1.4f) { moveTo(12f, 6.6f); lineTo(12f, 19.9f) }
    }
}

/** A diya lamp with a flame. */
val JyotiLamp: ImageVector by lazy {
    icon("JyotiLamp") {
        line(1.6f) {
            moveTo(12f, 3.2f)
            curveTo(13.6f, 5.2f, 14.2f, 6.3f, 14.2f, 7.9f)
            curveTo(14.2f, 9.6f, 13.2f, 10.7f, 12f, 10.7f)
            curveTo(10.8f, 10.7f, 9.8f, 9.6f, 9.8f, 7.9f)
            curveTo(9.8f, 7.2f, 10.2f, 6.4f, 10.8f, 5.7f)
            curveTo(10.9f, 6.5f, 11.3f, 7f, 11.8f, 7.2f)
            curveTo(11.7f, 6.1f, 11.8f, 4.6f, 12f, 3.2f)
            close()
        }
        line {
            moveTo(3.5f, 13.5f)
            lineTo(20.5f, 13.5f)
            curveTo(20.5f, 17.4f, 16.7f, 20.5f, 12f, 20.5f)
            curveTo(7.3f, 20.5f, 3.5f, 17.4f, 3.5f, 13.5f)
            close()
        }
    }
}

/** A settings cog. */
val JyotiGear: ImageVector by lazy {
    icon("JyotiGear") {
        line(1.8f) { circle(12f, 12f, 3.2f) }
        line(1.8f) {
            moveTo(12f, 3.2f); lineTo(12f, 5.6f)
            moveTo(12f, 18.4f); lineTo(12f, 20.8f)
            moveTo(3.2f, 12f); lineTo(5.6f, 12f)
            moveTo(18.4f, 12f); lineTo(20.8f, 12f)
            moveTo(5.8f, 5.8f); lineTo(7.5f, 7.5f)
            moveTo(16.5f, 16.5f); lineTo(18.2f, 18.2f)
            moveTo(18.2f, 5.8f); lineTo(16.5f, 7.5f)
            moveTo(7.5f, 16.5f); lineTo(5.8f, 18.2f)
        }
    }
}
