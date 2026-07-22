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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProductEntity
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
    onSaleRecorded: (ProductEntity, Double, String) -> Unit,
    onAddNewProductAndSell: (String, Double, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("CASH") } // CASH, TRANSFER, MIXED
    var selectedProductForQty by remember { mutableStateOf<ProductEntity?>(null) }
    var saleQuantity by remember { mutableDoubleStateOf(1.0) }

    // New product quick dialog state
    var showNewProductPriceInput by remember { mutableStateOf(false) }
    var newProductPriceText by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
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
                    Text(
                        text = "Registrar Venta",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = PureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Method Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedPaymentMethod == "CASH") NeonGreen else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedPaymentMethod = "CASH" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💵 Efectivo ($)",
                            color = if (selectedPaymentMethod == "CASH") PureBlack else PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedPaymentMethod == "TRANSFER") TransferBlue else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedPaymentMethod = "TRANSFER" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💳 Transfer (T)",
                            color = if (selectedPaymentMethod == "TRANSFER") PureBlack else PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar producto...", color = MutedGray) },
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

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedProductForQty != null) {
                    // Quantity Confirmation View
                    val prod = selectedProductForQty!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(prod.name, color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Precio Unitario: $${"%.2f".format(prod.sale_price)}", color = NeonGreen, fontSize = 14.sp)
                        Text("Stock Actual: ${prod.current_stock}", color = MutedGray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { if (saleQuantity > 1.0) saleQuantity -= 1.0 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) { Text("-", color = PureWhite, fontSize = 20.sp) }

                            Text(
                                text = "${saleQuantity.toInt()}",
                                color = PureWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            Button(
                                onClick = { saleQuantity += 1.0 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) { Text("+", color = PureWhite, fontSize = 20.sp) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { selectedProductForQty = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = PureWhite)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSaleRecorded(prod, saleQuantity, selectedPaymentMethod)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirmar ($${"%.2f".format(prod.sale_price * saleQuantity)})", color = PureBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (showNewProductPriceInput) {
                    // Create New Product On The Fly Price Dialog
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Agregar nuevo producto", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Nombre: $searchQuery", color = MutedGray, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Button(
                                onClick = { showNewProductPriceInput = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("Atrás", color = PureWhite) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val price = newProductPriceText.toDoubleOrNull() ?: 0.0
                                    if (price > 0) {
                                        onAddNewProductAndSell(searchQuery, price, selectedPaymentMethod)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) { Text("Guardar y Vender", color = PureBlack, fontWeight = FontWeight.Bold) }
                        }
                    }
                } else {
                    // Filtered Product List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedProductForQty = product
                                        saleQuantity = 1.0
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(product.name, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Stock: ${product.current_stock}", color = MutedGray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "$${"%.2f".format(product.sale_price)}",
                                    color = NeonGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Smart Match: If searching and option to add is allowed
                        if (searchQuery.isNotBlank() && canCreateProducts) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                        .clickable {
                                            showNewProductPriceInput = true
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "➕ Agregar \"$searchQuery\" como nuevo producto",
                                        color = NeonGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
