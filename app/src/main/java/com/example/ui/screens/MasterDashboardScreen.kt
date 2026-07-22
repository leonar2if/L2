package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.EmployeeEntity
import com.example.data.entity.ProductEntity
import com.example.ui.ForensicAuditData
import com.example.ui.components.QrCodeDisplay
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MasterDashboardScreen(
    bossUuid: String,
    products: List<ProductEntity>,
    employees: List<EmployeeEntity>,
    forensicAudit: ForensicAuditData?,
    onExportCatalogClick: (targetMode: String, canCreateProducts: Boolean) -> Unit,
    onAddEmployeeClick: (name: String, assignedMode: String, canCreateProducts: Boolean, commission: Double) -> Unit,
    onDeleteEmployeeClick: (uuid: String) -> Unit,
    onClearAuditResult: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Analytics, 1: HR & QR, 2: Auditoría (.posync_day)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Navigation for Master Owner
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = PureWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📊 Analytics", color = if (selectedTab == 0) NeonGreen else MutedGray, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("👥 Empleados & QR", color = if (selectedTab == 1) NeonGreen else MutedGray, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("📬 Buzón Auditoría", color = if (selectedTab == 2) NeonGreen else MutedGray, fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> AnalyticsTab(products = products)
            1 -> HrEmployeesTab(
                bossUuid = bossUuid,
                employees = employees,
                onExportCatalogClick = onExportCatalogClick,
                onAddEmployeeClick = onAddEmployeeClick,
                onDeleteEmployeeClick = onDeleteEmployeeClick
            )
            2 -> AuditMailboxTab(
                forensicAudit = forensicAudit,
                onClearAuditResult = onClearAuditResult
            )
        }
    }
}

