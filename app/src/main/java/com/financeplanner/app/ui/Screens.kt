package com.financeplanner.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeplanner.app.data.FinanceViewModel
import com.financeplanner.app.model.AccountSource
import com.financeplanner.app.model.BalanceSnapshot
import com.financeplanner.app.model.DashboardInsight
import com.financeplanner.app.model.SimulatedTransactionInput
import com.financeplanner.app.model.TransactionType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
fun HomeScreen(viewModel: FinanceViewModel) {
    val today = remember { LocalDate.now() }
    val balances = remember(today) {
        viewModel.balances(today..today.plusDays(30))
    }
    val caixinhaTotal = viewModel.caixinhas.sumOf { it.balance }
    val valeTotal = viewModel.vales.sumOf { it.balance }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Panorama", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Conta Corrente", value = viewModel.checkingAccount.balance)
            SummaryCard(title = "Caixinhas", value = caixinhaTotal)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Vales", value = valeTotal)
            SummaryCard(title = "Cartão (dívida)", value = -viewModel.creditCardConfig.debt, highlightNegative = true)
        }
        SummaryCard(title = "Total (CC + Caixinhas)", value = viewModel.checkingAccount.balance + caixinhaTotal)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Próximos 30 dias", style = MaterialTheme.typography.titleMedium)
        balances.firstOrNull()?.let {
            DailyBalanceTable(balances)
        }
    }
}

@Composable
fun InputsScreen(viewModel: FinanceViewModel) {
    var checkingText by remember { mutableStateOf(viewModel.checkingAccount.balance.toString()) }
    var salaryText by remember { mutableStateOf(viewModel.salary.amount.toString()) }
    var salaryDay by remember { mutableStateOf(viewModel.salary.dayOfMonth.toString()) }
    var cardDebt by remember { mutableStateOf(viewModel.creditCardConfig.debt.toString()) }
    var cardDay by remember { mutableStateOf(viewModel.creditCardConfig.closingDay.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Valores iniciais", style = MaterialTheme.typography.titleLarge)
        SummaryCard(title = "Atualizar Conta Corrente") {
            OutlinedTextField(
                value = checkingText,
                onValueChange = { checkingText = it },
                label = { Text("Saldo atual") }
            )
            Button(onClick = { checkingText.toDoubleOrNull()?.let(viewModel::updateCheckingBalance) }) { Text("Salvar") }
        }
        Text("Caixinhas CDB", style = MaterialTheme.typography.titleMedium)
        CaixinhaSection(viewModel)
        Text("Vales", style = MaterialTheme.typography.titleMedium)
        ValeSection(viewModel)
        Text("Movimentações Padrão", style = MaterialTheme.typography.titleMedium)
        SummaryCard(title = "Salário") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = salaryText, onValueChange = { salaryText = it }, label = { Text("Valor") })
                OutlinedTextField(value = salaryDay, onValueChange = { salaryDay = it }, label = { Text("Dia do mês") })
            }
            Button(onClick = {
                val amount = salaryText.toDoubleOrNull()
                val day = salaryDay.toIntOrNull()
                if (amount != null && day != null) viewModel.updateSalary(amount, day)
            }) { Text("Salvar salário") }
        }
        SummaryCard(title = "Cartão de Crédito") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = cardDebt, onValueChange = { cardDebt = it }, label = { Text("Dívida atual") })
                OutlinedTextField(value = cardDay, onValueChange = { cardDay = it }, label = { Text("Dia fechamento") })
            }
            Button(onClick = {
                val debt = cardDebt.toDoubleOrNull()
                val day = cardDay.toIntOrNull()
                if (debt != null && day != null) viewModel.updateCreditCard(debt, day)
            }) { Text("Salvar cartão") }
        }
    }
}

@Composable
private fun CaixinhaSection(viewModel: FinanceViewModel) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
            OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Saldo inicial") })
        }
        Button(onClick = {
            val amount = balance.toDoubleOrNull()
            if (name.isNotBlank() && amount != null) {
                viewModel.addCaixinha(name, amount)
                name = ""
                balance = ""
            }
        }) { Text("Adicionar caixinha") }
        viewModel.caixinhas.forEach { caixinha ->
            SummaryCard(title = caixinha.name, value = caixinha.balance) {
                Text("ID: ${caixinha.id.take(6)}...", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { viewModel.removeCaixinha(caixinha.id) }) { Text("Remover") }
            }
        }
    }
}

@Composable
private fun ValeSection(viewModel: FinanceViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        viewModel.vales.forEach { vale ->
            var balance by remember(vale.id) { mutableStateOf(vale.balance.toString()) }
            var creditDay by remember(vale.id) { mutableStateOf(if (vale.creditDay == -1) "" else vale.creditDay.toString()) }
            var amount by remember(vale.id) { mutableStateOf(vale.amount.toString()) }
            SummaryCard(title = vale.label) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Valor padrão") })
                    OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Saldo atual") })
                }
                OutlinedTextField(
                    value = creditDay,
                    onValueChange = { creditDay = it },
                    label = { Text("Dia de crédito (vazio = penúltimo dia útil)") }
                )
                Button(onClick = {
                    val current = balance.toDoubleOrNull()
                    val day = creditDay.toIntOrNull() ?: -1
                    val valeAmount = amount.toDoubleOrNull()
                    if (current != null && valeAmount != null) {
                        viewModel.updateVale(vale.id, current, day, valeAmount)
                    }
                }) { Text("Atualizar vale") }
            }
        }
    }
}

