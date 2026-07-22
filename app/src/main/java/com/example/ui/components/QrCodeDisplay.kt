package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import java.security.MessageDigest

@Composable
fun QrCodeDisplay(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    // Generate deterministic 21x21 QR-like matrix from string content hash
    val matrixSize = 21
    val matrix = BooleanArray(matrixSize * matrixSize)

    // Finder patterns (top-left, top-right, bottom-left 7x7 squares)
    fun setSquare(row: Int, col: Int, w: Int, h: Int) {
        for (r in row until (row + h)) {
            for (c in col until (col + w)) {
                if (r in 0 until matrixSize && c in 0 until matrixSize) {
                    val isBorder = r == row || r == row + h - 1 || c == col || c == col + w - 1
                    val isInner = r in (row + 2)..(row + 4) && c in (col + 2)..(col + 4)
                    matrix[r * matrixSize + c] = isBorder || isInner
                }
            }
        }
    }

    setSquare(0, 0, 7, 7)
    setSquare(0, 14, 7, 7)
    setSquare(14, 0, 7, 7)

    // Hash string to fill inner matrix
    val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
    var bitIndex = 0
    for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
            val inFinder1 = r in 0..7 && c in 0..7
            val inFinder2 = r in 0..7 && c in 13..20
            val inFinder3 = r in 13..20 && c in 0..7
            if (!inFinder1 && !inFinder2 && !inFinder3) {
                val byteVal = digest[bitIndex % digest.size].toInt()
                val bitVal = ((byteVal shr (bitIndex % 8)) and 1) == 1
                matrix[r * matrixSize + c] = bitVal
                bitIndex++
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(PureWhite, RoundedCornerShape(12.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val cellSize = this.size.width / matrixSize
            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r * matrixSize + c]) {
                        drawRect(
                            color = PureBlack,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}
