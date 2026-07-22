package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val device_id: String = UUID.randomUUID().toString(),
    val device_uuid: String = UUID.randomUUID().toString(),
    val android_id: String = "",
    val public_qr: String = UUID.randomUUID().toString(),
    val active_mode: String = "SOLO_OWNER",
    val active_plan: String = "PARTICULAR",
    val is_licensed: Boolean = false,
    val sync_boss_uuid: String? = null,
    val operation_system: String = "SALES_COUNT",
    val show_stock_tab: Boolean = true,
    val worker_name: String = "Usuario",
    val commission_rate: Double = 0.0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cost_price: Double = 0.0,
    val sale_price: Double,
    val initial_stock: Double = 0.0,
    val current_stock: Double = 0.0,
    val final_stock: Double = 0.0,
    val sales_ranking_score: Int = 0,
    val is_active: Boolean = true
)

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val worker_name: String = "",
    val initial_cash: Double = 0.0,
    val start_time: Long = System.currentTimeMillis(),
    val end_time: Long? = null,
    val declared_cash: Double = 0.0,
    val declared_transfer: Double = 0.0,
    val system_expected_cash: Double = 0.0,
    val total_commission: Double = 0.0,
    val net_profit: Double = 0.0,
    val status: String = "OPEN"
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val shift_id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val total_amount: Double,
    val status: String = "COMPLETED" // COMPLETED, VOIDED
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sale_id: String,
    val product_id: String,
    val product_name: String,
    val quantity: Double,
    val unit_price: Double,
    val subtotal: Double
)

@Entity(tableName = "sale_payments")
data class SalePaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sale_id: String,
    val payment_method: String, // CASH, TRANSFER
    val amount: Double
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val shift_id: String,
    val product_id: String,
    val product_name: String = "",
    val type: String = "SALE", // SALE, STOCK_ENTRY, STOCK_LOSS
    val quantity: Double,
    val unit_price: Double,
    val payment_method: String = "CASH",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val shift_id: String,
    val description: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val assigned_mode: String = "SALES_COUNT",
    val can_create_products: Boolean = false,
    val commission_rate: Double = 0.0,
    val assigned_inventory: String = "[]" // JSON array string
)

