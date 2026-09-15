package com.example.engine

import com.example.data.model.DrawCommand

data class StrokePoint(val x: Float, val y: Float)

typealias StrokePath = List<StrokePoint>

enum class VectorFont(val displayName: String) {
    HERSHEY_SANS("Hershey Sans (Clean)"),
    HERSHEY_SERIF("Hershey Serif"),
    SIMPLEX("Simplex Technical"),
    SCRIPT("Script / Cursive")
}

/**
 * Single-line vector stroke font engine designed specifically for pen plotters.
 * Converts text into machine-readable toolpath vectors without rasterization or outline fills.
 */
object StrokeFontEngine {
    private const val GLYPH_WIDTH = 10f
    private const val GLYPH_HEIGHT = 14f

    private val glyphMap: Map<Char, List<StrokePath>> by lazy {
        val map = mutableMapOf<Char, List<StrokePath>>()

        fun path(vararg pts: Pair<Number, Number>): StrokePath =
            pts.map { StrokePoint(it.first.toFloat(), it.second.toFloat()) }

        // Letters A-Z
        map['A'] = listOf(
            path(0 to 14, 5 to 0, 10 to 14),
            path(2.5 to 8, 7.5 to 8)
        )
        map['B'] = listOf(
            path(0 to 14, 0 to 0, 7 to 0, 10 to 3.5, 7 to 7, 0 to 7),
            path(7 to 7, 10 to 10.5, 7 to 14, 0 to 14)
        )
        map['C'] = listOf(
            path(10 to 3, 7 to 0, 3 to 0, 0 to 3, 0 to 11, 3 to 14, 7 to 14, 10 to 11)
        )
        map['D'] = listOf(
            path(0 to 14, 0 to 0, 6 to 0, 10 to 4, 10 to 10, 6 to 14, 0 to 14)
        )
        map['E'] = listOf(
            path(10 to 0, 0 to 0, 0 to 14, 10 to 14),
            path(0 to 7, 7 to 7)
        )
        map['F'] = listOf(
            path(0 to 14, 0 to 0, 10 to 0),
            path(0 to 7, 7 to 7)
        )
        map['G'] = listOf(
            path(10 to 3, 7 to 0, 3 to 0, 0 to 3, 0 to 11, 3 to 14, 7 to 14, 10 to 11, 10 to 7, 5 to 7)
        )
        map['H'] = listOf(
            path(0 to 0, 0 to 14),
            path(10 to 0, 10 to 14),
            path(0 to 7, 10 to 7)
        )
        map['I'] = listOf(
            path(2 to 0, 8 to 0),
            path(5 to 0, 5 to 14),
            path(2 to 14, 8 to 14)
        )
        map['J'] = listOf(
            path(8 to 0, 8 to 11, 5 to 14, 2 to 14, 0 to 11)
        )
        map['K'] = listOf(
            path(0 to 0, 0 to 14),
            path(10 to 0, 0 to 8, 10 to 14)
        )
        map['L'] = listOf(
            path(0 to 0, 0 to 14, 10 to 14)
        )
        map['M'] = listOf(
            path(0 to 14, 0 to 0, 5 to 7, 10 to 0, 10 to 14)
        )
        map['N'] = listOf(
            path(0 to 14, 0 to 0, 10 to 14, 10 to 0)
        )
        map['O'] = listOf(
            path(3 to 0, 7 to 0, 10 to 3, 10 to 11, 7 to 14, 3 to 14, 0 to 11, 0 to 3, 3 to 0)
        )
        map['P'] = listOf(
            path(0 to 14, 0 to 0, 7 to 0, 10 to 3.5, 7 to 7, 0 to 7)
        )
        map['Q'] = listOf(
            path(3 to 0, 7 to 0, 10 to 3, 10 to 11, 7 to 14, 3 to 14, 0 to 11, 0 to 3, 3 to 0),
            path(6 to 10, 10 to 14)
        )
        map['R'] = listOf(
            path(0 to 14, 0 to 0, 7 to 0, 10 to 3.5, 7 to 7, 0 to 7),
            path(5 to 7, 10 to 14)
        )
        map['S'] = listOf(
            path(10 to 3, 7 to 0, 3 to 0, 0 to 3, 3 to 7, 7 to 7, 10 to 11, 7 to 14, 3 to 14, 0 to 11)
        )
        map['T'] = listOf(
            path(0 to 0, 10 to 0),
            path(5 to 0, 5 to 14)
        )
        map['U'] = listOf(
            path(0 to 0, 0 to 11, 3 to 14, 7 to 14, 10 to 11, 10 to 0)
        )
        map['V'] = listOf(
            path(0 to 0, 5 to 14, 10 to 0)
        )
        map['W'] = listOf(
            path(0 to 0, 2.5 to 14, 5 to 7, 7.5 to 14, 10 to 0)
        )
        map['X'] = listOf(
            path(0 to 0, 10 to 14),
            path(10 to 0, 0 to 14)
        )
        map['Y'] = listOf(
            path(0 to 0, 5 to 7, 10 to 0),
            path(5 to 7, 5 to 14)
        )
        map['Z'] = listOf(
            path(0 to 0, 10 to 0, 0 to 14, 10 to 14)
        )

        // Numbers 0-9
        map['0'] = listOf(
            path(3 to 0, 7 to 0, 10 to 3, 10 to 11, 7 to 14, 3 to 14, 0 to 11, 0 to 3, 3 to 0),
            path(8 to 2, 2 to 12)
        )
        map['1'] = listOf(
            path(2 to 3, 5 to 0, 5 to 14),
            path(1 to 14, 9 to 14)
        )
        map['2'] = listOf(
            path(0 to 3, 3 to 0, 7 to 0, 10 to 3, 10 to 6, 0 to 14, 10 to 14)
        )
        map['3'] = listOf(
            path(0 to 2, 3 to 0, 7 to 0, 10 to 3, 7 to 7, 10 to 11, 7 to 14, 3 to 14, 0 to 12),
            path(4 to 7, 7 to 7)
        )
        map['4'] = listOf(
            path(7 to 14, 7 to 0, 0 to 9, 10 to 9)
        )
        map['5'] = listOf(
            path(10 to 0, 0 to 0, 0 to 6, 7 to 6, 10 to 9, 7 to 14, 2 to 14, 0 to 12)
        )
        map['6'] = listOf(
            path(8 to 0, 3 to 0, 0 to 3, 0 to 11, 3 to 14, 7 to 14, 10 to 11, 10 to 7, 7 to 6, 0 to 6)
        )
        map['7'] = listOf(
            path(0 to 0, 10 to 0, 4 to 14)
        )
        map['8'] = listOf(
            path(3 to 0, 7 to 0, 10 to 3, 7 to 7, 10 to 11, 7 to 14, 3 to 14, 0 to 11, 3 to 7, 0 to 3, 3 to 0)
        )
        map['9'] = listOf(
            path(2 to 14, 7 to 14, 10 to 11, 10 to 3, 7 to 0, 3 to 0, 0 to 3, 0 to 7, 3 to 8, 10 to 8)
        )

        // Punctuation and symbols
        map['.'] = listOf(path(4 to 13, 6 to 13, 6 to 14, 4 to 14, 4 to 13))
        map[','] = listOf(path(5 to 12, 5 to 14, 3 to 16))
        map['!'] = listOf(
            path(5 to 0, 5 to 9),
            path(4.5 to 13, 5.5 to 13, 5.5 to 14, 4.5 to 14, 4.5 to 13)
        )
        map['?'] = listOf(
            path(0 to 3, 3 to 0, 7 to 0, 10 to 3, 7 to 7, 5 to 8, 5 to 10),
            path(4.5 to 13, 5.5 to 13, 5.5 to 14, 4.5 to 14, 4.5 to 13)
        )
        map['-'] = listOf(path(1 to 7, 9 to 7))
        map['+'] = listOf(
            path(1 to 7, 9 to 7),
            path(5 to 3, 5 to 11)
        )
        map['/'] = listOf(path(0 to 14, 10 to 0))
        map[':'] = listOf(
            path(4.5 to 4, 5.5 to 4, 5.5 to 5, 4.5 to 5, 4.5 to 4),
            path(4.5 to 11, 5.5 to 11, 5.5 to 12, 4.5 to 12, 4.5 to 11)
        )
        map['='] = listOf(
            path(1 to 5, 9 to 5),
            path(1 to 9, 9 to 9)
        )
        map['('] = listOf(path(8 to 0, 4 to 4, 4 to 10, 8 to 14))
        map[')'] = listOf(path(2 to 0, 6 to 4, 6 to 10, 2 to 14))
        map['#'] = listOf(
            path(3 to 0, 3 to 14),
            path(7 to 0, 7 to 14),
            path(0 to 4, 10 to 4),
            path(0 to 10, 10 to 10)
        )
        map['\''] = listOf(path(5 to 0, 5 to 4))
        map['"'] = listOf(path(3 to 0, 3 to 4), path(7 to 0, 7 to 4))
        map['_'] = listOf(path(0 to 14, 10 to 14))

        // Populate lowercase from uppercase as standard clean single-line sans
        for (c in 'a'..'z') {
            val upper = c.uppercaseChar()
            if (map.containsKey(upper)) {
                map[c] = map[upper]!!
            }
        }

        map
    }

