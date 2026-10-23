package com.example.androidbljoy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbljoy.theme.CyberPrimary
import com.example.androidbljoy.theme.CyberSurface
import com.example.androidbljoy.theme.CyberSurfaceVariant
import com.example.androidbljoy.theme.MutedText
import com.example.androidbljoy.theme.NeonRed
import com.example.androidbljoy.theme.OffWhite

@Composable
fun DigitalTrimControl(
    trimValue: Int,
    isLocked: Boolean,
    onTrimChange: (Int) -> Unit,
    onToggleLock: () -> Unit,
    isVertical: Boolean = true,
    label: String = "TRIM",
    modifier: Modifier = Modifier
) {
    if (isVertical) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier
        ) {
            // Trim UP button
            TrimButton(
                symbol = "▲",
                width = 34.dp,
                height = 32.dp,
                enabled = !isLocked,
                onClick = {
                    if (!isLocked) onTrimChange((trimValue + 1).coerceIn(-50, 50))
                }
            )

            // Center Scale & Value Display
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurface.copy(alpha = 0.95f))
                    .border(1.dp, if (isLocked) NeonRed.copy(alpha = 0.5f) else CyberSurfaceVariant, RoundedCornerShape(6.dp))
                    .pointerInput(isLocked) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!isLocked) onTrimChange(0)
                            },
                            onLongPress = {
                                onToggleLock()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background visual scale ticks
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.width(10.dp).height(1.dp).background(MutedText.copy(alpha = 0.4f)))
                    Box(modifier = Modifier.width(6.dp).height(1.dp).background(MutedText.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.width(18.dp).height(1.5.dp).background(if (trimValue == 0) CyberPrimary else MutedText))
                    Box(modifier = Modifier.width(6.dp).height(1.dp).background(MutedText.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.width(10.dp).height(1.dp).background(MutedText.copy(alpha = 0.4f)))
                }

                // Numeric Trim Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Trim Bloqueado",
                            tint = NeonRed,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = if (trimValue > 0) "+$trimValue" else "$trimValue",
                        color = if (trimValue == 0) MutedText else CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Trim DOWN button
            TrimButton(
                symbol = "▼",
                width = 34.dp,
                height = 32.dp,
                enabled = !isLocked,
                onClick = {
                    if (!isLocked) onTrimChange((trimValue - 1).coerceIn(-50, 50))
                }
            )
        }
    } else {
        // Horizontal Trim Switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
        ) {
            // Trim LEFT button
            TrimButton(
                symbol = "◄",
                width = 40.dp,
                height = 32.dp,
                enabled = !isLocked,
                onClick = {
                    if (!isLocked) onTrimChange((trimValue - 1).coerceIn(-50, 50))
                }
            )

            // Center Scale & Value Display (Horizontal)
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurface.copy(alpha = 0.95f))
                    .border(1.dp, if (isLocked) NeonRed.copy(alpha = 0.5f) else CyberSurfaceVariant, RoundedCornerShape(6.dp))
                    .pointerInput(isLocked) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!isLocked) onTrimChange(0)
                            },
                            onLongPress = {
                                onToggleLock()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background visual scale ticks
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.height(10.dp).width(1.dp).background(MutedText.copy(alpha = 0.4f)))
                    Box(modifier = Modifier.height(6.dp).width(1.dp).background(MutedText.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.height(18.dp).width(1.5.dp).background(if (trimValue == 0) CyberPrimary else MutedText))
                    Box(modifier = Modifier.height(6.dp).width(1.dp).background(MutedText.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.height(10.dp).width(1.dp).background(MutedText.copy(alpha = 0.4f)))
                }

                // Numeric Trim Readout
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Trim Bloqueado",
                            tint = NeonRed,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = if (trimValue > 0) "+$trimValue" else "$trimValue",
                        color = if (trimValue == 0) MutedText else CyberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Trim RIGHT button
            TrimButton(
                symbol = "►",
                width = 40.dp,
                height = 32.dp,
                enabled = !isLocked,
                onClick = {
                    if (!isLocked) onTrimChange((trimValue + 1).coerceIn(-50, 50))
                }
            )
        }
    }
}

@Composable
private fun TrimButton(
    symbol: String,
    enabled: Boolean,
    width: androidx.compose.ui.unit.Dp = 34.dp,
    height: androidx.compose.ui.unit.Dp = 32.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) CyberSurfaceVariant else CyberSurfaceVariant.copy(alpha = 0.3f))
            .border(BorderStroke(1.dp, if (enabled) CyberPrimary.copy(alpha = 0.5f) else Color.Transparent), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (enabled) CyberPrimary else MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
