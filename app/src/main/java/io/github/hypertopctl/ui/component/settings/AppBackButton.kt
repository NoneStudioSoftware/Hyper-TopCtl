package io.github.hypertopctl.ui.component.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Standard Android 16/17 Expressive back button with circular filled background.
 * Ported from ReSukiSU Manager's AppBackButton.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppBackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    containerColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
    contentDescription: String? = null,
) {
    Row {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(36.dp),
            shapes = IconButtonDefaults.shapes(
                shape = CircleShape,
            ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = containerColor,
            ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
