package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ConfigEntity
import com.example.data.entity.EmployeeEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShiftEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.SalePaymentEntity

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalePayments(payments: List<SalePaymentEntity>)

    @Query("SELECT * FROM sales WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getSalesForShiftFlow(shiftId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItemEntity>

    @Query("SELECT * FROM sale_payments WHERE sale_id = :saleId")
    suspend fun getSalePayments(saleId: String): List<SalePaymentEntity>

    @Query("UPDATE sales SET status = 'VOIDED' WHERE id = :saleId")
    suspend fun voidSale(saleId: String)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config LIMIT 1")
    fun getConfigFlow(): Flow<ConfigEntity?>

    @Query("SELECT * FROM config LIMIT 1")
    suspend fun getConfig(): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: ConfigEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getAllActiveProducts(): List<ProductEntity>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY start_time DESC LIMIT 1")
    fun getCurrentOpenShiftFlow(): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY start_time DESC LIMIT 1")
    suspend fun getCurrentOpenShift(): ShiftEntity?

    @Query("SELECT * FROM shifts ORDER BY start_time DESC")
    fun getAllShiftsFlow(): Flow<List<ShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity)

    @Update
    suspend fun updateShift(shift: ShiftEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getTransactionsForShiftFlow(shiftId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getExpensesForShiftFlow(shiftId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployeesFlow(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE uuid = :uuid")
    suspend fun deleteEmployee(uuid: String)
}
