package com.pablo.paper.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.common.LiquidGlassDialog
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearPageDialog(
    pageNumber: Int,
    isDarkMode: Boolean = false,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "¿Borrar anotaciones?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = textColor
                    )
                    Text(
                        text = "Página $pageNumber",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecColor
                    )
                }
            }

            Text(
                text = "Se eliminarán todos los trazos, dibujos, formas y notas de esta página. Podrás deshacer la acción si lo necesitas.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        onConfirm()
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3B30),
                        contentColor = Color.White
                    )
                ) {
                    Text("Borrar Todo")
                }
            }
        }
    }
}