    /**
     * Converts a string into a list of vector strokes on the machine coordinate bed.
     *
     * @param text String to write.
     * @param fontSizeMm Height of character glyph in millimeters.
     * @param letterSpacingMm Space between consecutive characters in mm.
     * @param lineSpacingMm Space between lines of text in mm.
     * @param startX Starting X coordinate in mm.
     * @param startY Starting Y coordinate in mm.
     * @param maxBedWidth Maximum width of machine bed in mm.
     * @param maxBedHeight Maximum height of machine bed in mm.
     */
    fun vectorizeText(
        text: String,
        fontSizeMm: Float = 10f,
        letterSpacingMm: Float = 2f,
        lineSpacingMm: Float = 5f,
        startX: Float = 10f,
        startY: Float = 10f,
        maxBedWidth: Float = 100f,
        maxBedHeight: Float = 100f,
        font: VectorFont = VectorFont.HERSHEY_SANS
    ): List<StrokePath> {
        val strokes = mutableListOf<StrokePath>()
        val scaleY = fontSizeMm / GLYPH_HEIGHT
        val baseWidth = when (font) {
            VectorFont.SIMPLEX -> GLYPH_WIDTH * 0.85f
            VectorFont.SCRIPT -> GLYPH_WIDTH * 0.9f
            else -> GLYPH_WIDTH
        }
        val scaleX = scaleY * (baseWidth / GLYPH_HEIGHT)
        val charWidthMm = baseWidth * scaleX
        val lineHeightMm = fontSizeMm + lineSpacingMm

        var cursorX = startX
        var cursorY = startY

        for (char in text) {
            when (char) {
                '\n' -> {
                    cursorX = startX
                    cursorY += lineHeightMm
                    continue
                }
                ' ' -> {
                    cursorX += charWidthMm * 0.7f + letterSpacingMm
                    continue
                }
            }

            // Word wrap if beyond machine bed width
            if (cursorX + charWidthMm > maxBedWidth - 2f && cursorX > startX) {
                cursorX = startX
                cursorY += lineHeightMm
            }

            val glyph = glyphMap[char] ?: glyphMap[char.uppercaseChar()]
            if (glyph != null) {
                for (glyphStroke in glyph) {
                    val transformedStroke = glyphStroke.map { pt ->
                        val slant = if (font == VectorFont.SCRIPT) (GLYPH_HEIGHT - pt.y) * 0.22f else 0f
                        val px = (cursorX + (pt.x + slant) * scaleX).coerceIn(0f, maxBedWidth)
                        val py = (cursorY + pt.y * scaleY).coerceIn(0f, maxBedHeight)
                        StrokePoint(px, py)
                    }
                    if (transformedStroke.isNotEmpty()) {
                        strokes.add(transformedStroke)
                    }
                }

                if (font == VectorFont.HERSHEY_SERIF && char.isLetter()) {
                    val baselineY = (cursorY + GLYPH_HEIGHT * scaleY).coerceIn(0f, maxBedHeight)
                    strokes.add(listOf(
                        StrokePoint((cursorX - 0.5f * scaleX).coerceIn(0f, maxBedWidth), baselineY),
                        StrokePoint((cursorX + 2.5f * scaleX).coerceIn(0f, maxBedWidth), baselineY)
                    ))
                }
            }

            cursorX += charWidthMm + letterSpacingMm
        }

        return strokes
    }

