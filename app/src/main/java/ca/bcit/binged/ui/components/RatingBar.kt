package ca.bcit.binged.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Star rating. Pass onRatingChange to make the stars tappable;
// leave it out to just display a rating.
@Composable
fun RatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 20.dp,
    onRatingChange: ((Int) -> Unit)? = null
) {
    Row(modifier = modifier.semantics { contentDescription = "$rating out of $maxRating stars" }) {
        for (star in 1..maxRating) {
            val tint = if (star <= rating) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            if (onRatingChange != null) {
                IconButton(onClick = { onRatingChange(star) }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "$star stars",
                        tint = tint,
                        modifier = Modifier.size(starSize)
                    )
                }
            } else {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}