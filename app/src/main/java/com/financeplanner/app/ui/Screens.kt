@file:OptIn(ExperimentalMaterial3Api::class)

package com.financeplanner.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Input
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeplanner.app.data.FinanceViewModel
import com.financeplanner.app.model.AccountSource
import com.financeplanner.app.model.BalanceSnapshot
import com.financeplanner.app.model.DashboardInsight
import com.financeplanner.app.model.SimulatedTransactionInput
import com.financeplanner.app.model.TransactionType
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun Double.toInputText(): String = if (this == 0.0) "" else this.toString()
private fun Int.toInputText(): String = if (this <= 0) "" else this.toString()

@Composable
fun HomeScreen(viewModel: FinanceViewModel, onNavigateTo: (String) -> Unit) {
    val caixinhaTotal = viewModel.caixinhas.sumOf { it.balance }
    val valeTotal = viewModel.vales.sumOf { it.balance }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Panorama",
                subtitle = "Acompanhe rapidamente seus saldos e o que vem por aí nas próximas semanas.",
                icon = Icons.Outlined.Home
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Conta Corrente",
                    value = viewModel.checkingAccount.balance,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Caixinhas",
                    value = caixinhaTotal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(title = "Vales", value = valeTotal, modifier = Modifier.weight(1f))
                SummaryCard(
                    title = "Cartão (dívida)",
                    value = -viewModel.creditCardConfig.nextInvoiceAmount,
                    highlightNegative = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            SummaryCard(
                title = "Total (CC + Caixinhas)",
                value = viewModel.checkingAccount.balance + caixinhaTotal,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HomeShortcuts(onNavigateTo = onNavigateTo)
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeShortcuts(onNavigateTo: (String) -> Unit) {
    SummaryCard(title = "Atalhos rápidos", modifier = Modifier.fillMaxWidth()) {
        Text(
            "Acesse rapidamente as outras áreas sem depender do rodapé.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShortcutChip(label = "Inputs", icon = Icons.Outlined.Input) { onNavigateTo("inputs") }
            ShortcutChip(label = "Simular", icon = Icons.Outlined.PlaylistAdd) { onNavigateTo("simulate") }
            ShortcutChip(label = "Dashboard", icon = Icons.Outlined.Assessment) { onNavigateTo("dashboard") }
        }
    }
}

@Composable
private fun ShortcutChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun InputsScreen(viewModel: FinanceViewModel) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var checkingText by remember { mutableStateOf(viewModel.checkingAccount.balance.toInputText()) }
    var salaryText by remember { mutableStateOf(viewModel.salary.amount.toInputText()) }
    var salaryDay by remember { mutableStateOf(viewModel.salary.dayOfMonth.toInputText()) }
    var cardInvoice by remember { mutableStateOf(viewModel.creditCardConfig.nextInvoiceAmount.toInputText()) }
    var cardDay by remember { mutableStateOf(viewModel.creditCardConfig.closingDay.toInputText()) }

    val showSavedMessage: (String) -> Unit = { message ->
        focusManager.clearFocus(force = true)
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            ScreenHeader(
                title = "Configurações financeiras",
                subtitle = "Organize saldos, rendimentos e datas com campos claros e confortáveis de preencher.",
                icon = Icons.Outlined.Settings
            )
        }
        item {
            SummaryCard(title = "Atualizar Conta Corrente", modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = checkingText,
                    onValueChange = { checkingText = it },
                    label = { Text("Saldo atual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    supportingText = { Text("Use ponto para separar centavos.") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            checkingText.toDoubleOrNull()?.let {
                                viewModel.updateCheckingBalance(it)
                                showSavedMessage("Saldo atualizado")
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
        item { Text("Caixinhas CDB", style = MaterialTheme.typography.titleMedium) }
        item { CaixinhaSection(viewModel, onSaved = { showSavedMessage(it) }) }
        item { Text("Vales", style = MaterialTheme.typography.titleMedium) }
        item { ValeSection(viewModel, onSaved = { showSavedMessage(it) }) }
        item { Text("Movimentações Padrão", style = MaterialTheme.typography.titleMedium) }
        item {
            SummaryCard(title = "Salário", modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = salaryText,
                        onValueChange = { salaryText = it },
                        label = { Text("Valor") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salaryDay,
                        onValueChange = { salaryDay = it },
                        label = { Text("Dia do mês") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val amount = salaryText.toDoubleOrNull()
                            val day = salaryDay.toIntOrNull()
                            if (amount != null && day != null) {
                                viewModel.updateSalary(amount, day)
                                showSavedMessage("Salário atualizado")
                            }
                        }
                    ) { Text("Salvar salário") }
                }
            }
        }
        item {
            SummaryCard(title = "Cartão de Crédito", modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = cardInvoice,
                        onValueChange = { cardInvoice = it },
                        label = { Text("Próxima fatura") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        supportingText = { Text("Valor previsto para o próximo fechamento.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cardDay,
                        onValueChange = { cardDay = it },
                        label = { Text("Dia fechamento") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val invoice = cardInvoice.toDoubleOrNull()
                            val day = cardDay.toIntOrNull()
                            if (invoice != null && day != null) {
                                viewModel.updateCreditCard(invoice, day)
                                showSavedMessage("Fatura atualizada")
                            }
                        }
                    ) { Text("Salvar fatura") }
                }
            }
        }
    }
}
}

@Composable
private fun CaixinhaSection(viewModel: FinanceViewModel, onSaved: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(title = "Nova caixinha", modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Saldo inicial") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    supportingText = { Text("Use ponto para centavos.") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        val amount = balance.toDoubleOrNull()
                        if (name.isNotBlank() && amount != null) {
                            viewModel.addCaixinha(name, amount)
                            name = ""
                            balance = ""
                            onSaved("Caixinha adicionada")
                        }
                    }
                ) { Text("Adicionar caixinha") }
            }
        }
        viewModel.caixinhas.forEach { caixinha ->
            SummaryCard(title = caixinha.name, value = caixinha.balance, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ID: ${caixinha.id.take(6)}...", style = MaterialTheme.typography.bodySmall)
                        Text("Atualize ou remova quando necessário.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { viewModel.removeCaixinha(caixinha.id) }) { Text("Remover") }
                }
            }
        }
    }
}

