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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.components.GoogleStyleSearchModal
import com.example.ui.components.LiveMetricsHeader
import com.example.ui.components.SalesFeedCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun CajaScreen(
    products: List<ProductEntity>,
    transactions: List<TransactionEntity>,
    canCreateProducts: Boolean,
    commissionRate: Double,
    showCommission: Boolean,
    onSaleRecorded: (ProductEntity, Double, String) -> Unit,
    onAddNewProductAndSell: (String, Double, String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAcabarDiaClick: () -> Unit
) {
    var showSaleModal by remember { mutableStateOf(false) }

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
            FloatingActionButton(
                onClick = { showSaleModal = true },
                containerColor = NeonGreen,
                contentColor = PureBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta", modifier = Modifier.padding(12.dp))
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

            // Shift Close Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ventas de Hoy (${transactions.size})", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)

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
                        Text("💡 Sin ventas aún hoy", color = MutedGray, fontSize = 16.sp)
                        Text("Presiona el botón + abajo a la derecha para registrar tu primera venta.", color = MutedGray, fontSize = 12.sp)
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

        if (showSaleModal) {
            GoogleStyleSearchModal(
                products = products,
                canCreateProducts = canCreateProducts,
                onDismiss = { showSaleModal = false },
                onSaleRecorded = onSaleRecorded,
                onAddNewProductAndSell = onAddNewProductAndSell
            )
        }
    }
}