@Composable
private fun AnalyticsTab(products: List<ProductEntity>) {
    val totalInventoryValue = remember(products) { products.sumOf { it.current_stock * it.sale_price } }
    val totalCostValue = remember(products) { products.sumOf { it.current_stock * it.cost_price } }
    val projectedProfit = totalInventoryValue - totalCostValue
    val lowStockCount = remember(products) { products.count { it.current_stock <= 5.0 } }
    val topProducts = remember(products) { products.sortedByDescending { it.sales_ranking_score }.take(5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Panel Gerencial Global", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Supervisión de Almacén y Margen de Ganancia", color = MutedGray, fontSize = 12.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Valor del Inventario en Bodega", color = MutedGray, fontSize = 13.sp)
                    Text("$${"%.2f".format(totalInventoryValue)}", color = NeonGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Costo Total Invertido", color = MutedGray, fontSize = 11.sp)
                            Text("$${"%.2f".format(totalCostValue)}", color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Ganancia Neta Proyectada", color = MutedGray, fontSize = 11.sp)
                            Text("$${"%.2f".format(projectedProfit)}", color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (lowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️ Alertas de Stock Mínimo", color = CrimsonRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("$lowStockCount Productos", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Productos Más Vendidos (Ranking)", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(topProducts) { p ->
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
                        Text(p.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Stock actual: ${p.current_stock.toInt()} | Costo: $${p.cost_price}", color = MutedGray, fontSize = 12.sp)
                    }
                    Text("${p.sales_ranking_score} vendid.", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HrEmployeesTab(
    bossUuid: String,
    employees: List<EmployeeEntity>,
    onExportCatalogClick: (targetMode: String, canCreateProducts: Boolean) -> Unit,
    onAddEmployeeClick: (name: String, assignedMode: String, canCreateProducts: Boolean, commission: Double) -> Unit,
    onDeleteEmployeeClick: (uuid: String) -> Unit
) {
    var showAddEmployeeModal by remember { mutableStateOf(false) }
    var empName by remember { mutableStateOf("") }
    var empMode by remember { mutableStateOf("SALES_COUNT") }
    var empCanCreate by remember { mutableStateOf(false) }
    var empCommissionText by remember { mutableStateOf("0") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Código QR Maestro para Vinculación", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("El empleado debe escanear este QR durante la primera apertura de su app.", color = MutedGray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                QrCodeDisplay(content = bossUuid, size = 180.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("UUID Jefe: $bossUuid", color = MutedGray, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
        }

        item {
            Button(
                onClick = { onExportCatalogClick("SALES_COUNT", false) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("📤 Generar & Compartir Configuración (.posync_cat)", color = PureBlack, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Plantilla de Empleados", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showAddEmployeeModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) { Text("➕ Crear Perfil", color = NeonGreen) }
            }
        }

        items(employees) { emp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(emp.name, color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Modo: ${if (emp.assigned_mode == "SALES_COUNT") "Caja Registradora" else "Diferencia Inventario"}",
                            color = MutedGray,
                            fontSize = 12.sp
                        )
                        Text(
                            "Permiso Crear Productos: ${if (emp.can_create_products) "SÍ" else "NO"}",
                            color = MutedGray,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { onDeleteEmployeeClick(emp.uuid) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) { Text("Eliminar", color = PureWhite, fontSize = 11.sp) }
                }
            }
        }
    }

    if (showAddEmployeeModal) {
        Dialog(onDismissRequest = { showAddEmployeeModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Crear Perfil de Empleado", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = empName,
                        onValueChange = { empName = it },
                        label = { Text("Nombre del Empleado", color = MutedGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Modo Asignado:", color = PureWhite, fontSize = 12.sp)
                    Row {
                        Button(
                            onClick = { empMode = "SALES_COUNT" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (empMode == "SALES_COUNT") NeonGreen else DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Modo Caja", color = if (empMode == "SALES_COUNT") PureBlack else PureWhite) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { empMode = "STOCK_DIFFERENCE" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (empMode == "STOCK_DIFFERENCE") NeonGreen else DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Modo Stock", color = if (empMode == "STOCK_DIFFERENCE") PureBlack else PureWhite) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = empCommissionText,
                        onValueChange = { empCommissionText = it },
                        label = { Text("% Comisión sobre Ventas", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = { showAddEmployeeModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar", color = PureWhite) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val comm = empCommissionText.toDoubleOrNull() ?: 0.0
                                if (empName.isNotBlank()) {
                                    onAddEmployeeClick(empName, empMode, empCanCreate, comm)
                                    showAddEmployeeModal = false
                                    empName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.weight(1f)
                        ) { Text("Guardar", color = PureBlack, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditMailboxTab(
    forensicAudit: ForensicAuditData?,
    onClearAuditResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Buzón de Auditoría Forense (.posync_day)", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Al abrir un archivo .posync_day recibido por WhatsApp, se realiza el análisis antifraude aquí.", color = MutedGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (forensicAudit == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📥 Toca en WhatsApp el archivo .posync_day enviado por tu empleado para analizar su cierre ciego aquí.",
                    color = MutedGray,
                    fontSize = 14.sp
                )
            }
        } else {
            val dateStr = remember(forensicAudit.shiftEnd) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(forensicAudit.shiftEnd))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Análisis Forense de Cierre", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Empleado: ${forensicAudit.workerName} | Fecha: $dateStr", color = MutedGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dinero Declarado:", color = PureWhite, fontSize = 14.sp)
                        Text("$${"%.2f".format(forensicAudit.declaredCash)}", color = PureWhite, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dinero Calculado por Sistema:", color = PureWhite, fontSize = 14.sp)
                        Text("$${"%.2f".format(forensicAudit.systemExpectedCash)}", color = PureWhite, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (forensicAudit.isSurplus) NeonGreen.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (forensicAudit.isSurplus) "✅ Sobrante en Gaveta:" else "🚨 FALTANTE EN GAVETA:",
                                color = if (forensicAudit.isSurplus) NeonGreen else CrimsonRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "$${"%.2f".format(forensicAudit.cashDifference)}",
                                color = if (forensicAudit.isSurplus) NeonGreen else CrimsonRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClearAuditResult,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Limpiar Vista", color = PureWhite) }
                }
            }
        }
    }
}