@Composable
private fun ValeSection(viewModel: FinanceViewModel, onSaved: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        viewModel.vales.forEach { vale ->
            var balance by remember(vale.id) { mutableStateOf(vale.balance.toInputText()) }
            var creditDay by remember(vale.id) { mutableStateOf(if (vale.creditDay == -1) "" else vale.creditDay.toString()) }
            var amount by remember(vale.id) { mutableStateOf(vale.amount.toInputText()) }
            SummaryCard(title = vale.label, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Valor padrão") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        label = { Text("Saldo atual") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = creditDay,
                    onValueChange = { creditDay = it },
                    label = { Text("Dia de crédito (vazio = penúltimo dia útil)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val current = balance.toDoubleOrNull()
                            val day = creditDay.toIntOrNull() ?: -1
                            val valeAmount = amount.toDoubleOrNull()
                            if (current != null && valeAmount != null) {
                                viewModel.updateVale(vale.id, current, day, valeAmount)
                                onSaved("Vale atualizado")
                            }
                        }
                    ) { Text("Atualizar vale") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SimulationScreen(viewModel: FinanceViewModel) {
    val today = remember { LocalDate.now() }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.DEBIT) }
    val dateSelections = remember { mutableStateListOf<DateSelection>() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf(SourceOption(AccountSource.CHECKING, "Conta Corrente")) }
    var transferEnabled by remember { mutableStateOf(false) }
    var transferDestination by remember { mutableStateOf<SourceOption?>(null) }

    val range = remember { today..today.plusDays(30) }
    val standardTransactions = remember(range) { viewModel.upcomingStandardTransactions(range) }
    val simulated = remember { mutableStateListOf(*viewModel.allSimulatedTransactions().toTypedArray()) }
    val selectedDates = dateSelections.flatMap { it.dates() }.distinct().sorted()
    val hasDateError = selectedDates.isEmpty()

    val caixinhaOptions = viewModel.caixinhas.map { SourceOption(AccountSource.CAIXINHAS, "Caixinha: ${it.name}") }
    val valeOptions = viewModel.vales.map { SourceOption(AccountSource.VALE, "Vale: ${it.label}") }
    val sourceOptions = listOf(
        SourceOption(AccountSource.CHECKING, "Conta Corrente"),
        SourceOption(AccountSource.CREDIT_CARD, "Cartão de crédito")
    ) + caixinhaOptions + valeOptions

    val transferDestinations = when (selectedSource.source) {
        AccountSource.CHECKING -> caixinhaOptions
        AccountSource.CAIXINHAS -> listOf(SourceOption(AccountSource.CHECKING, "Conta Corrente"))
        else -> emptyList()
    }

    if (selectedSource !in sourceOptions) {
        selectedSource = sourceOptions.first()
    }

    if (transferDestination !in transferDestinations) {
        transferDestination = transferDestinations.firstOrNull()
    }

    LaunchedEffect(viewModel) {
        simulated.clear()
        simulated.addAll(viewModel.allSimulatedTransactions())
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = today.toEpochMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = dateState.selectedDateMillis?.toLocalDate()
                        if (selected != null) {
                            dateSelections.add(SingleDateSelection(selected))
                        }
                        showDatePicker = false
                    }
                ) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = dateState,
                title = { Text("Escolha uma data") }
            )
        }
    }

    if (showRangePicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            colors = DatePickerDefaults.colors(),
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = rangeState.selectedStartDateMillis
                        val end = rangeState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            val startDate = start.toLocalDate()
                            val endDate = end.toLocalDate()
                            val normalizedStart = minOf(startDate, endDate)
                            val normalizedEnd = maxOf(startDate, endDate)
                            dateSelections.add(RangeDateSelection(normalizedStart, normalizedEnd))
                        }
                        showRangePicker = false
                    }
                ) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(
                state = rangeState,
                title = { Text("Selecione um período") }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Simular transações",
                subtitle = "Planeje movimentos futuros com campos legíveis e atalhos rápidos para escolher origem.",
                icon = Icons.Outlined.PlaylistAdd
            )
        }
        item {
            SummaryCard(title = "Configurar transação simulada", modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Valor") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Datas e períodos", style = MaterialTheme.typography.titleSmall)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dateSelections.forEachIndexed { index, selection ->
                            AssistChip(
                                onClick = { dateSelections.removeAt(index) },
                                label = { Text(selection.label()) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                            )
                        }
                        if (dateSelections.isEmpty()) {
                            Text(
                                "Nenhuma data selecionada",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showDatePicker = true }) { Text("Adicionar data") }
                        OutlinedButton(onClick = { showRangePicker = true }) { Text("Adicionar período") }
                    }
                    val summaryText = if (selectedDates.isEmpty()) {
                        "Selecione pelo menos uma data ou período para aplicar a transação."
                    } else {
                        val preview = selectedDates.take(3).joinToString { it.format(dateFormatter) }
                        val extra = selectedDates.size - selectedDates.take(3).size
                        val suffix = if (extra > 0) " +$extra" else ""
                        "Serão aplicadas ${selectedDates.size} datas ($preview$suffix)"
                    }
                    Text(
                        summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("Tipo", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == TransactionType.DEBIT, onClick = { type = TransactionType.DEBIT; transferEnabled = false }, label = { Text("Débito") })
                    FilterChip(selected = type == TransactionType.CREDIT, onClick = { type = TransactionType.CREDIT; transferEnabled = false }, label = { Text("Crédito") })
                }
                Text("Origem", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sourceOptions.forEach { option ->
                        FilterChip(
                            selected = selectedSource == option,
                            onClick = {
                                selectedSource = option
                                if (transferEnabled && transferDestination !in transferDestinations) {
                                    transferDestination = transferDestinations.firstOrNull()
                                }
                            },
                            label = { Text(option.label) }
                        )
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Transferência entre contas", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Ative para mover valores entre a conta corrente e uma caixinha CDB sem criar duas transações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = transferEnabled,
                        onClick = {
                            transferEnabled = !transferEnabled
                            if (transferEnabled) {
                                type = TransactionType.DEBIT
                                if (transferDestination !in transferDestinations) {
                                    transferDestination = transferDestinations.firstOrNull()
                                }
                            }
                        },
                        label = { Text("Transferir") }
                    )
                    if (transferEnabled && transferDestinations.isEmpty()) {
                        Text(
                            "Escolha uma origem compatível (conta ou caixinha) para liberar as opções de destino.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (transferEnabled && transferDestinations.isNotEmpty()) {
                    Text("Enviar para", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transferDestinations.forEach { option ->
                            FilterChip(
                                selected = transferDestination == option,
                                onClick = { transferDestination = option },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val numeric = amount.toDoubleOrNull()
                            val destinationReady = when {
                                !transferEnabled -> true
                                transferDestinations.isEmpty() -> false
                                else -> transferDestination != null
                            }
                            if (name.isNotBlank() && numeric != null && selectedDates.isNotEmpty() && destinationReady) {
                                viewModel.addSimulatedTransaction(
                                    SimulatedTransactionInput(
                                        name = name,
                                        amount = numeric,
                                        dates = selectedDates,
                                        type = if (transferEnabled) TransactionType.DEBIT else type,
                                        source = selectedSource.source,
                                        destination = if (transferEnabled) transferDestination?.source else null
                                    )
                                )
                                simulated.clear()
                                simulated.addAll(viewModel.allSimulatedTransactions())
                                name = ""
                                amount = ""
                            }
                        }
                    ) { Text("Adicionar simulação") }
                }
            }
        }
        item {
            SummaryCard(
                title = "Futuras transações padrão",
                modifier = Modifier.fillMaxWidth(),
                collapsible = true,
                initiallyExpanded = false
            ) {
                standardTransactions.forEach { event ->
                    TransactionRow(title = event.name, subtitle = event.date.format(dateFormatter), amount = event.amount, positive = event.type == TransactionType.CREDIT)
                }
            }
        }
        item {
            SummaryCard(
                title = "Transações simuladas",
                modifier = Modifier.fillMaxWidth(),
                collapsible = true,
                initiallyExpanded = false
            ) {
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
    val today = remember { LocalDate.now() }
    val defaultEnd = remember {
        val candidate = viewModel.nextSalaryDate(today).minusDays(1)
        if (candidate.isBefore(today)) today else candidate
    }
    var range by remember { mutableStateOf(today..defaultEnd) }
    var showPicker by remember { mutableStateOf(false) }
    val dateRangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = range.start.toEpochMillis(),
        initialSelectedEndDateMillis = range.endInclusive.toEpochMillis()
    )

    val insights = viewModel.dashboardInsights(range)
    val history = viewModel.balances(range)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Dashboard",
                subtitle = "Visualize tendências e ajuste o período para entender a saúde financeira.",
                icon = Icons.Outlined.Assessment
            )
        }
        item {
            SummaryCard(title = "Período de análise", modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mostrando de ${range.start.format(dateFormatter)} até ${range.endInclusive.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showPicker = true }) {
                        Text("Escolher no calendário")
                    }
                    TextButton(onClick = { range = today..defaultEnd }) {
                        Text("Usar padrão")
                    }
                }
            }
        }
        item { InsightsSection(insights) }
        if (history.isNotEmpty()) {
            item { Text("Faixas por conta", style = MaterialTheme.typography.titleMedium) }
            item { AccountBandsChart(history) }
            item { Text("Variação percentual", style = MaterialTheme.typography.titleMedium) }
            item { VariationSection(history) }
        }
    }
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangeState.selectedStartDateMillis?.toLocalDate()
                        val end = dateRangeState.selectedEndDateMillis?.toLocalDate()
                        if (start != null && end != null && !end.isBefore(start)) {
                            range = start..end
                            showPicker = false
                        }
                    }
                ) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(
                state = dateRangeState,
                title = { Text("Selecione o período") }
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: Double? = null,
    highlightNegative: Boolean = false,
    modifier: Modifier = Modifier,
    collapsible: Boolean = false,
    initiallyExpanded: Boolean = true,
    content: @Composable (() -> Unit)? = null
) {
    val hasContent = content != null
    val canToggle = collapsible && hasContent
    val startExpanded = if (canToggle) initiallyExpanded else true
    var expanded by remember { mutableStateOf(startExpanded) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (canToggle) it.clickable { expanded = !expanded } else it },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    value?.let {
                        val color = if (it < 0 || highlightNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        Text(
                            currencyFormat.format(it),
                            color = color,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                if (canToggle) {
                    val rotation = if (expanded) 0f else -90f
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (expanded) "Recolher" else "Expandir",
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (hasContent && expanded) {
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                content?.invoke()
            }
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
    val dateWeight = 1.2f
    val valueWeight = 1f
    val headerStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
    val numericStyle = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum", textAlign = TextAlign.End)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dia", modifier = Modifier.weight(dateWeight), style = headerStyle, maxLines = 1, softWrap = false)
                Text("CC", modifier = Modifier.weight(valueWeight), style = headerStyle, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                Text("Caixinhas", modifier = Modifier.weight(valueWeight), style = headerStyle, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                Text("Vales", modifier = Modifier.weight(valueWeight), style = headerStyle, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                Text("Cartão", modifier = Modifier.weight(valueWeight), style = headerStyle, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
            }
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            history.forEachIndexed { index, snapshot ->
                val checkingColor = if (snapshot.checking >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val caixinhaColor = if (snapshot.caixinhaTotal >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val valeColor = if (snapshot.valeTotal >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                val cardColor = if (snapshot.cardDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        snapshot.date.format(dateFormatter),
                        modifier = Modifier.weight(dateWeight),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        currencyFormat.format(snapshot.checking),
                        modifier = Modifier.weight(valueWeight),
                        color = checkingColor,
                        style = numericStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                    Text(
                        currencyFormat.format(snapshot.caixinhaTotal),
                        modifier = Modifier.weight(valueWeight),
                        color = caixinhaColor,
                        style = numericStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                    Text(
                        currencyFormat.format(snapshot.valeTotal),
                        modifier = Modifier.weight(valueWeight),
                        color = valeColor,
                        style = numericStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                    Text(
                        currencyFormat.format(-snapshot.cardDebt),
                        modifier = Modifier.weight(valueWeight),
                        color = cardColor,
                        style = numericStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                }
                if (index < history.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
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
                val percent = if (insight.startValue != 0.0) (diff / insight.startValue) * 100 else 0.0
                Text(
                    text = String.format(Locale("pt", "BR"), "%.1f%%", percent),
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Início: ${currencyFormat.format(insight.startValue)}")
                Text("Fim: ${currencyFormat.format(insight.endValue)}", fontWeight = FontWeight.Bold)
                Text("Variação: ${currencyFormat.format(diff)}", color = color)
            }
        }
    }
}

@Composable
private fun AccountBandsChart(history: List<BalanceSnapshot>) {
    if (history.isEmpty()) return
    val checkingData = history.map { it.date to it.checking }
    val caixinhaData = history.map { it.date to it.caixinhaTotal }
    val max = maxOf(checkingData.maxOf { it.second }, caixinhaData.maxOf { it.second })
    val min = minOf(checkingData.minOf { it.second }, caixinhaData.minOf { it.second })
    val span = (max - min).takeIf { it != 0.0 } ?: 1.0
    val checkingColor = MaterialTheme.colorScheme.primary
    val caixinhaColor = MaterialTheme.colorScheme.tertiary
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = checkingColor, label = "Conta Corrente")
                LegendDot(color = caixinhaColor, label = "Caixinhas Total")
            }
            Canvas(modifier = Modifier.height(180.dp)) {
                val widthStep = size.width / (history.size - 1).coerceAtLeast(1)
                fun yFor(value: Double): Float = size.height - (((value - min) / span).toFloat() * size.height)

                fun drawSeries(data: List<Pair<LocalDate, Double>>, color: Color) {
                    var lastPoint: androidx.compose.ui.geometry.Offset? = null
                    data.forEachIndexed { index, pair ->
                        val x = widthStep * index
                        val y = yFor(pair.second)
                        val point = androidx.compose.ui.geometry.Offset(x, y)
                        lastPoint?.let { previous ->
                            drawLine(color = color.copy(alpha = 0.7f), start = previous, end = point, strokeWidth = 6f)
                        }
                        drawCircle(color = color, radius = 6f, center = point)
                        lastPoint = point
                    }
                }

                drawSeries(checkingData, checkingColor)
                drawSeries(caixinhaData, caixinhaColor)
            }
        }
    }
}

@Composable
private fun VariationSection(history: List<BalanceSnapshot>) {
    val checkingPoints = variationPoints(history) { it.checking }
    val caixinhaPoints = variationPoints(history) { it.caixinhaTotal }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VariationChart(title = "Conta Corrente", points = checkingPoints, baseColor = MaterialTheme.colorScheme.primary)
        VariationChart(title = "Caixinhas Total", points = caixinhaPoints, baseColor = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun VariationChart(title: String, points: List<VariationPoint>, baseColor: Color) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { max(it.vsInitial, it.vsPrevious) }
    val minValue = points.minOf { min(it.vsInitial, it.vsPrevious) }
    val scale = max(abs(maxValue), abs(minValue)).takeIf { it != 0.0 } ?: 1.0
    val vsInitialColor = baseColor
    val vsPreviousColor = MaterialTheme.colorScheme.secondary
    val neutralLineColor = MaterialTheme.colorScheme.outlineVariant

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = vsInitialColor, label = "Vs início")
                LegendDot(color = vsPreviousColor, label = "Vs dia anterior")
            }
            Canvas(modifier = Modifier.height(200.dp)) {
                val widthStep = size.width / (points.size - 1).coerceAtLeast(1)
                val centerY = size.height / 2f
                drawLine(
                    color = neutralLineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                    strokeWidth = 2f
                )

                fun yFor(value: Double): Float = centerY - ((value / scale).toFloat() * centerY)

                fun drawSeries(values: List<Double>, color: Color) {
                    var lastPoint: androidx.compose.ui.geometry.Offset? = null
                    values.forEachIndexed { index, value ->
                        val x = widthStep * index
                        val y = yFor(value)
                        val point = androidx.compose.ui.geometry.Offset(x, y)
                        lastPoint?.let { previous ->
                            drawLine(color = color.copy(alpha = 0.7f), start = previous, end = point, strokeWidth = 5f)
                        }
                        drawCircle(color = color, radius = 6f, center = point)
                        lastPoint = point
                    }
                }

                drawSeries(points.map { it.vsInitial }, vsInitialColor)
                drawSeries(points.map { it.vsPrevious }, vsPreviousColor)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = RoundedCornerShape(50)))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun variationPoints(
    history: List<BalanceSnapshot>,
    selector: (BalanceSnapshot) -> Double
): List<VariationPoint> {
    if (history.isEmpty()) return emptyList()
    val startValue = selector(history.first())
    return history.mapIndexed { index, snapshot ->
        val current = selector(snapshot)
        val previous = if (index == 0) current else selector(history[index - 1])
        VariationPoint(
            date = snapshot.date,
            vsInitial = percentageChange(current, startValue),
            vsPrevious = if (index == 0) 0.0 else percentageChange(current, previous)
        )
    }
}

private fun percentageChange(current: Double, base: Double): Double =
    if (base == 0.0) 0.0 else ((current - base) / base) * 100

private data class VariationPoint(val date: LocalDate, val vsInitial: Double, val vsPrevious: Double)

private data class SourceOption(val source: AccountSource, val label: String)

private sealed class DateSelection {
    abstract fun dates(): List<LocalDate>
    abstract fun label(): String
}

private data class SingleDateSelection(val date: LocalDate) : DateSelection() {
    override fun dates(): List<LocalDate> = listOf(date)

    override fun label(): String = date.format(dateFormatter)
}

private data class RangeDateSelection(val start: LocalDate, val end: LocalDate) : DateSelection() {
    override fun dates(): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var pointer = start
        while (!pointer.isAfter(end)) {
            dates.add(pointer)
            pointer = pointer.plusDays(1)
        }
        return dates
    }

    override fun label(): String = "${start.format(dateFormatter)} - ${end.format(dateFormatter)}"
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
