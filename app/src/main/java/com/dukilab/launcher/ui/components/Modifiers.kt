package com.dukilab.launcher.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

/** 左上に直角二等辺三角形を描く（dukiLab.の角マーク、bundle.css の .dl-cut）。 */
fun Modifier.cornerMark(size: Dp, color: Color): Modifier = drawWithContent {
    drawContent()
    val s = size.toPx()
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(s, 0f)
        lineTo(0f, s)
        close()
    }
    drawPath(path, color)
}

/** 方眼の地（bundle.css の .dl-grid）。線は装飾専用。 */
fun Modifier.gridBackground(surface: Color, line: Color, cell: Dp): Modifier = drawBehind {
    drawRect(surface)
    val step = cell.toPx()
    if (step <= 0f) return@drawBehind
    val stroke = 1f
    var x = 0f
    while (x <= size.width) {
        drawLine(line, Offset(x, 0f), Offset(x, size.height), stroke)
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(line, Offset(0f, y), Offset(size.width, y), stroke)
        y += step
    }
}
