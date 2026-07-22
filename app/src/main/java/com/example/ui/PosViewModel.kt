package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.DrmEngine
import com.example.data.crypto.PosyncCrypto
import com.example.data.database.AppDatabase
import com.example.data.entity.ConfigEntity
import com.example.data.entity.EmployeeEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShiftEntity
import com.example.data.entity.TransactionEntity
import com.example.data.enums.OperationMode
import com.example.data.enums.ShiftStatus
import com.example.data.enums.UserPlan
import com.example.data.enums.UserRole
import com.example.data.repository.PaymentSplit
import com.example.data.repository.PosRepository
import com.example.data.repository.SaleCartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class ForensicAuditData(
    val workerName: String,
    val shiftStart: Long,
    val shiftEnd: Long,
    val declaredCash: Double,
    val declaredTransfer: Double,
    val systemExpectedCash: Double,
    val cashDifference: Double, // Declared - Expected
    val isSurplus: Boolean,
    val totalSales: Double,
    val transactionsCount: Int,
    val rawJson: String
)

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = PosRepository(db)

    val configState: StateFlow<ConfigEntity?> = repository.configFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeProductsState: StateFlow<List<ProductEntity>> = repository.activeProductsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentShiftState: StateFlow<ShiftEntity?> = repository.currentOpenShiftFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val employeesState: StateFlow<List<EmployeeEntity>> = repository.employeesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current shift transactions
    private val _shiftTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val shiftTransactions: StateFlow<List<TransactionEntity>> = _shiftTransactions.asStateFlow()

    // Current shift expenses
    private val _shiftExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val shiftExpenses: StateFlow<List<ExpenseEntity>> = _shiftExpenses.asStateFlow()

    // UI Toast or Banner message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Keygen Modal state
    private val _showKeygenModal = MutableStateFlow(false)
    val showKeygenModal: StateFlow<Boolean> = _showKeygenModal.asStateFlow()

    // Forensic Audit result state for Master Owner
    private val _forensicAudit = MutableStateFlow<ForensicAuditData?>(null)
    val forensicAudit: StateFlow<ForensicAuditData?> = _forensicAudit.asStateFlow()

    // Generated export file URI for sharing via Intent / WhatsApp
    private val _shareFileUri = MutableStateFlow<Uri?>(null)
    val shareFileUri: StateFlow<Uri?> = _shareFileUri.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure config exists or initialize default
            val existing = repository.getConfig()
            if (existing == null) {
                val androidId = getAndroidId()
                val newConfig = ConfigEntity(
                    device_id = androidId,
                    device_uuid = UUID.randomUUID().toString(),
                    android_id = androidId,
                    public_qr = UUID.randomUUID().toString(),
                    active_mode = "UNCONFIGURED",
                    active_plan = "UNCONFIGURED",
                    is_licensed = false,
                    sync_boss_uuid = null,
                    operation_system = "SALES_COUNT",
                    show_stock_tab = true,
                    worker_name = "Usuario"
                )
                repository.saveConfig(newConfig)
            }
            
            // Seed initial demo products if empty
            val products = repository.allProductsFlow.firstOrNull() ?: emptyList()
            if (products.isEmpty()) {
                seedInitialDemoProducts()
            }
        }

        // Collect current shift items
        viewModelScope.launch {
            repository.currentOpenShiftFlow.collect { shift ->
                if (shift != null) {
                    repository.getTransactionsForShift(shift.id).collect { txs ->
                        _shiftTransactions.value = txs
                    }
                } else {
                    _shiftTransactions.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            repository.currentOpenShiftFlow.collect { shift ->
                if (shift != null) {
                    repository.getExpensesForShift(shift.id).collect { exps ->
                        _shiftExpenses.value = exps
                    }
                } else {
                    _shiftExpenses.value = emptyList()
                }
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearShareFileUri() {
        _shareFileUri.value = null
    }

    fun getAndroidId(): String {
        return try {
            Settings.Secure.getString(
                getApplication<Application>().contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: UUID.randomUUID().toString().take(8)
        } catch (e: Exception) {
            UUID.randomUUID().toString().take(8)
        }
    }

    private suspend fun seedInitialDemoProducts() {
        val sampleList = listOf(
            ProductEntity(name = "Coca Cola 600ml", cost_price = 12.0, sale_price = 18.0, initial_stock = 50.0, current_stock = 45.0, is_active = true),
            ProductEntity(name = "Sabritas Sal 45g", cost_price = 10.0, sale_price = 16.0, initial_stock = 30.0, current_stock = 28.0, is_active = true),
            ProductEntity(name = "Agua Ciel 1L", cost_price = 6.0, sale_price = 12.0, initial_stock = 40.0, current_stock = 38.0, is_active = true),
            ProductEntity(name = "Café Americano 250ml", cost_price = 8.0, sale_price = 25.0, initial_stock = 100.0, current_stock = 92.0, is_active = true),
            ProductEntity(name = "Pan Dulce Artesanal", cost_price = 5.0, sale_price = 10.0, initial_stock = 20.0, current_stock = 15.0, is_active = true)
        )
        for (p in sampleList) {
            repository.insertProduct(p)
        }
    }

    // Explicitly Start Shift (Iniciar Día)
    fun startShift(initialCash: Double) {
        viewModelScope.launch {
            val cfg = repository.getConfig()
            repository.startShift(cfg?.worker_name ?: "Usuario", initialCash)
            showToast("Día iniciado con $${"%.2f".format(initialCash)} en caja")
        }
    }

    // Step 1: Complete Onboarding Plan & Role selection
    fun completeOnboarding(role: UserRole, plan: UserPlan) {
        viewModelScope.launch {
            val current = repository.getConfig() ?: ConfigEntity(device_id = getAndroidId())
            val isFreeLinked = (role == UserRole.LINKED_WORKER)
            val updated = current.copy(
                active_mode = role.name,
                active_plan = if (isFreeLinked) UserPlan.FREE_LINKED.name else plan.name,
                is_licensed = isFreeLinked, // Free linked worker doesn't require payment DRM
                operation_system = if (role == UserRole.MASTER_OWNER) "ANALYTICS_ONLY" else "SALES_COUNT",
                show_stock_tab = (role != UserRole.INDEPENDENT_WORKER)
            )
            repository.saveConfig(updated)
        }
    }

    // DRM Activation
    fun openKeygenModal() {
        _showKeygenModal.value = true
    }

    fun closeKeygenModal() {
        _showKeygenModal.value = false
    }

    fun activateDrm(pin: String): Boolean {
        val cfg = configState.value ?: return false
        val challenge = DrmEngine.generateChallengeText(cfg.device_id, cfg.active_plan)
        val isValid = DrmEngine.validatePin(challenge, pin)
        if (isValid) {
            viewModelScope.launch {
                val updated = cfg.copy(is_licensed = true)
                repository.saveConfig(updated)
                showToast("¡Licencia activada con éxito!")
            }
            return true
        } else {
            showToast("PIN de activación incorrecto")
            return false
        }
    }

    // Linked Worker Binding via QR Scan
    fun bindLinkedWorkerToBoss(bossUuid: String) {
        viewModelScope.launch {
            val cfg = configState.value ?: return@launch
            val updated = cfg.copy(
                active_mode = UserRole.LINKED_WORKER.name,
                active_plan = UserPlan.FREE_LINKED.name,
                is_licensed = true,
                sync_boss_uuid = bossUuid
            )
            repository.saveConfig(updated)
            showToast("Vinculado correctamente al jefe UUID: $bossUuid")
        }
    }

    // Sales Execution
    fun registerMultiItemSale(items: List<SaleCartItem>, payments: List<PaymentSplit>) {
        viewModelScope.launch {
            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar una venta")
                return@launch
            }
            repository.recordMultiItemSale(shift.id, items, payments)
            showToast("Venta de ${items.size} producto(s) registrada")
        }
    }

    fun registerSale(productId: String, productName: String, quantity: Double, unitPrice: Double, paymentMethod: String) {
        viewModelScope.launch {
            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar una venta")
                return@launch
            }
            repository.recordSale(
                shiftId = shift.id,
                productId = productId,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPrice,
                paymentMethod = paymentMethod
            )
            showToast("Venta registrada")
        }
    }

    // Quick Add Product on the fly from Sale Modal
    fun addProductAndRecordSale(name: String, salePrice: Double, paymentMethod: String) {
        viewModelScope.launch {
            val cfg = configState.value
            val canCreate = when (cfg?.active_mode) {
                UserRole.LINKED_WORKER.name -> cfg.show_stock_tab
                else -> true
            }
            if (!canCreate && cfg?.active_mode == UserRole.LINKED_WORKER.name) {
                showToast("No tienes permiso para agregar productos nuevos")
                return@launch
            }

            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar una venta")
                return@launch
            }

            val newProd = ProductEntity(
                name = name,
                cost_price = salePrice * 0.7,
                sale_price = salePrice,
                initial_stock = 100.0,
                current_stock = 99.0
            )
            repository.insertProduct(newProd)

            repository.recordSale(
                shiftId = shift.id,
                productId = newProd.id,
                productName = newProd.name,
                quantity = 1.0,
                unitPrice = salePrice,
                paymentMethod = paymentMethod
            )

            showToast("Producto '$name' creado y vendido")
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx.id, tx.product_id, tx.quantity)
            showToast("Venta cancelada")
        }
    }

    // Expenses
    fun addExpense(description: String, amount: Double) {
        viewModelScope.launch {
            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar un egreso")
                return@launch
            }
            repository.addExpense(shift.id, description, amount)
            showToast("Egreso registrado")
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            showToast("Egreso eliminado")
        }
    }

    // Stock Difference Day Operations
    fun loadPreviousDayStock() {
        viewModelScope.launch {
            val products = activeProductsState.value
            for (p in products) {
                // Set initial_stock = current_stock
                val updated = p.copy(initial_stock = p.current_stock)
                repository.updateProduct(updated)
            }
            showToast("Inventario inicial cargado desde el día anterior")
        }
    }

    fun recordStockEntry(product: ProductEntity, quantity: Double) {
        viewModelScope.launch {
            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar un movimiento de stock")
                return@launch
            }
            repository.recordStockMovement(shift.id, product.id, product.name, "STOCK_ENTRY", quantity)
            showToast("Entrada de stock registrada")
        }
    }

    fun recordStockLoss(product: ProductEntity, quantity: Double) {
        viewModelScope.launch {
            val shift = repository.getCurrentOpenShift()
            if (shift == null) {
                showToast("Debes iniciar el día antes de registrar un movimiento de stock")
                return@launch
            }
            repository.recordStockMovement(shift.id, product.id, product.name, "STOCK_LOSS", quantity)
            showToast("Baja de stock registrada")
        }
    }

    fun saveFinalStockCount(product: ProductEntity, count: Double) {
        viewModelScope.launch {
            val updated = product.copy(final_stock = count)
            repository.updateProduct(updated)
        }
    }

    // Shift Close - Transparent (Solo Owner / Independent Worker)
    fun closeTransparentShift(declaredCash: Double, declaredTransfer: Double) {
        viewModelScope.launch {
            val shift = repository.currentOpenShiftFlow.firstOrNull() ?: return@launch
            val txs = repository.getTransactionsForShift(shift.id).firstOrNull() ?: emptyList()
            val exps = repository.getExpensesForShift(shift.id).firstOrNull() ?: emptyList()

            var cashSales = 0.0
            var transferSales = 0.0
            var totalCost = 0.0

            for (tx in txs) {
                if (tx.type == "SALE") {
                    if (tx.payment_method == "CASH") cashSales += (tx.quantity * tx.unit_price)
                    else transferSales += (tx.quantity * tx.unit_price)
                    
                    val prod = db.productDao().getProductById(tx.product_id)
                    if (prod != null) {
                        totalCost += (tx.quantity * prod.cost_price)
                    }
                }
            }

            val totalExpenses = exps.sumOf { it.amount }
            val systemExpectedCash = cashSales - totalExpenses
            val totalSales = cashSales + transferSales
            val cfg = configState.value
            val commissionRate = cfg?.commission_rate ?: 0.0
            val totalCommission = totalSales * (commissionRate / 100.0)
            val netProfit = (totalSales - totalCost - totalExpenses).coerceAtLeast(0.0)

            val closedShift = shift.copy(
                end_time = System.currentTimeMillis(),
                declared_cash = declaredCash,
                declared_transfer = declaredTransfer,
                system_expected_cash = systemExpectedCash,
                total_commission = totalCommission,
                net_profit = netProfit,
                status = "CLOSED"
            )

            repository.closeShift(closedShift)
            showToast("Turno cerrado guardado en historial")
        }
    }

    // Shift Close - Blind Audit for Linked Worker (Empresa)
    fun submitBlindAudit(declaredCash: Double, declaredTransfer: Double, context: Context): Uri? {
        val cfg = configState.value ?: return null
        val bossUuid = cfg.sync_boss_uuid ?: cfg.device_id
        val shift = currentShiftState.value ?: return null
        val txs = shiftTransactions.value
        val exps = shiftExpenses.value

        var cashSales = 0.0
        var transferSales = 0.0
        for (tx in txs) {
            if (tx.type == "SALE") {
                if (tx.payment_method == "CASH") cashSales += (tx.quantity * tx.unit_price)
                else transferSales += (tx.quantity * tx.unit_price)
            }
        }
        val totalExpenses = exps.sumOf { it.amount }
        val systemExpectedCash = cashSales - totalExpenses

        // Create audit JSON
        val auditJson = JSONObject()
        auditJson.put("worker_name", cfg.worker_name)
        auditJson.put("boss_uuid", bossUuid)
        auditJson.put("start_time", shift.start_time)
        auditJson.put("end_time", System.currentTimeMillis())
        auditJson.put("declared_cash", declaredCash)
        auditJson.put("declared_transfer", declaredTransfer)
        auditJson.put("system_expected_cash", systemExpectedCash)
        auditJson.put("transactions_count", txs.size)
        auditJson.put("total_sales", cashSales + transferSales)

        val encryptedPayload = PosyncCrypto.encryptPayload(auditJson.toString(), bossUuid)

        // Close shift locally
        viewModelScope.launch {
            val closedShift = shift.copy(
                end_time = System.currentTimeMillis(),
                declared_cash = declaredCash,
                declared_transfer = declaredTransfer,
                system_expected_cash = systemExpectedCash,
                status = "CLOSED"
            )
            repository.closeShift(closedShift)
        }

        // Save to cache file for sharing
        val fileName = "Cierre_${System.currentTimeMillis()}_${cfg.worker_name}.posync_day"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(encryptedPayload.toByteArray(Charsets.UTF_8))
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        _shareFileUri.value = uri
        return uri
    }

    // Master Owner Catalog Export (.posync_cat)
    fun exportCatalogAndConfig(context: Context, targetMode: String, canCreateProducts: Boolean): Uri? {
        val cfg = configState.value ?: return null
        val products = activeProductsState.value

        val root = JSONObject()
        root.put("boss_uuid", cfg.device_id)
        root.put("assigned_mode", targetMode)
        root.put("can_create_products", canCreateProducts)

        val prodArr = JSONArray()
        for (p in products) {
            val item = JSONObject()
            item.put("id", p.id)
            item.put("name", p.name)
            item.put("cost_price", p.cost_price)
            item.put("sale_price", p.sale_price)
            item.put("current_stock", p.current_stock)
            prodArr.put(item)
        }
        root.put("products", prodArr)

        val encrypted = PosyncCrypto.encryptPayload(root.toString(), cfg.device_id)

        val fileName = "Catalogo_${cfg.worker_name}.posync_cat"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(encrypted.toByteArray(Charsets.UTF_8))
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        _shareFileUri.value = uri
        return uri
    }

    // Decrypt and Import .posync_cat payload
    fun importPosyncCatUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val encryptedText = inputStream?.bufferedReader()?.use { it.readText() } ?: return@launch
                val cfg = repository.getConfig() ?: return@launch
                val bossUuid = cfg.sync_boss_uuid ?: cfg.device_id

                val decryptedJson = PosyncCrypto.decryptPayload(encryptedText, bossUuid)
                val json = JSONObject(decryptedJson)

                val assignedMode = json.optString("assigned_mode", "SALES_COUNT")
                val canCreate = json.optBoolean("can_create_products", false)
                val prodArr = json.optJSONArray("products")

                val newProducts = mutableListOf<ProductEntity>()
                if (prodArr != null) {
                    for (i in 0 until prodArr.length()) {
                        val obj = prodArr.getJSONObject(i)
                        newProducts.add(
                            ProductEntity(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                name = obj.getString("name"),
                                cost_price = obj.optDouble("cost_price", 0.0),
                                sale_price = obj.getDouble("sale_price"),
                                current_stock = obj.optDouble("current_stock", 0.0)
                            )
                        )
                    }
                }

                repository.importCatalogProducts(newProducts)

                // Update employee config
                val updatedCfg = cfg.copy(
                    operation_system = assignedMode,
                    show_stock_tab = canCreate
                )
                repository.saveConfig(updatedCfg)

                withContext(Dispatchers.Main) {
                    showToast("Configuración y ${newProducts.size} productos cargados con éxito")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al desencriptar el catálogo: ${e.localizedMessage}")
                }
            }
        }
    }

    // Master Owner Decrypt and Process .posync_day Audit
    fun processPosyncDayUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val encryptedText = inputStream?.bufferedReader()?.use { it.readText() } ?: return@launch
                val cfg = repository.getConfig() ?: return@launch

                val decryptedJson = PosyncCrypto.decryptPayload(encryptedText, cfg.device_id)
                val json = JSONObject(decryptedJson)

                val workerName = json.getString("worker_name")
                val start = json.getLong("start_time")
                val end = json.getLong("end_time")
                val declaredCash = json.getDouble("declared_cash")
                val declaredTransfer = json.getDouble("declared_transfer")
                val expectedCash = json.getDouble("system_expected_cash")
                val diff = declaredCash - expectedCash
                val isSurplus = diff >= 0
                val totalSales = json.optDouble("total_sales", 0.0)
                val txCount = json.optInt("transactions_count", 0)

                val auditResult = ForensicAuditData(
                    workerName = workerName,
                    shiftStart = start,
                    shiftEnd = end,
                    declaredCash = declaredCash,
                    declaredTransfer = declaredTransfer,
                    systemExpectedCash = expectedCash,
                    cashDifference = diff,
                    isSurplus = isSurplus,
                    totalSales = totalSales,
                    transactionsCount = txCount,
                    rawJson = decryptedJson
                )

                _forensicAudit.value = auditResult
                withContext(Dispatchers.Main) {
                    showToast("Auditoría forense desencriptada para $workerName")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al desencriptar el cierre: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearForensicAudit() {
        _forensicAudit.value = null
    }

    // Mode Toggle in Settings (for Particular Plan)
    fun setOperationSystem(mode: OperationMode) {
        viewModelScope.launch {
            val cfg = configState.value ?: return@launch
            if (cfg.active_mode == UserRole.LINKED_WORKER.name) {
                showToast("Un empleado vinculado no puede modificar su modo de trabajo")
                return@launch
            }
            val updated = cfg.copy(operation_system = mode.name)
            repository.saveConfig(updated)
            showToast("Modo de trabajo cambiado a ${mode.displayName}")
        }
    }

    fun toggleStockTab(show: Boolean) {
        viewModelScope.launch {
            val cfg = configState.value ?: return@launch
            val updated = cfg.copy(show_stock_tab = show)
            repository.saveConfig(updated)
        }
    }

    fun updateCommissionRate(rate: Double) {
        viewModelScope.launch {
            val cfg = configState.value ?: return@launch
            val updated = cfg.copy(commission_rate = rate)
            repository.saveConfig(updated)
            showToast("Comisión de trabajador actualizada a $rate%")
        }
    }

    fun updateWorkerName(name: String) {
        viewModelScope.launch {
            val cfg = configState.value ?: return@launch
            val updated = cfg.copy(worker_name = name)
            repository.saveConfig(updated)
        }
    }

    fun addEmployee(name: String, assignedMode: String, canCreate: Boolean, commission: Double) {
        viewModelScope.launch {
            val emp = EmployeeEntity(
                name = name,
                assigned_mode = assignedMode,
                can_create_products = canCreate,
                commission_rate = commission
            )
            repository.insertEmployee(emp)
            showToast("Perfil de empleado creado")
        }
    }

    fun deleteEmployee(uuid: String) {
        viewModelScope.launch {
            repository.deleteEmployee(uuid)
            showToast("Empleado eliminado")
        }
    }
}
