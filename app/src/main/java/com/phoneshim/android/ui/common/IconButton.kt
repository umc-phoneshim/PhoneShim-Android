package com.phoneshim.android.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phoneshim.android.ui.theme.PhoneShimDimens

@Composable
fun IconButton(
    label: String,
    @DrawableRes icon: Int,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconWidth: Dp = 16.dp,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val acceptsInput = enabled && !isLoading
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled || isLoading) 1f else 0.6f)
            .background(backgroundColor, RoundedCornerShape(PhoneShimDimens.spacing12))
            .clickable(enabled = acceptsInput, role = Role.Button, onClick = onClick)
            .padding(PhoneShimDimens.spacing16),
        horizontalArrangement = Arrangement.spacedBy(
            PhoneShimDimens.spacing12,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(iconWidth)
                    .height(16.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .width(iconWidth)
                    .height(16.dp),
            )
        }
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
