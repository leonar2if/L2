package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShiftEntity
import com.example.data.entity.TransactionEntity
import com.example.data.repository.PaymentSplit
import com.example.data.repository.SaleCartItem
import com.example.ui.components.GoogleStyleSearchModal
import com.example.ui.components.LiveMetricsHeader
import com.example.ui.components.SalesFeedCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun CajaScreen(
    currentShift: ShiftEntity?,
    products: List<ProductEntity>,
    transactions: List<TransactionEntity>,
    canCreateProducts: Boolean,
    commissionRate: Double,
    showCommission: Boolean,
    onStartShiftClick: (Double) -> Unit,
    onMultiSaleRecorded: (List<SaleCartItem>, List<PaymentSplit>) -> Unit,
    onAddNewProductAndSell: (String, Double, String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAcabarDiaClick: () -> Unit
) {
    var showSaleModal by remember { mutableStateOf(false) }
    var initialCashText by remember { mutableStateOf("") }

    val isShiftOpen = currentShift != null && currentShift.status == "OPEN"

    val cashTotal = remember(transactions) {
        transactions.filter { it.type == "SALE" && it.payment_method == "CASH" }
            .sumOf { it.quantity * it.unit_price }
    }

    val transferTotal = remember(transactions) {
        transactions.filter { it.type == "SALE" && it.payment_method == "TRANSFER" }
            .sumOf { it.quantity * it.unit_price }
    }

    val totalSales = cashTotal + transferTotal
    val commissionTotal = totalSales * (commissionRate / 100.0)

    Scaffold(
        floatingActionButton = {
            if (isShiftOpen) {
                FloatingActionButton(
                    onClick = { showSaleModal = true },
                    containerColor = NeonGreen,
                    contentColor = PureBlack,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Venta", modifier = Modifier.padding(12.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            // Live Metrics Header
            LiveMetricsHeader(
                totalCash = cashTotal,
                totalTransfer = transferTotal,
                commission = commissionTotal,
                showCommission = showCommission
            )

            if (!isShiftOpen) {
                // Iniciar Día Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "☀️ Sin Turno Activo",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingresa la caja inicial para abrir el día de ventas",
                                color = MutedGray,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = initialCashText,
                                onValueChange = { initialCashText = it },
                                label = { Text("Efectivo Inicial en Caja ($)", color = MutedGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PureWhite,
                                    unfocusedTextColor = PureWhite,
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val cash = initialCashText.toDoubleOrNull() ?: 0.0
                                    onStartShiftClick(cash)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PureBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("INICIAR DÍA", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                // Shift Close Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ventas del Turno (${transactions.size})", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Caja Inicial: $${"%.2f".format(currentShift?.initial_cash ?: 0.0)}", color = MutedGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onAcabarDiaClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PureBlack)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Acabar Día", color = PureBlack, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💡 Sin ventas aún en este turno", color = MutedGray, fontSize = 16.sp)
                            Text("Presiona el botón + abajo a la derecha para registrar tus ventas.", color = MutedGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            SalesFeedCard(
                                transaction = tx,
                                onDelete = { onDeleteTransaction(tx) }
                            )
                        }
                    }
                }
            }
        }

        if (showSaleModal) {
            GoogleStyleSearchModal(
                products = products,
                canCreateProducts = canCreateProducts,
                onDismiss = { showSaleModal = false },
                onMultiSaleRecorded = onMultiSaleRecorded,
                onAddNewProductAndSell = onAddNewProductAndSell
            )
        }
    }
}

