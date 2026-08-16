package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.model.CapturedPhoto
import com.example.model.ResolutionMode
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoViewerDialog(
    photo: CapturedPhoto,
    onDismiss: () -> Unit,
    onDelete: (CapturedPhoto) -> Unit
) {
    val context = LocalContext.current
    var showExifSheet by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 6f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("photo_viewer_dialog")
    ) {
        // Zoomable / Pannable Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo.filePath,
                contentDescription = "Visualisation Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }

        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CameraDarkBackground.copy(alpha = 0.7f))
                    .testTag("photo_viewer_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White
                )
            }

            // Resolution & Mode Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (photo.resolutionMode == ResolutionMode.MP_50) CameraCyanAccent else CameraAmberPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "${photo.resolutionMode.label} • ${photo.width} × ${photo.height}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Info EXIF toggle
                IconButton(
                    onClick = { showExifSheet = !showExifSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CameraDarkBackground.copy(alpha = 0.7f))
                        .testTag("photo_viewer_exif_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Détails EXIF",
                        tint = if (showExifSheet) CameraAmberPrimary else Color.White
                    )
                }

                // Delete Button
                IconButton(
                    onClick = {
                        onDelete(photo)
                        onDismiss()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CameraDarkBackground.copy(alpha = 0.7f))
                        .testTag("photo_viewer_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = CameraShutterRed
                    )
                }
            }
        }

        // Bottom EXIF Metadata Card Overlay
        AnimatedVisibility(
            visible = showExifSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            ExifMetadataCard(photo = photo)
        }
    }
}

@Composable
fun ExifMetadataCard(
    photo: CapturedPhoto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CameraDarkSurfaceVariant.copy(alpha = 0.95f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CameraBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MÉTADONNÉES DE PRISE DE VUE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = CameraAmberPrimary
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = CameraTextTertiary
                )
            }

            Divider(color = CameraBorder)

            // Grid of EXIF parameters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExifItem(title = "Résolution", value = "${photo.resolutionMode.label} (${photo.megapixels} MP)")
                ExifItem(title = "Dimensions", value = "${photo.width} × ${photo.height}")
                ExifItem(title = "ISO", value = photo.iso)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExifItem(title = "Vitesse", value = photo.shutterSpeed)
                ExifItem(title = "Balance Blancs", value = photo.whiteBalance)
                ExifItem(title = "Ouverture", value = photo.aperture)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExifItem(title = "Zoom Total", value = String.format(Locale.US, "%.1fx", photo.totalZoom))
                ExifItem(title = "Zoom Physique", value = String.format(Locale.US, "%.1fx (Optique)", photo.opticalZoom))
                ExifItem(title = "Zoom Numérique", value = String.format(Locale.US, "%.1fx (Recadrage)", photo.digitalZoom))
            }

            val sizeKb = photo.fileSizeBytes / 1024
            val sizeMb = sizeKb / 1024f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExifItem(title = "Taille du fichier", value = if (sizeMb >= 1f) String.format(Locale.US, "%.2f Mo", sizeMb) else "$sizeKb Ko")
                ExifItem(title = "Mode Capteur", value = photo.resolutionMode.subtitle)
            }
        }
    }
}

@Composable
private fun ExifItem(
    title: String,
    value: String
) {
    Column(modifier = Modifier.widthIn(min = 80.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = CameraTextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = CameraTextPrimary
        )
    }
}
