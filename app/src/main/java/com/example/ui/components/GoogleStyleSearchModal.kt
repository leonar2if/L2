package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProductEntity
import com.example.data.repository.PaymentSplit
import com.example.data.repository.SaleCartItem
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TransferBlue

@Composable
fun GoogleStyleSearchModal(
    products: List<ProductEntity>,
    canCreateProducts: Boolean,
    onDismiss: () -> Unit,
    onMultiSaleRecorded: (List<SaleCartItem>, List<PaymentSplit>) -> Unit,
    onAddNewProductAndSell: (String, Double, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Cart State
    val cartItems = remember { mutableStateListOf<SaleCartItem>() }

    // Payment amounts
    var cashPaidText by remember { mutableStateOf("") }
    var transferPaidText by remember { mutableStateOf("") }

    // On the fly product state
    var showNewProductPriceInput by remember { mutableStateOf(false) }
    var newProductPriceText by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalCartAmount = cartItems.sumOf { it.quantity * it.unitPrice }
    val cashPaid = cashPaidText.toDoubleOrNull() ?: 0.0
    val transferPaid = transferPaidText.toDoubleOrNull() ?: 0.0
    val totalPaid = cashPaid + transferPaid

    val remainingAmount = (totalCartAmount - totalPaid).coerceAtLeast(0.0)
    val changeAmount = (totalPaid - totalCartAmount).coerceAtLeast(0.0)

    val isValidPayment = cartItems.isNotEmpty() && totalPaid >= totalCartAmount && totalCartAmount > 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nueva Venta Multi-Producto",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar (Google Keep Style)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar o agregar producto...", color = MutedGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selected Cart Items Section
                if (cartItems.isNotEmpty()) {
                    Text(
                        text = "Carrito de Compra (${cartItems.size} ítems)",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cartItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$${"%.2f".format(item.unitPrice)} c/u", color = MutedGray, fontSize = 11.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1.0) {
                                                cartItems[index] = item.copy(quantity = item.quantity - 1.0)
                                            } else {
                                                cartItems.removeAt(index)
                                            }
                                        }
                                    ) {
                                        Text("-", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "${item.quantity.toInt()}",
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            cartItems[index] = item.copy(quantity = item.quantity + 1.0)
                                        }
                                    ) {
                                        Text("+", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "$${"%.2f".format(item.quantity * item.unitPrice)}",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                                    )

                                    IconButton(onClick = { cartItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.ui.theme.CrimsonRed)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Scrollable Products Search List or On-The-Fly dialog
                if (showNewProductPriceInput) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Nuevo Producto Express", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Nombre: $searchQuery", color = MutedGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProductPriceText,
                            onValueChange = { newProductPriceText = it },
                            label = { Text("Precio de Venta ($)", color = MutedGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = { showNewProductPriceInput = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancelar", color = PureWhite) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val price = newProductPriceText.toDoubleOrNull() ?: 0.0
                                    if (price > 0) {
                                        onAddNewProductAndSell(searchQuery, price, "CASH")
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) { Text("Crear y Vender", color = PureBlack, fontWeight = FontWeight.Bold) }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredProducts) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
                                        if (existingIndex >= 0) {
                                            val existing = cartItems[existingIndex]
                                            cartItems[existingIndex] = existing.copy(quantity = existing.quantity + 1.0)
                                        } else {
                                            cartItems.add(SaleCartItem(product, 1.0, product.sale_price))
                                        }
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(product.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Stock: ${product.current_stock}", color = MutedGray, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$${"%.2f".format(product.sale_price)}",
                                        color = NeonGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = NeonGreen)
                                }
                            }
                        }

                        if (searchQuery.isNotBlank() && canCreateProducts) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                        .clickable { showNewProductPriceInput = true }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "➕ Crear \"$searchQuery\"",
                                        color = NeonGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Multi Payment Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Venta:", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("$${"%.2f".format(totalCartAmount)}", color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cashPaidText,
                            onValueChange = { cashPaidText = it },
                            label = { Text("💵 Efectivo ($)", color = MutedGray, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = NeonGreen
                            )
                        )

                        OutlinedTextField(
                            value = transferPaidText,
                            onValueChange = { transferPaidText = it },
                            label = { Text("💳 Transfer ($)", color = MutedGray, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite, unfocusedTextColor = PureWhite, focusedBorderColor = TransferBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (remainingAmount > 0) {
                            Text("Restante: $${"%.2f".format(remainingAmount)}", color = com.example.ui.theme.CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else if (changeAmount > 0) {
                            Text("Cambio a Entregar: $${"%.2f".format(changeAmount)}", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Pago Exacto Cubierto", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Pagado: $${"%.2f".format(totalPaid)}", color = PureWhite, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Final Confirm Button
                Button(
                    onClick = {
                        val payments = mutableListOf<PaymentSplit>()
                        if (cashPaid > 0) payments.add(PaymentSplit("CASH", cashPaid - changeAmount))
                        if (transferPaid > 0) payments.add(PaymentSplit("TRANSFER", transferPaid))
                        if (payments.isEmpty() && totalCartAmount > 0) {
                            payments.add(PaymentSplit("CASH", totalCartAmount))
                        }
                        onMultiSaleRecorded(cartItems.toList(), payments)
                        onDismiss()
                    },
                    enabled = isValidPayment,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        disabledContainerColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isValidPayment) "FINALIZAR VENTA ($${"%.2f".format(totalCartAmount)})" else "INGRESE PAGOS Y TOTAL",
                        color = if (isValidPayment) PureBlack else MutedGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