@Composable
fun SimulationScreen(viewModel: FinanceViewModel) {
    val today = remember { LocalDate.now() }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2024-12-13;2024-12-15..2024-12-20") }
    var type by remember { mutableStateOf(TransactionType.DEBIT) }
    var source by remember { mutableStateOf(AccountSource.CHECKING) }
    var destination by remember { mutableStateOf(AccountSource.CAIXINHAS) }

    val range = remember { today..today.plusDays(30) }
    val standardTransactions = remember(range) { viewModel.upcomingStandardTransactions(range) }
    val simulated = remember { mutableStateListOf(*viewModel.futureSimulations(range).toTypedArray()) }

    LaunchedEffect(viewModel) {
        simulated.clear()
        simulated.addAll(viewModel.futureSimulations(range))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Simular transações", style = MaterialTheme.typography.titleLarge) }
        item {
            SummaryCard(title = "Configurar transação simulada") {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Valor") })
                    OutlinedTextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("Datas (yyyy-MM-dd;início..fim)") })
                }
                Text("Tipo")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == TransactionType.DEBIT, onClick = { type = TransactionType.DEBIT }, label = { Text("Débito") })
                    FilterChip(selected = type == TransactionType.CREDIT, onClick = { type = TransactionType.CREDIT }, label = { Text("Crédito") })
                }
                Text("Origem")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = source == AccountSource.CHECKING, onClick = { source = AccountSource.CHECKING }, label = { Text("Conta Corrente") })
                    FilterChip(selected = source == AccountSource.CAIXINHAS, onClick = { source = AccountSource.CAIXINHAS }, label = { Text("Caixinhas") })
                    FilterChip(selected = source == AccountSource.VALE, onClick = { source = AccountSource.VALE }, label = { Text("Vales") })
                }
                if (type == TransactionType.DEBIT) {
                    Text("Destino (transferência opcional)")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = destination == AccountSource.CHECKING, onClick = { destination = AccountSource.CHECKING }, label = { Text("Conta Corrente") })
                        FilterChip(selected = destination == AccountSource.CAIXINHAS, onClick = { destination = AccountSource.CAIXINHAS }, label = { Text("Caixinhas") })
                    }
                }
                Button(onClick = {
                    val parsed = parseDates(dateInput)
                    val numeric = amount.toDoubleOrNull()
                    if (name.isNotBlank() && numeric != null && parsed.isNotEmpty()) {
                        viewModel.addSimulatedTransaction(
                            SimulatedTransactionInput(
                                name = name,
                                amount = numeric,
                                dates = parsed,
                                type = type,
                                source = source,
                                destination = if (type == TransactionType.DEBIT) destination else null
                            )
                        )
                        simulated.clear()
                        simulated.addAll(viewModel.futureSimulations(range))
                        name = ""
                        amount = ""
                    }
                }) { Text("Adicionar simulação") }
            }
        }
        item {
            SummaryCard(title = "Futuras transações padrão") {
                standardTransactions.forEach { event ->
                    TransactionRow(title = event.name, subtitle = event.date.format(dateFormatter), amount = event.amount, positive = event.type == TransactionType.CREDIT)
                }
            }
        }
        item {
            SummaryCard(title = "Transações simuladas") {
                if (simulated.isEmpty()) {
                    Text("Nenhuma simulação adicionada")
                }
                simulated.forEach { event ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        TransactionRow(
                            title = event.name,
                            subtitle = event.date.format(dateFormatter),
                            amount = event.amount,
                            positive = event.type == TransactionType.CREDIT
                        )
                        IconButton(onClick = {
                            viewModel.removeSimulatedTransaction(event.id)
                            simulated.remove(event)
                        }) { Icon(Icons.Outlined.Delete, contentDescription = null) }
                    }
                }
                if (simulated.isNotEmpty()) {
                    TextButton(onClick = {
                        viewModel.clearSimulatedTransactions()
                        simulated.clear()
                    }) { Text("Limpar todas") }
                }
            }
        }
        item {
            Text("Projeção diária", style = MaterialTheme.typography.titleMedium)
            DailyBalanceTable(viewModel.balances(range))
        }
    }
}

