package com.example.data.enums

enum class UserRole(val displayName: String) {
    SOLO_OWNER("Dueño Autónomo"),
    INDEPENDENT_WORKER("Trabajador Independiente"),
    MASTER_OWNER("Dueño Maestro (Empresa)"),
    LINKED_WORKER("Empleado Vinculado")
}

enum class UserPlan(val displayName: String) {
    PARTICULAR("Particular"),
    EMPRESA("Empresa"),
    FREE_LINKED("Gratuito (Vinculado)")
}

enum class OperationMode(val displayName: String) {
    SALES_COUNT("Modo Caja Registradora"),
    STOCK_DIFFERENCE("Modo Diferencia Inventario")
}

enum class ShiftStatus {
    OPEN,
    CLOSED,
    PENDING_AUDIT,
    AUDITED
}

enum class TransactionType {
    SALE,
    STOCK_ENTRY,
    STOCK_LOSS,
    STOCK_TRANSFER
}

enum class PaymentMethod(val label: String, val tag: String) {
    CASH("Efectivo", "($)"),
    TRANSFER("Transferencia", "(T)"),
    MIXED("Mixto", "(M)")
}
