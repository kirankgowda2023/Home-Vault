package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WarrantyInfo
import com.example.data.model.WarrantyState
import com.example.ui.theme.BrandEmeraldPrimary
import com.example.ui.theme.WarrantyActiveGreen
import com.example.ui.theme.WarrantyActiveGreenLight
import com.example.ui.theme.WarrantyExpiredRed
import com.example.ui.theme.WarrantyExpiredRedLight
import com.example.ui.theme.WarrantyExpiringAmber
import com.example.ui.theme.WarrantyExpiringAmberLight
import com.example.ui.theme.WarrantyNoneGray
import com.example.ui.theme.WarrantyNoneGrayLight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

// Signature Isometric Cube Wireframe Logo Icon from Figma
@Composable
fun HomeVaultCubeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF00A884),
    strokeWidth: Float = 2.5f
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Isometric cube geometry
        val top = Offset(cx, h * 0.12f)
        val topLeft = Offset(w * 0.15f, h * 0.32f)
        val topRight = Offset(w * 0.85f, h * 0.32f)
        val center = Offset(cx, cy * 1.05f)
        val bottomLeft = Offset(w * 0.15f, h * 0.68f)
        val bottomRight = Offset(w * 0.85f, h * 0.68f)
        val bottom = Offset(cx, h * 0.88f)

        val stroke = Stroke(
            width = strokeWidth.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Outer hexagon boundary
        val outerPath = Path().apply {
            moveTo(top.x, top.y)
            lineTo(topRight.x, topRight.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottom.x, bottom.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            lineTo(topLeft.x, topLeft.y)
            close()
        }
        drawPath(outerPath, color = tint, style = stroke)

        // Inner 3 connecting spokes
        drawLine(color = tint, start = top, end = center, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color = tint, start = center, end = bottomLeft, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color = tint, start = center, end = bottomRight, strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

// 100% Secure Local Storage banner found across Figma screens
@Composable
fun SecurityBanner(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFE6FFFA),
        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().testTag("security_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF0D9488),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "100% Secure Local Storage. No cloud sync, no tracking.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF065F46)
            )
        }
    }
}

// Figma UI Kit Badges (Active, Expiring soon, Expired)
@Composable
fun FigmaWarrantyBadge(
    state: WarrantyState,
    daysRemaining: Long = 0,
    humanReadable: String = "",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (state) {
        WarrantyState.ACTIVE -> Triple(
            Color(0xFFD1FAE5),
            Color(0xFF0F766E),
            if (humanReadable.isNotBlank() && humanReadable.contains("Year")) humanReadable else "Active Warranty"
        )
        WarrantyState.EXPIRING_SOON -> Triple(
            if (daysRemaining in 1..7) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
            if (daysRemaining in 1..7) Color(0xFFDC2626) else Color(0xFFD97706),
            if (daysRemaining in 1..7) "Expires in $daysRemaining days" else "Expiring soon"
        )
        WarrantyState.EXPIRED -> Triple(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            "Expired"
        )
        WarrantyState.NO_WARRANTY -> Triple(
            Color(0xFFF1F5F9),
            Color(0xFF94A3B8),
            "No Warranty"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// Figma UI Kit Toast / Notification
@Composable
fun FeedbackToastCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0B132B),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D9488).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun WarrantyBadge(
    warrantyInfo: WarrantyInfo,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (dotColor, bgColor, textColor, label) = when (warrantyInfo.state) {
        WarrantyState.ACTIVE -> Quad(
            WarrantyActiveGreen,
            WarrantyActiveGreenLight,
            Color(0xFF065F46),
            if (compact) "Active" else "Active • ${warrantyInfo.humanReadableRemaining}"
        )
        WarrantyState.EXPIRING_SOON -> Quad(
            WarrantyExpiringAmber,
            WarrantyExpiringAmberLight,
            Color(0xFF92400E),
            warrantyInfo.humanReadableRemaining
        )
        WarrantyState.EXPIRED -> Quad(
            WarrantyExpiredRed,
            WarrantyExpiredRedLight,
            Color(0xFF991B1B),
            if (compact) "Expired" else warrantyInfo.humanReadableRemaining
        )
        WarrantyState.NO_WARRANTY -> Quad(
            WarrantyNoneGray,
            WarrantyNoneGrayLight,
            Color(0xFF334155),
            "No Warranty"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("warranty_badge_${warrantyInfo.state.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 6.dp else 8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun PrivacyPill(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.06f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Offline Privacy",
                tint = BrandEmeraldPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "100% Offline & Private",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BillStatusBadge(hasBill: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (hasBill) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasBill) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = if (hasBill) Icons.Default.Receipt else Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = if (hasBill) Color(0xFF1D4ED8) else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (hasBill) "Bill Secured" else "No Bill",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasBill) Color(0xFF1E40AF) else Color(0xFF64748B)
            )
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "home" -> Icons.Default.Home
        "electronics" -> Icons.Default.Smartphone
        "vehicle" -> Icons.Default.DirectionsCar
        "kitchen" -> Icons.Default.Kitchen
        "personal" -> Icons.Default.Person
        else -> Icons.Default.Category
    }
}

fun getCategoryAccentColor(category: String): Color {
    return when (category.lowercase()) {
        "home" -> Color(0xFF2563EB)
        "electronics" -> Color(0xFF7C3AED)
        "vehicle" -> Color(0xFFD97706)
        "kitchen" -> Color(0xFF059669)
        "personal" -> Color(0xFFDB2777)
        else -> Color(0xFF475569)
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
