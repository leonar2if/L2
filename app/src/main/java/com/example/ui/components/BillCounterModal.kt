package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TransferBlue

@Composable
fun BillCounterModal(
    isBlind: Boolean, // True for Linked Worker Empresa, False for Particular
    onDismiss: () -> Unit,
    onSubmit: (declaredCash: Double, declaredTransfer: Double) -> Unit
) {
    val denominations = remember { listOf(10, 20, 50, 100, 200, 500, 1000) }
    val counts = remember { mutableStateMapOf<Int, Int>() }
    var transferAmountText by remember { mutableStateOf("0") }

    val calculatedCash = denominations.sumOf { (counts[it] ?: 0) * it }.toDouble()
    val transferAmount = transferAmountText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isBlind) "Arqueo Ciego de Caja" else "Arqueo de Caja",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isBlind) {
                    Text(
                        text = "Cuenta y registra los billetes físicos de tu gaveta.",
                        color = MutedGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(denominations) { denom ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Billetes de $$denom",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        val current = counts[denom] ?: 0
                                        if (current > 0) counts[denom] = current - 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                                ) { Text("-", color = PureWhite) }

                                Text(
                                    text = "${counts[denom] ?: 0}",
                                    color = NeonGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Button(
                                    onClick = {
                                        val current = counts[denom] ?: 0
                                        counts[denom] = current + 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                                ) { Text("+", color = PureWhite) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = transferAmountText,
                    onValueChange = { transferAmountText = it },
                    label = { Text("Monto Total por Transferencia ($)", color = MutedGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = TransferBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time calculated cash header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Efectivo Declarado:", color = PureWhite, fontSize = 13.sp)
                    Text("$${"%.2f".format(calculatedCash)}", color = NeonGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar", color = PureWhite) }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSubmit(calculatedCash, transferAmount) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar Cierre", color = PureBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
