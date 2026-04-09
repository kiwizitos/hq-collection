package com.kiwizitos.siege.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Extensões e helpers úteis para o Design System Siege.
 */

/**
 * Adiciona uma borda vertical no lado esquerdo de um composable.
 * Útil para cards e containers com accent.
 * 
 * @param color Cor da borda
 * @param width Largura da borda
 */
fun Modifier.leftBorder(color: Color, width: Dp): Modifier = this.then(
    drawBehind {
        val borderWidth = width.toPx()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = borderWidth
        )
    }
)

/**
 * Adiciona uma borda vertical no lado direito de um composable.
 * 
 * @param color Cor da borda
 * @param width Largura da borda
 */
fun Modifier.rightBorder(color: Color, width: Dp): Modifier = this.then(
    drawBehind {
        val borderWidth = width.toPx()
        drawLine(
            color = color,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = borderWidth
        )
    }
)

/**
 * Adiciona uma borda horizontal no topo de um composable.
 * 
 * @param color Cor da borda
 * @param width Largura da borda
 */
fun Modifier.topBorder(color: Color, width: Dp): Modifier = this.then(
    drawBehind {
        val borderWidth = width.toPx()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = borderWidth
        )
    }
)

/**
 * Adiciona uma borda horizontal no fundo de um composable.
 * 
 * @param color Cor da borda
 * @param width Largura da borda
 */
fun Modifier.bottomBorder(color: Color, width: Dp): Modifier = this.then(
    drawBehind {
        val borderWidth = width.toPx()
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = borderWidth
        )
    }
)
