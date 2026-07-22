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

    suspend fun getOrCreateOpenShift(workerName: String): ShiftEntity {
        val openShift = db.shiftDao().getCurrentOpenShift()
        if (openShift != null) return openShift

        val newShift = ShiftEntity(
            id = UUID.randomUUID().toString(),
            worker_name = workerName,
            start_time = System.currentTimeMillis(),
            status = "OPEN"
        )
        db.shiftDao().insertShift(newShift)
        return newShift
    }

    suspend fun closeShift(shift: ShiftEntity) {
        db.shiftDao().updateShift(shift)
    }

    // Transactions
    fun getTransactionsForShift(shiftId: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsForShiftFlow(shiftId)

    val allTransactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactionsFlow()

    suspend fun recordSale(
        shiftId: String,
        productId: String,
        productName: String,
        quantity: Double,
        unitPrice: Double,
        paymentMethod: String
    ) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            shift_id = shiftId,
            product_id = productId,
            product_name = productName,
            type = "SALE",
            quantity = quantity,
            unit_price = unitPrice,
            payment_method = paymentMethod,
            timestamp = System.currentTimeMillis()
        )
        db.transactionDao().insertTransaction(transaction)

        // Update product stock and ranking
        val product = db.productDao().getProductById(productId)
        if (product != null) {
            val newStock = (product.current_stock - quantity).coerceAtLeast(0.0)
            val updated = product.copy(
                current_stock = newStock,
                sales_ranking_score = product.sales_ranking_score + quantity.toInt()
            )
            db.productDao().updateProduct(updated)
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
