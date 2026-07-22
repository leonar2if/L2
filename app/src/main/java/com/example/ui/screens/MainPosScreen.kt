package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.enums.OperationMode
import com.example.data.enums.UserRole
import com.example.ui.PosViewModel
import com.example.ui.components.PosTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.util.FileSharingHelper

private enum class ScreenDest {
    MAIN_POS,
    SETTINGS_HUB,
    AUDIT
}

@Composable
fun MainPosScreen(
    viewModel: PosViewModel,
    incomingFileUri: Uri? = null
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsState()
    val products by viewModel.activeProductsState.collectAsState()
    val shift by viewModel.currentShiftState.collectAsState()
    val transactions by viewModel.shiftTransactions.collectAsState()
    val expenses by viewModel.shiftExpenses.collectAsState()
    val employees by viewModel.employeesState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val forensicAudit by viewModel.forensicAudit.collectAsState()
    val shareFileUri by viewModel.shareFileUri.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenDest.MAIN_POS) }
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Caja, 1: Stock

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Share File listener
    LaunchedEffect(shareFileUri) {
        shareFileUri?.let { uri ->
            FileSharingHelper.shareFileViaWhatsApp(context, uri, "Cierre / Configuración .posync")
            viewModel.clearShareFileUri()
        }
    }

    // Incoming .posync file handler
    LaunchedEffect(incomingFileUri) {
        incomingFileUri?.let { uri ->
            val path = uri.toString()
            if (path.endsWith(".posync_cat", ignoreCase = true)) {
                viewModel.importPosyncCatUri(uri, context)
            } else if (path.endsWith(".posync_day", ignoreCase = true)) {
                viewModel.processPosyncDayUri(uri, context)
            }
        }
    }

    if (config == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonGreen)
        }
        return
    }

    val cfg = config!!

    // Phase 1.1: Onboarding Wizard (if unconfigured)
    if (cfg.active_mode == "UNCONFIGURED") {
        OnboardingScreen(
            onComplete = { role, plan ->
                viewModel.completeOnboarding(role, plan)
            }
        )
        return
    }

    // Phase 1.2: Payment & DRM Activation (if not licensed)
    if (!cfg.is_licensed) {
        val roleEnum = try { UserRole.valueOf(cfg.active_mode) } catch (e: Exception) { UserRole.SOLO_OWNER }
        DrmActivationScreen(
            role = roleEnum,
            plan = cfg.active_plan,
            androidId = viewModel.getAndroidId(),
            onActivateWithPin = { pin ->
                viewModel.activateDrm(pin)
            },
            onBindBossUuid = { bossUuid ->
                viewModel.bindLinkedWorkerToBoss(bossUuid)
            }
        )
        return
    }

    // Unlocked Main POS Application
    val isMasterOwner = cfg.active_mode == UserRole.MASTER_OWNER.name
    val isLinkedWorker = cfg.active_mode == UserRole.LINKED_WORKER.name
    val isIndependentWorker = cfg.active_mode == UserRole.INDEPENDENT_WORKER.name
    val isSoloOwner = cfg.active_mode == UserRole.SOLO_OWNER.name

    val canCreateProducts = if (isLinkedWorker) cfg.show_stock_tab else true

    Scaffold(
        topBar = {
            PosTopBar(
                title = when {
                    currentScreen == ScreenDest.SETTINGS_HUB -> "Ajustes del Sistema"
                    currentScreen == ScreenDest.AUDIT -> "Arqueo de Turno"
                    isMasterOwner -> "Panel Maestro B2B2E"
                    else -> "POS Híbrido Offline"
                },
                onSettingsClick = {
                    currentScreen = if (currentScreen == ScreenDest.SETTINGS_HUB) ScreenDest.MAIN_POS else ScreenDest.SETTINGS_HUB
                }
            )
        },
        bottomBar = {
            // Show bottom bar only in Sales Count Mode with show_stock_tab active, and not in settings/audit
            if (!isMasterOwner && currentScreen == ScreenDest.MAIN_POS && cfg.operation_system == OperationMode.SALES_COUNT.name && cfg.show_stock_tab) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = PureWhite
                ) {
                    NavigationBarItem(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Caja") },
                        label = { Text("Caja", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlack,
                            selectedTextColor = NeonGreen,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = MutedGray,
                            unselectedTextColor = MutedGray
                        )
                    )

                    NavigationBarItem(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = "Stock") },
                        label = { Text("Stock", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlack,
                            selectedTextColor = NeonGreen,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = MutedGray,
                            unselectedTextColor = MutedGray
                        )
                    )
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
            when (currentScreen) {
                ScreenDest.SETTINGS_HUB -> {
                    SettingsHubScreen(
                        config = cfg,
                        expenses = expenses,
                        onModeChanged = { mode -> viewModel.setOperationSystem(mode) },
                        onToggleStockTab = { show -> viewModel.toggleStockTab(show) },
                        onUpdateCommissionRate = { rate -> viewModel.updateCommissionRate(rate) },
                        onUpdateWorkerName = { name -> viewModel.updateWorkerName(name) },
                        onAddExpenseClick = { desc, amt -> viewModel.addExpense(desc, amt) },
                        onDeleteExpenseClick = { id -> viewModel.deleteExpense(id) },
                        onCloseAppClick = { (context as? Activity)?.finish() }
                    )
                }

                ScreenDest.AUDIT -> {
                    if (isLinkedWorker) {
                        BlindAuditScreen(
                            workerName = cfg.worker_name,
                            onConfirmBlindAudit = { declaredCash, declaredTransfer ->
                                viewModel.submitBlindAudit(declaredCash, declaredTransfer, context)
                                currentScreen = ScreenDest.MAIN_POS
                            },
                            onCancel = { currentScreen = ScreenDest.MAIN_POS }
                        )
                    } else {
                        val cashSales = transactions.filter { it.type == "SALE" && it.payment_method == "CASH" }
                            .sumOf { it.quantity * it.unit_price }
                        val transferSales = transactions.filter { it.type == "SALE" && it.payment_method == "TRANSFER" }
                            .sumOf { it.quantity * it.unit_price }
                        val totalSales = cashSales + transferSales
                        val totalExps = expenses.sumOf { it.amount }
                        val commissionAmt = totalSales * (cfg.commission_rate / 100.0)

                        TransparentAuditScreen(
                            shift = shift,
                            isSoloOwner = isSoloOwner,
                            totalCashSales = cashSales,
                            totalTransferSales = transferSales,
                            totalExpenses = totalExps,
                            netProfit = (totalSales - totalExps).coerceAtLeast(0.0),
                            commissionAmount = commissionAmt,
                            onConfirmCloseShift = { declaredCash, declaredTransfer ->
                                viewModel.closeTransparentShift(declaredCash, declaredTransfer)
                                currentScreen = ScreenDest.MAIN_POS
                            },
                            onCancel = { currentScreen = ScreenDest.MAIN_POS }
                        )
                    }
                }

                ScreenDest.MAIN_POS -> {
                    if (isMasterOwner) {
                        MasterDashboardScreen(
                            bossUuid = cfg.device_id,
                            products = products,
                            employees = employees,
                            forensicAudit = forensicAudit,
                            onExportCatalogClick = { mode, canCreate ->
                                viewModel.exportCatalogAndConfig(context, mode, canCreate)
                            },
                            onAddEmployeeClick = { name, mode, canCreate, comm ->
                                viewModel.addEmployee(name, mode, canCreate, comm)
                            },
                            onDeleteEmployeeClick = { uuid ->
                                viewModel.deleteEmployee(uuid)
                            },
                            onClearAuditResult = { viewModel.clearForensicAudit() }
                        )
                    } else {
                        if (cfg.operation_system == OperationMode.SALES_COUNT.name) {
                            if (activeSubTab == 0 || !cfg.show_stock_tab) {
                                CajaScreen(
                                    currentShift = shift,
                                    products = products,
                                    transactions = transactions,
                                    canCreateProducts = canCreateProducts,
                                    commissionRate = cfg.commission_rate,
                                    showCommission = isIndependentWorker,
                                    onStartShiftClick = { initialCash ->
                                        viewModel.startShift(initialCash)
                                    },
                                    onMultiSaleRecorded = { items, payments ->
                                        viewModel.registerMultiItemSale(items, payments)
                                    },
                                    onAddNewProductAndSell = { name, price, method ->
                                        viewModel.addProductAndRecordSale(name, price, method)
                                    },
                                    onDeleteTransaction = { tx ->
                                        viewModel.deleteTransaction(tx)
                                    },
                                    onAcabarDiaClick = {
                                        currentScreen = ScreenDest.AUDIT
                                    }
                                )
                            } else {
                                StockTableScreen(
                                    products = products,
                                    canCreateProducts = canCreateProducts,
                                    onLoadPreviousDayClick = { viewModel.loadPreviousDayStock() },
                                    onAddProductClick = { name, cost, sale, stock ->
                                        viewModel.addProductAndRecordSale(name, sale, "CASH")
                                    },
                                    onRecordStockEntry = { prod, qty -> viewModel.recordStockEntry(prod, qty) },
                                    onRecordStockLoss = { prod, qty -> viewModel.recordStockLoss(prod, qty) },
                                    onSaveFinalStockCount = { prod, count -> viewModel.saveFinalStockCount(prod, count) },
                                    onAcabarDiaClick = { currentScreen = ScreenDest.AUDIT }
                                )
                            }
                        } else {
                            // Pure Stock Difference Mode (No TabBar)
                            StockTableScreen(
                                products = products,
                                canCreateProducts = canCreateProducts,
                                onLoadPreviousDayClick = { viewModel.loadPreviousDayStock() },
                                onAddProductClick = { name, cost, sale, stock ->
                                    viewModel.addProductAndRecordSale(name, sale, "CASH")
                                },
                                onRecordStockEntry = { prod, qty -> viewModel.recordStockEntry(prod, qty) },
                                onRecordStockLoss = { prod, qty -> viewModel.recordStockLoss(prod, qty) },
                                onSaveFinalStockCount = { prod, count -> viewModel.saveFinalStockCount(prod, count) },
                                onAcabarDiaClick = { currentScreen = ScreenDest.AUDIT }
                            )
                        }
                    }
                }
            }
        }
    }
}