    fun textToStrokes(
        text: String,
        fontSizeMm: Float = 10f,
        letterSpacingMm: Float = 2f,
        lineSpacingMm: Float = 5f,
        startX: Float = 10f,
        startY: Float = 10f,
        maxBedWidth: Float = 100f,
        maxBedHeight: Float = 100f,
        font: VectorFont = VectorFont.HERSHEY_SANS
    ): List<StrokePath> = vectorizeText(text, fontSizeMm, letterSpacingMm, lineSpacingMm, startX, startY, maxBedWidth, maxBedHeight, font)

    /**
     * Converts a collection of vector strokes into machine [DrawCommand] list.
     * Handles Pen UP rapid travel and Pen DOWN drawing contact.
     */
    fun strokesToCommands(strokes: List<StrokePath>): List<DrawCommand> {
        val commands = mutableListOf<DrawCommand>()
        for (stroke in strokes) {
            if (stroke.isEmpty()) continue
            val first = stroke.first()
            // Rapid move with pen UP to start position
            commands.add(DrawCommand(x = first.x, y = first.y, pen = 0))
            // Touch pen DOWN
            commands.add(DrawCommand(x = first.x, y = first.y, pen = 1))
            // Follow stroke path with pen DOWN
            for (i in 1 until stroke.size) {
                val pt = stroke[i]
                commands.add(DrawCommand(x = pt.x, y = pt.y, pen = 1))
            }
            // Lift pen UP at end of stroke
            val last = stroke.last()
            commands.add(DrawCommand(x = last.x, y = last.y, pen = 0))
        }
        return commands
    }
}
