package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.ConfigEntity
import com.example.data.entity.EmployeeEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShiftEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.SalePaymentEntity

data class SaleCartItem(
    val product: ProductEntity,
    val quantity: Double,
    val unitPrice: Double
)

data class PaymentSplit(
    val method: String, // CASH, TRANSFER
    val amount: Double
)

class PosRepository(private val db: AppDatabase) {

    // Config
    val configFlow: Flow<ConfigEntity?> = db.configDao().getConfigFlow()

    suspend fun getConfig(): ConfigEntity? = db.configDao().getConfig()

    suspend fun saveConfig(config: ConfigEntity) {
        db.configDao().insertOrUpdateConfig(config)
    }

    // Products
    val activeProductsFlow: Flow<List<ProductEntity>> = db.productDao().getAllActiveProductsFlow()
    val allProductsFlow: Flow<List<ProductEntity>> = db.productDao().getAllProductsFlow()

    suspend fun insertProduct(product: ProductEntity) {
        db.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        db.productDao().updateProduct(product)
    }

    suspend fun deleteProduct(id: String) {
        db.productDao().deleteProduct(id)
    }

    // Shifts
    val currentOpenShiftFlow: Flow<ShiftEntity?> = db.shiftDao().getCurrentOpenShiftFlow()
    val allShiftsFlow: Flow<List<ShiftEntity>> = db.shiftDao().getAllShiftsFlow()

    suspend fun getCurrentOpenShift(): ShiftEntity? {
        return db.shiftDao().getCurrentOpenShift()
    }

    suspend fun startShift(workerName: String, initialCash: Double): ShiftEntity {
        val openShift = db.shiftDao().getCurrentOpenShift()
        if (openShift != null) return openShift

        val newShift = ShiftEntity(
            id = UUID.randomUUID().toString(),
            worker_name = workerName,
            initial_cash = initialCash,
            start_time = System.currentTimeMillis(),
            status = "OPEN"
        )
        db.shiftDao().insertShift(newShift)
        return newShift
    }

    suspend fun closeShift(shift: ShiftEntity) {
        db.shiftDao().updateShift(shift)
    }

    // Transactions & Sales
    fun getTransactionsForShift(shiftId: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsForShiftFlow(shiftId)

    val allTransactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactionsFlow()

    suspend fun recordMultiItemSale(
        shiftId: String,
        items: List<SaleCartItem>,
        payments: List<PaymentSplit>
    ) {
        val totalSaleAmount = items.sumOf { it.quantity * it.unitPrice }
        val saleId = UUID.randomUUID().toString()

        val saleEntity = SaleEntity(
            id = saleId,
            shift_id = shiftId,
            timestamp = System.currentTimeMillis(),
            total_amount = totalSaleAmount,
            status = "COMPLETED"
        )
        db.saleDao().insertSale(saleEntity)

        val saleItems = items.map { cart ->
            SaleItemEntity(
                id = UUID.randomUUID().toString(),
                sale_id = saleId,
                product_id = cart.product.id,
                product_name = cart.product.name,
                quantity = cart.quantity,
                unit_price = cart.unitPrice,
                subtotal = cart.quantity * cart.unitPrice
            )
        }
        db.saleDao().insertSaleItems(saleItems)

        val salePayments = payments.map { pay ->
            SalePaymentEntity(
                id = UUID.randomUUID().toString(),
                sale_id = saleId,
                payment_method = pay.method,
                amount = pay.amount
            )
        }
        db.saleDao().insertSalePayments(salePayments)

        // Primary payment method for log display
        val primaryPaymentMethod = payments.firstOrNull()?.method ?: "CASH"

        // Update Stock & Ranking for sold products, insert Transaction logs
        for (item in items) {
            val prod = db.productDao().getProductById(item.product.id) ?: item.product
            val newStock = (prod.current_stock - item.quantity).coerceAtLeast(0.0)
            val updated = prod.copy(
                current_stock = newStock,
                sales_ranking_score = prod.sales_ranking_score + item.quantity.toInt()
            )
            db.productDao().updateProduct(updated)

            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                shift_id = shiftId,
                product_id = item.product.id,
                product_name = item.product.name,
                type = "SALE",
                quantity = item.quantity,
                unit_price = item.unitPrice,
                payment_method = primaryPaymentMethod,
                timestamp = System.currentTimeMillis()
            )
            db.transactionDao().insertTransaction(tx)
        }
    }

