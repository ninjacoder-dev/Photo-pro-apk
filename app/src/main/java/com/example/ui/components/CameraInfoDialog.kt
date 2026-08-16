package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CameraInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CameraDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CameraAmberPrimary
                )
                Text(
                    text = "Guide & Fonctionnalités Pro",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CameraTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InfoSection(
                    title = "📸 Résolution 12MP vs 50MP",
                    text = "• 12 MP (Pixel Binning 4-en-1) : Regroupe 4 photosites en un seul grand pixel pour une sensibilité maximale en faible luminosité et une capture instantanée.\n• 50 MP (Plein Capteur) : Débloque la matrice complète du capteur pour des détails ultra-fins et des recadrages sans perte."
                )

                InfoSection(
                    title = "🔍 Zoom Physique vs Zoom Numérique",
                    text = "• Zoom Physique (Optique) : Utilise la lentille téléobjectif matérielle sans aucune perte de qualité (jusqu'au Zoom Physique Max indiqué).\n• Zoom Numérique (Crop) : Agrandit l'image par recadrage numérique et interpolation jusqu'au Zoom Numérique Max.\n• L'application affiche en temps réel la décomposition exacte entre la part optique et la part numérique."
                )

                InfoSection(
                    title = "⚙️ Paramètres Manuels Experts",
                    text = "• ISO (50 à 6400) : Sensibilité lumineuse du capteur.\n• Vitesse (1/8000s à 30s) : Temps de captation de la lumière (figer le mouvement instantané ou pose longue jusqu'à 30 secondes).\n• Balance des Blancs (Kelvin) : Température des couleurs de 2000K (chaud) à 10000K (froid).\n• Focus Manuel (MF) : Réglage précis de la distance de mise au point de macro à l'infini."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CameraAmberPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("info_dialog_confirm_button")
            ) {
                Text(
                    text = "J'ai compris",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    )
}

@Composable
private fun InfoSection(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CameraDarkSurfaceVariant.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = CameraCyanAccent
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = CameraTextSecondary
        )
    }
}