@Composable
fun DashboardScreen(viewModel: FinanceViewModel) {
    var startText by remember { mutableStateOf(LocalDate.now().toString()) }
    var endText by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var range by remember { mutableStateOf(LocalDate.now()..LocalDate.now().plusDays(30)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Início (yyyy-MM-dd)") })
            OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("Fim (yyyy-MM-dd)") })
            Button(onClick = {
                val start = runCatching { LocalDate.parse(startText) }.getOrNull()
                val end = runCatching { LocalDate.parse(endText) }.getOrNull()
                if (start != null && end != null) range = start..end
            }) { Text("Aplicar filtro") }
        }
        val insights = viewModel.dashboardInsights(range)
        InsightsSection(insights)
        val history = viewModel.balances(range)
        if (history.isNotEmpty()) {
            Text("Faixa diária", style = MaterialTheme.typography.titleMedium)
            BandChart(history.map { it.date to (it.checking + it.caixinhaTotal) })
            Text("Variação diária", style = MaterialTheme.typography.titleMedium)
            LineChart(history.map { it.date to (it.checking + it.caixinhaTotal) })
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: Double? = null, highlightNegative: Boolean = false, content: @Composable (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            value?.let {
                val color = if (it < 0 || highlightNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                Text(currencyFormat.format(it), color = color, fontWeight = FontWeight.Bold)
            }
            content?.invoke()
        }
    }
}

@Composable
private fun TransactionRow(title: String, subtitle: String, amount: Double, positive: Boolean) {
    val color = if (positive) Color(0xFF10B981) else MaterialTheme.colorScheme.error
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Text(currencyFormat.format(amount), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DailyBalanceTable(history: List<BalanceSnapshot>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dia", fontWeight = FontWeight.Bold)
                Text("CC")
                Text("Caixinhas")
                Text("Vales")
                Text("Cartão")
            }
            history.forEach { snapshot ->
                val checkingColor = if (snapshot.checking >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val caixinhaColor = if (snapshot.caixinhaTotal >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val valeColor = if (snapshot.valeTotal >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val cardColor = if (snapshot.cardDebt <= 0) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(snapshot.date.format(dateFormatter), modifier = Modifier.weight(1f))
                    Text(currencyFormat.format(snapshot.checking), modifier = Modifier.weight(1f), color = checkingColor, textAlign = TextAlign.End)
                    Text(currencyFormat.format(snapshot.caixinhaTotal), modifier = Modifier.weight(1f), color = caixinhaColor, textAlign = TextAlign.End)
                    Text(currencyFormat.format(snapshot.valeTotal), modifier = Modifier.weight(1f), color = valeColor, textAlign = TextAlign.End)
                    Text(currencyFormat.format(snapshot.cardDebt), modifier = Modifier.weight(1f), color = cardColor, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun InsightsSection(insights: List<DashboardInsight>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        insights.forEach { insight ->
            val diff = insight.endValue - insight.startValue
            val positive = diff >= 0
            val color = if (positive) Color(0xFF10B981) else MaterialTheme.colorScheme.error
            SummaryCard(title = insight.label) {
                Text("Início: ${currencyFormat.format(insight.startValue)}")
                Text("Fim: ${currencyFormat.format(insight.endValue)}", fontWeight = FontWeight.Bold)
                Text("Variação: ${currencyFormat.format(diff)}", color = color)
            }
        }
    }
}

@Composable
private fun BandChart(data: List<Pair<LocalDate, Double>>) {
    if (data.isEmpty()) return
    val max = data.maxOf { it.second }
    val min = data.minOf { it.second }
    val span = (max - min).takeIf { it != 0.0 } ?: 1.0
    Card(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.height(140.dp)) {
            val widthStep = size.width / (data.size - 1).coerceAtLeast(1)
            data.forEachIndexed { index, pair ->
                val x = widthStep * index
                val normalized = (pair.second - min) / span
                val y = size.height - (normalized * size.height)
                drawCircle(color = MaterialTheme.colorScheme.primary, radius = 6f, center = androidx.compose.ui.geometry.Offset(x, y))
            }
        }
    }
}

@Composable
private fun LineChart(data: List<Pair<LocalDate, Double>>) {
    if (data.isEmpty()) return
    val max = data.maxOf { it.second }
    val min = data.minOf { it.second }
    val span = (max - min).takeIf { it != 0.0 } ?: 1.0
    Card(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.height(140.dp)) {
            val widthStep = size.width / (data.size - 1).coerceAtLeast(1)
            var lastPoint: androidx.compose.ui.geometry.Offset? = null
            data.forEachIndexed { index, pair ->
                val x = widthStep * index
                val normalized = (pair.second - min) / span
                val y = size.height - (normalized * size.height)
                val point = androidx.compose.ui.geometry.Offset(x, y)
                lastPoint?.let { previous ->
                    drawLine(color = MaterialTheme.colorScheme.secondary, start = previous, end = point, strokeWidth = 6f)
                }
                lastPoint = point
            }
        }
    }
}

private fun parseDates(raw: String): List<LocalDate> {
    val parts = raw.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    val dates = mutableListOf<LocalDate>()
    parts.forEach { part ->
        if (part.contains("..")) {
            val rangeParts = part.split("..")
            val start = runCatching { LocalDate.parse(rangeParts[0]) }.getOrNull()
            val end = runCatching { LocalDate.parse(rangeParts.getOrElse(1) { rangeParts[0] }) }.getOrNull()
            if (start != null && end != null) {
                var date = start
                while (!date.isAfter(end)) {
                    dates.add(date)
                    date = date.plusDays(1)
                }
            }
        } else {
            runCatching { LocalDate.parse(part) }.getOrNull()?.let(dates::add)
        }
    }
    return dates.distinct().sorted()
}
