package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.entity.ConfigEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.enums.OperationMode
import com.example.data.enums.UserRole
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun SettingsHubScreen(
    config: ConfigEntity?,
    expenses: List<ExpenseEntity>,
    onModeChanged: (OperationMode) -> Unit,
    onToggleStockTab: (Boolean) -> Unit,
    onUpdateCommissionRate: (Double) -> Unit,
    onUpdateWorkerName: (String) -> Unit,
    onAddExpenseClick: (description: String, amount: Double) -> Unit,
    onDeleteExpenseClick: (id: String) -> Unit,
    onCloseAppClick: () -> Unit
) {
    val planName = config?.active_plan ?: "PARTICULAR"
    val roleName = when (config?.active_mode) {
        UserRole.SOLO_OWNER.name -> UserRole.SOLO_OWNER.displayName
        UserRole.INDEPENDENT_WORKER.name -> UserRole.INDEPENDENT_WORKER.displayName
        UserRole.MASTER_OWNER.name -> UserRole.MASTER_OWNER.displayName
        UserRole.LINKED_WORKER.name -> UserRole.LINKED_WORKER.displayName
        else -> config?.active_mode ?: "UNCONFIGURED"
    }

    val isLinkedWorker = config?.active_mode == UserRole.LINKED_WORKER.name
    var nameText by remember(config) { mutableStateOf(config?.worker_name ?: "Usuario") }
    var commissionText by remember(config) { mutableStateOf((config?.commission_rate ?: 0.0).toString()) }

    var showExpenseModal by remember { mutableStateOf(false) }
    var expDesc by remember { mutableStateOf("") }
    var expAmountText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // MANDATORY SETTINGS HEADER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonGreen, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Plan Activo: $planName - $roleName",
                    color = NeonGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID Dispositivo: ${config?.device_id?.take(12)}...",
                    color = MutedGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLinkedWorker) {
            // BLOCKED SETTINGS FOR LINKED WORKER
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Ajustes Bloqueados por la Empresa",
                        color = CrimsonRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tus precios, productos y modo de trabajo son dictados por el archivo .posync_cat enviado por tu jefe. No puedes modificar la configuración.",
                        color = MutedGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onCloseAppClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión / Salir", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // PARTICULAR & MASTER OWNER SETTINGS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Configuración de Perfil", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Nombre del Trabajador", color = MutedGray) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onUpdateWorkerName(nameText) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) { Text("Guardar", color = PureBlack, fontWeight = FontWeight.Bold) }
                    }
                }

                if (config?.active_mode != UserRole.MASTER_OWNER.name) {
                    item {
                        Text("Modo de Trabajo Operativo", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Alterna entre Modo Caja y Modo Diferencia Inventario.", color = MutedGray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Button(
                                onClick = { onModeChanged(OperationMode.SALES_COUNT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (config?.operation_system == OperationMode.SALES_COUNT.name) NeonGreen else DarkSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Modo Caja",
                                    color = if (config?.operation_system == OperationMode.SALES_COUNT.name) PureBlack else PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onModeChanged(OperationMode.STOCK_DIFFERENCE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (config?.operation_system == OperationMode.STOCK_DIFFERENCE.name) NeonGreen else DarkSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Modo Stock",
                                    color = if (config?.operation_system == OperationMode.STOCK_DIFFERENCE.name) PureBlack else PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mostrar Pestaña Stock en Modo Caja", color = PureWhite, fontSize = 14.sp)
                                    Text("Permite cambiar entre Caja y Stock.", color = MutedGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = config?.show_stock_tab == true,
                                    onCheckedChange = { onToggleStockTab(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                )
                            }
                        }
                    }

                    item {
                        Text("Porcentaje de Comisión sobre Ventas", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = commissionText,
                                onValueChange = { commissionText = it },
                                label = { Text("% Comisión", color = MutedGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val rate = commissionText.toDoubleOrNull() ?: 0.0
                                    onUpdateCommissionRate(rate)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) { Text("Actualizar", color = PureBlack, fontWeight = FontWeight.Bold) }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Registro de Gastos y Egresos", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showExpenseModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) { Text("➕ Egreso", color = NeonGreen) }
                        }
                    }

                    items(expenses) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(exp.description, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("-$${"%.2f".format(exp.amount)}", color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onDeleteExpenseClick(exp.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                                ) { Text("Borrar", color = PureWhite, fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExpenseModal) {
        Dialog(onDismissRequest = { showExpenseModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrar Egreso/Gasto Local", color = CrimsonRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = expDesc,
                        onValueChange = { expDesc = it },
                        label = { Text("Descripción del Gasto", color = MutedGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = CrimsonRed)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expAmountText,
                        onValueChange = { expAmountText = it },
                        label = { Text("Monto ($)", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = CrimsonRed)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = { showExpenseModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar", color = PureWhite) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val amt = expAmountText.toDoubleOrNull() ?: 0.0
                                if (expDesc.isNotBlank() && amt > 0) {
                                    onAddExpenseClick(expDesc, amt)
                                    showExpenseModal = false
                                    expDesc = ""
                                    expAmountText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.weight(1f)
                        ) { Text("Guardar", color = PureWhite, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
