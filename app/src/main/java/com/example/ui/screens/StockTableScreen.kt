package com.example.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.entity.ProductEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun StockTableScreen(
    products: List<ProductEntity>,
    canCreateProducts: Boolean,
    onLoadPreviousDayClick: () -> Unit,
    onAddProductClick: (name: String, cost: Double, sale: Double, stock: Double) -> Unit,
    onRecordStockEntry: (ProductEntity, Double) -> Unit,
    onRecordStockLoss: (ProductEntity, Double) -> Unit,
    onSaveFinalStockCount: (ProductEntity, Double) -> Unit,
    onAcabarDiaClick: () -> Unit
) {
    var selectedProductForMovement by remember { mutableStateOf<ProductEntity?>(null) }
    var movementType by remember { mutableStateOf("ENTRY") } // ENTRY or LOSS
    var movementQtyText by remember { mutableStateOf("") }

    var showAddProductModal by remember { mutableStateOf(false) }
    var newProdName by remember { mutableStateOf("") }
    var newProdCost by remember { mutableStateOf("") }
    var newProdSale by remember { mutableStateOf("") }
    var newProdStock by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Top Giant Action Button
        Button(
            onClick = onLoadPreviousDayClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, NeonGreen, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🔄 Cargar del Día Anterior (Inicial = Final Ayer)", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (canCreateProducts) {
                Button(
                    onClick = { showAddProductModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ Nuevo Prod.", color = PureBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = onAcabarDiaClick,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                modifier = Modifier.weight(1f)
            ) {
                Text("🔒 Acabar Día", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tabla de Control de Inventario", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Venta Deductiva = Inicial + Entradas - Bajas - Final", color = MutedGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Inventory Table Feed
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(product.name, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("$${"%.2f".format(product.sale_price)}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Inicial", color = MutedGray, fontSize = 10.sp)
                                Text("${product.initial_stock.toInt()}", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Actual", color = MutedGray, fontSize = 10.sp)
                                Text("${product.current_stock.toInt()}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Conteo Final", color = MutedGray, fontSize = 10.sp)
                                Text("${product.final_stock.toInt()}", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    selectedProductForMovement = product
                                    movementType = "ENTRY"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                modifier = Modifier.height(36.dp)
                            ) { Text("📥 Entrada", color = NeonGreen, fontSize = 11.sp) }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    selectedProductForMovement = product
                                    movementType = "LOSS"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                modifier = Modifier.height(36.dp)
                            ) { Text("📤 Baja", color = CrimsonRed, fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
    }

    // Movement Modal (Entrada/Baja)
    if (selectedProductForMovement != null) {
        val prod = selectedProductForMovement!!
        Dialog(onDismissRequest = { selectedProductForMovement = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (movementType == "ENTRY") "📥 Registrar Entrada de Stock" else "📤 Registrar Baja / Merma",
                        color = if (movementType == "ENTRY") NeonGreen else CrimsonRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Producto: ${prod.name}", color = PureWhite, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = movementQtyText,
                        onValueChange = { movementQtyText = it },
                        label = { Text("Cantidad", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = NeonGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = { selectedProductForMovement = null },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar", color = PureWhite) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val qty = movementQtyText.toDoubleOrNull() ?: 0.0
                                if (qty > 0) {
                                    if (movementType == "ENTRY") onRecordStockEntry(prod, qty)
                                    else onRecordStockLoss(prod, qty)
                                    selectedProductForMovement = null
                                    movementQtyText = ""
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

    // New Product Modal
    if (showAddProductModal) {
        Dialog(onDismissRequest = { showAddProductModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("➕ Agregar Nuevo Producto", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("Nombre del Producto", color = MutedGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newProdCost,
                        onValueChange = { newProdCost = it },
                        label = { Text("Precio de Compra/Costo ($)", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newProdSale,
                        onValueChange = { newProdSale = it },
                        label = { Text("Precio de Venta ($)", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newProdStock,
                        onValueChange = { newProdStock = it },
                        label = { Text("Stock Inicial", color = MutedGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = { showAddProductModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar", color = PureWhite) }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val cost = newProdCost.toDoubleOrNull() ?: 0.0
                                val sale = newProdSale.toDoubleOrNull() ?: 0.0
                                val stock = newProdStock.toDoubleOrNull() ?: 0.0
                                if (newProdName.isNotBlank() && sale > 0) {
                                    onAddProductClick(newProdName, cost, sale, stock)
                                    showAddProductModal = false
                                    newProdName = ""
                                    newProdCost = ""
                                    newProdSale = ""
                                    newProdStock = ""
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