    suspend fun recordSale(
        shiftId: String,
        productId: String,
        productName: String,
        quantity: Double,
        unitPrice: Double,
        paymentMethod: String
    ) {
        val prod = db.productDao().getProductById(productId)
        if (prod != null) {
            val cartItem = SaleCartItem(product = prod, quantity = quantity, unitPrice = unitPrice)
            val payment = PaymentSplit(method = paymentMethod, amount = quantity * unitPrice)
            recordMultiItemSale(shiftId, listOf(cartItem), listOf(payment))
        }
    }

    suspend fun deleteTransaction(transactionId: String, productId: String, quantity: Double) {
        db.transactionDao().deleteTransaction(transactionId)
        // Restore stock
        val product = db.productDao().getProductById(productId)
        if (product != null) {
            val updated = product.copy(current_stock = product.current_stock + quantity)
            db.productDao().updateProduct(updated)
        }
    }

    suspend fun recordStockMovement(
        shiftId: String,
        productId: String,
        productName: String,
        type: String, // STOCK_ENTRY or STOCK_LOSS
        quantity: Double
    ) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            shift_id = shiftId,
            product_id = productId,
            product_name = productName,
            type = type,
            quantity = quantity,
            unit_price = 0.0,
            payment_method = "NONE",
            timestamp = System.currentTimeMillis()
        )
        db.transactionDao().insertTransaction(transaction)

        val product = db.productDao().getProductById(productId)
        if (product != null) {
            val newStock = if (type == "STOCK_ENTRY") {
                product.current_stock + quantity
            } else {
                (product.current_stock - quantity).coerceAtLeast(0.0)
            }
            val updated = product.copy(current_stock = newStock)
            db.productDao().updateProduct(updated)
        }
    }

    // Expenses
    fun getExpensesForShift(shiftId: String): Flow<List<ExpenseEntity>> =
        db.expenseDao().getExpensesForShiftFlow(shiftId)

    val allExpensesFlow: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpensesFlow()

    suspend fun addExpense(shiftId: String, description: String, amount: Double) {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            shift_id = shiftId,
            description = description,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        db.expenseDao().insertExpense(expense)
    }

    suspend fun deleteExpense(id: String) {
        db.expenseDao().deleteExpense(id)
    }

    // Employees (Master Owner)
    val employeesFlow: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployeesFlow()

    suspend fun insertEmployee(employee: EmployeeEntity) {
        db.employeeDao().insertEmployee(employee)
    }

    suspend fun deleteEmployee(uuid: String) {
        db.employeeDao().deleteEmployee(uuid)
    }

    // Catalog & Config Export / Import (.posync_cat)
    suspend fun generateCatalogExportJson(bossUuid: String, targetMode: String, canCreateProducts: Boolean): String {
        val root = JSONObject()
        root.put("boss_uuid", bossUuid)
        root.put("assigned_mode", targetMode)
        root.put("can_create_products", canCreateProducts)

        val productsArray = JSONArray()
        val products = db.productDao().getAllActiveProducts()
        for (p in products) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("cost_price", p.cost_price)
            pObj.put("sale_price", p.sale_price)
            pObj.put("current_stock", p.current_stock)
            pObj.put("sales_ranking_score", p.sales_ranking_score)
            productsArray.put(pObj)
        }
        root.put("products", productsArray)
        return root.toString()
    }

    suspend fun importCatalogProducts(productsList: List<ProductEntity>) {
        db.productDao().insertProducts(productsList)
    }
}
