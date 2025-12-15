package com.financeplanner.app.data

import androidx.lifecycle.ViewModel
import com.financeplanner.app.model.AccountSource
import com.financeplanner.app.model.BalanceSnapshot
import com.financeplanner.app.model.Caixinha
import com.financeplanner.app.model.CreditCardConfig
import com.financeplanner.app.model.CheckingAccount
import com.financeplanner.app.model.DashboardFocus
import com.financeplanner.app.model.DashboardInsight
import com.financeplanner.app.model.SalaryConfig
import com.financeplanner.app.model.SimulatedTransactionInput
import com.financeplanner.app.model.TransactionEvent
import com.financeplanner.app.model.TransactionType
import com.financeplanner.app.model.Vale
import com.financeplanner.app.model.adjustForWeekend
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class FinanceViewModel : ViewModel() {
    var checkingAccount = CheckingAccount(balance = 0.0)
        private set

    var caixinhas = mutableListOf<Caixinha>()
        private set

    var vales = mutableListOf(
        Vale(id = UUID.randomUUID().toString(), label = "Vale Refeição", balance = 465.08, creditDay = -1, amount = 1173.26),
        Vale(id = UUID.randomUUID().toString(), label = "Vale Alimentação", balance = 752.31, creditDay = -1, amount = 924.47)
    )
        private set

    var salary = SalaryConfig(amount = 5943.48, dayOfMonth = 25)
        private set

    var creditCardConfig = CreditCardConfig(nextInvoiceAmount = 0.0, closingDay = 25)
        private set

    private val simulatedTransactions = mutableListOf<TransactionEvent>()

    fun updateCheckingBalance(newBalance: Double) {
        checkingAccount = checkingAccount.copy(balance = newBalance)
    }

    fun addCaixinha(name: String, balance: Double) {
        caixinhas.add(Caixinha(id = UUID.randomUUID().toString(), name = name, balance = balance))
    }

    fun updateCaixinha(id: String, name: String, balance: Double) {
        caixinhas.replaceAll { if (it.id == id) it.copy(name = name, balance = balance) else it }
    }

    fun removeCaixinha(id: String) {
        caixinhas.removeAll { it.id == id }
    }

    fun updateVale(id: String, balance: Double, creditDay: Int, amount: Double) {
        vales.replaceAll { if (it.id == id) it.copy(balance = balance, creditDay = creditDay, amount = amount) else it }
    }

    fun updateSalary(amount: Double, dayOfMonth: Int) {
        salary = salary.copy(amount = amount, dayOfMonth = dayOfMonth)
    }

    fun updateCreditCard(nextInvoiceAmount: Double, closingDay: Int) {
        creditCardConfig = creditCardConfig.copy(nextInvoiceAmount = nextInvoiceAmount, closingDay = closingDay)
    }

    fun addSimulatedTransaction(input: SimulatedTransactionInput) {
        input.dates.forEach { date ->
            simulatedTransactions.add(
                TransactionEvent(
                    id = UUID.randomUUID().toString(),
                    name = input.name,
                    amount = input.amount,
                    date = date,
                    type = input.type,
                    source = input.source,
                    destination = input.destination
                )
            )
        }
    }

    fun removeSimulatedTransaction(id: String) {
        simulatedTransactions.removeAll { it.id == id }
    }

    fun clearSimulatedTransactions() {
        simulatedTransactions.clear()
    }

    fun upcomingStandardTransactions(range: ClosedRange<LocalDate>): List<TransactionEvent> {
        val events = mutableListOf<TransactionEvent>()
        val start = range.start
        val end = range.endInclusive

        // Salary
        val salaryDate = nextDateForDay(salary.dayOfMonth, start).adjustForWeekend()
        if (salaryDate in start..end) {
            events.add(
                TransactionEvent(
                    id = "salary-${salaryDate}",
                    name = "Salário",
                    amount = salary.amount,
                    date = salaryDate,
                    type = TransactionType.CREDIT,
                    source = AccountSource.CHECKING
                )
            )
        }

        // Credit card payment
        val cardDate = nextDateForDay(creditCardConfig.closingDay, start)
        if (creditCardConfig.nextInvoiceAmount > 0 && cardDate in start..end) {
            events.add(
                TransactionEvent(
                    id = "card-${cardDate}",
                    name = "Pagamento fatura",
                    amount = creditCardConfig.nextInvoiceAmount,
                    date = cardDate,
                    type = TransactionType.DEBIT,
                    source = AccountSource.CHECKING
                )
            )
        }

        // Vale credits (penultimate business day by default)
        val monthsToCheck = setOf(start.withDayOfMonth(1), start.plusMonths(1).withDayOfMonth(1))
        monthsToCheck.forEach { monthStart ->
            val penultimateBusiness = penultimateBusinessDay(monthStart)
            val targetDate = penultimateBusiness
            vales.forEach { vale ->
                val creditDate = if (vale.creditDay == -1) targetDate else nextDateForDay(vale.creditDay, start)
                if (creditDate in start..end) {
                    events.add(
                        TransactionEvent(
                            id = "vale-${vale.id}-${creditDate}",
                            name = vale.label,
                            amount = vale.amount,
                            date = creditDate,
                            type = TransactionType.CREDIT,
                            source = AccountSource.VALE
                        )
                    )
                }
            }
        }

        return events.sortedBy { it.date }
    }

    fun futureSimulations(range: ClosedRange<LocalDate>): List<TransactionEvent> =
        simulatedTransactions.filter { it.date in range.start..range.endInclusive }.sortedBy { it.date }

    fun allSimulatedTransactions(): List<TransactionEvent> = simulatedTransactions.sortedBy { it.date }

    fun balances(range: ClosedRange<LocalDate>): List<BalanceSnapshot> {
        val start = range.start
        val end = range.endInclusive
        var checking = checkingAccount.balance
        var caixinhaTotal = caixinhas.sumOf { it.balance }
        var valeTotal = vales.sumOf { it.balance }
        var cardDebt = creditCardConfig.nextInvoiceAmount

        val allEvents = (upcomingStandardTransactions(range) + futureSimulations(range)).groupBy { it.date }
        val snapshots = mutableListOf<BalanceSnapshot>()
        var currentDate = start
        while (!currentDate.isAfter(end)) {
            allEvents[currentDate]?.forEach { event ->
                when (event.source) {
                    AccountSource.CHECKING -> {
                        checking += if (event.type == TransactionType.CREDIT) event.amount else -event.amount
                    }
                    AccountSource.CAIXINHAS -> {
                        caixinhaTotal += if (event.type == TransactionType.CREDIT) event.amount else -event.amount
                    }
                    AccountSource.VALE -> {
                        valeTotal += if (event.type == TransactionType.CREDIT) event.amount else -event.amount
                    }
                    AccountSource.CREDIT_CARD -> {
                        cardDebt += if (event.type == TransactionType.DEBIT) event.amount else -event.amount
                    }
                }
                if (event.destination != null && event.type == TransactionType.DEBIT) {
                    when (event.destination) {
                        AccountSource.CHECKING -> checking += event.amount
                        AccountSource.CAIXINHAS -> caixinhaTotal += event.amount
                        AccountSource.VALE -> valeTotal += event.amount
                        AccountSource.CREDIT_CARD -> cardDebt += event.amount
                    }
                }
                if (event.id.startsWith("card-")) {
                    cardDebt = 0.0
                }
            }
            snapshots.add(
                BalanceSnapshot(
                    date = currentDate,
                    checking = checking,
                    caixinhaTotal = caixinhaTotal,
                    valeTotal = valeTotal,
                    cardDebt = cardDebt
                )
            )
            currentDate = currentDate.plusDays(1)
        }
        return snapshots
    }

    fun dashboardInsights(range: ClosedRange<LocalDate>, focus: DashboardFocus): List<DashboardInsight> {
        val history = balances(range)
        val first = history.firstOrNull() ?: return emptyList()
        val last = history.lastOrNull() ?: return emptyList()

        val totalStart = first.checking + first.caixinhaTotal
        val totalEnd = last.checking + last.caixinhaTotal

        val caixinhaStart = first.caixinhaTotal
        val caixinhaEnd = last.caixinhaTotal

        val checkingStart = first.checking
        val checkingEnd = last.checking

        val valeStart = first.valeTotal
        val valeEnd = last.valeTotal

        return when (focus) {
            DashboardFocus.CONTAS -> listOf(
                DashboardInsight("Total", totalStart, totalEnd),
                DashboardInsight("Conta Corrente", checkingStart, checkingEnd),
                DashboardInsight("Caixinhas Total", caixinhaStart, caixinhaEnd)
            )

            DashboardFocus.VALES -> listOf(
                DashboardInsight("Vales", valeStart, valeEnd)
            )
        }
    }

    fun nextSalaryDate(from: LocalDate = LocalDate.now()): LocalDate =
        nextDateForDay(salary.dayOfMonth, from).adjustForWeekend()

    private fun nextDateForDay(day: Int, start: LocalDate): LocalDate {
        val candidate = start.withDayOfMonth(minOf(day, start.lengthOfMonth()))
        return if (!candidate.isBefore(start)) candidate else candidate.plusMonths(1).withDayOfMonth(minOf(day, candidate.plusMonths(1).lengthOfMonth()))
    }

    private fun penultimateBusinessDay(monthStart: LocalDate): LocalDate {
        val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
        var date = monthEnd
        var businessCount = 0
        while (businessCount < 2) {
            if (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY) {
                businessCount++
            }
            if (businessCount < 2) {
                date = date.minusDays(1)
            }
        }
        return date
    }
}
