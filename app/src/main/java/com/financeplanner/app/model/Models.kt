package com.financeplanner.app.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class TransactionType { DEBIT, CREDIT }

data class CheckingAccount(var balance: Double)

data class Caixinha(val id: String, var name: String, var balance: Double)

data class Vale(val id: String, val label: String, var balance: Double, var creditDay: Int, var amount: Double)

data class SalaryConfig(var amount: Double, var dayOfMonth: Int)

data class CreditCardConfig(var nextInvoiceAmount: Double, var closingDay: Int)

data class TransactionEvent(
    val id: String,
    val name: String,
    val amount: Double,
    val date: LocalDate,
    val type: TransactionType,
    val source: AccountSource,
    val destination: AccountSource? = null
)

data class SimulatedTransactionInput(
    val name: String,
    val amount: Double,
    val dates: List<LocalDate>,
    val type: TransactionType,
    val source: AccountSource,
    val destination: AccountSource? = null
)

data class BalanceSnapshot(
    val date: LocalDate,
    val checking: Double,
    val caixinhaTotal: Double,
    val valeTotal: Double,
    val cardDebt: Double
)

enum class AccountSource { CHECKING, CAIXINHAS, VALE }

data class DashboardInsight(
    val label: String,
    val startValue: Double,
    val endValue: Double
)

fun LocalDate.adjustForWeekend(): LocalDate = when (dayOfWeek) {
    DayOfWeek.SATURDAY -> minusDays(1)
    DayOfWeek.SUNDAY -> minusDays(2)
    else -> this
}
