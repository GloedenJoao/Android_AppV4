package com.financeplanner.app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Input
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeplanner.app.data.FinanceViewModel
import com.financeplanner.app.ui.DashboardScreen
import com.financeplanner.app.ui.HomeScreen
import com.financeplanner.app.ui.InputsScreen
import com.financeplanner.app.ui.SimulationScreen
import com.financeplanner.app.ui.theme.FinancePlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinancePlannerTheme {
                val viewModel: FinanceViewModel = viewModel()
                FinancePlannerApp(viewModel = viewModel)
            }
        }
    }
}

data class Screen(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun FinancePlannerApp(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val screens = listOf(
        Screen("home", "Início") { Icon(Icons.Outlined.Home, contentDescription = null) },
        Screen("inputs", "Inputs") { Icon(Icons.Outlined.Input, contentDescription = null) },
        Screen("simulate", "Simular") { Icon(Icons.Outlined.PlaylistAdd, contentDescription = null) },
        Screen("dashboard", "Dashboard") { Icon(Icons.Outlined.Assessment, contentDescription = null) }
    )

    Scaffold(
        bottomBar = { BottomNav(navController, screens) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("inputs") { InputsScreen(viewModel) }
            composable("simulate") { SimulationScreen(viewModel) }
            composable("dashboard") { DashboardScreen(viewModel) }
        }
    }
}

@Composable
private fun BottomNav(navController: NavHostController, screens: List<Screen>) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) },
                icon = { screen.icon() },
                label = { Text(screen.label) }
            )
        }
    }
}
