package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShoppingListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ExpenseTrackerViewModel
import androidx.compose.material.icons.filled.ShoppingBag
import com.example.ui.LanguageHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: ExpenseTrackerViewModel = viewModel()
      
      // Theme toggling flow alignment
      val themeMode by viewModel.themeMode.collectAsState()
      val isDarkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        ExpenseTrackerApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun ExpenseTrackerApp(viewModel: ExpenseTrackerViewModel) {
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val expenses by viewModel.expenses.collectAsState(initial = emptyList())
  val filteredExpenses by viewModel.filteredHistoryExpenses.collectAsState(initial = emptyList())
  val categories by viewModel.categories.collectAsState(initial = emptyList())
  val budgets by viewModel.budgets.collectAsState(initial = emptyList())
  val shoppingItems by viewModel.shoppingItems.collectAsState(initial = emptyList())
  val lang by viewModel.languageMode.collectAsState()

  // Navigation track
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Bottom Nav items list mapping (removed settings)
  val navItems = listOf(
    NavigationItem("dashboard", "Home", Icons.Default.Dashboard),
    NavigationItem("history", "History", Icons.Default.History),
    NavigationItem("shopping_list", "Notes", Icons.Default.ShoppingBag),
    NavigationItem("analytics", "Analytics", Icons.Default.Analytics),
    NavigationItem("budgets", "Budgets", Icons.Default.Savings)
  )

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      // Show drawer content if not in "categories" or "login" config screen
      if (currentRoute != "categories" && currentRoute != "login") {
        ModalDrawerSheet {
          SettingsScreen(
            viewModel = viewModel,
            expenses = filteredExpenses,
            onNavigateToCategories = {
              scope.launch { drawerState.close() }
              navController.navigate("categories")
            },
            onLogout = {
              scope.launch { drawerState.close() }
              navController.navigate("login") {
                popUpTo(0) { inclusive = true }
              }
            }
          )
        }
      }
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        // Show bottom navigation bar if not in "categories" or "login" config screen
        if (currentRoute != "categories" && currentRoute != "login") {
          NavigationBar {
            navItems.forEach { item ->
              val isSelected = currentRoute == item.route
              NavigationBarItem(
                selected = isSelected,
                onClick = {
                  if (currentRoute != item.route) {
                    navController.navigate(item.route) {
                      popUpTo("dashboard") {
                        saveState = true
                      }
                      launchSingleTop = true
                      restoreState = true
                    }
                  }
                },
                icon = { Icon(item.icon, contentDescription = LanguageHelper.translate(item.title, lang)) },
                label = { Text(LanguageHelper.translate(item.title, lang)) }
              )
            }
          }
        }
      }
    ) { innerPadding ->
      val startDestination = remember { if (viewModel.isSupabaseLoggedIn()) "dashboard" else "login" }
      NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
      ) {
        composable("login") {
          LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
              navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
              }
            },
            onBypass = {
              navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
              }
            }
          )
        }

        composable("dashboard") {
          DashboardScreen(
            viewModel = viewModel,
            expenses = expenses,
            budgets = budgets,
            onNavigateToHistory = { navController.navigate("history") },
            onNavigateToBudgets = { navController.navigate("budgets") },
            onNavigateToShopping = { navController.navigate("shopping_list") },
            onOpenDrawer = { scope.launch { drawerState.open() } }
          )
        }

      composable("history") {
        HistoryScreen(
          viewModel = viewModel,
          filteredExpenses = filteredExpenses,
          categories = categories,
          onBack = { navController.popBackStack() }
        )
      }

      composable("shopping_list") {
        ShoppingListScreen(
          viewModel = viewModel,
          shoppingItems = shoppingItems,
          categories = categories,
          onBack = { navController.popBackStack() }
        )
      }

      composable("analytics") {
        AnalyticsScreen(
          viewModel = viewModel,
          expenses = expenses,
          onBack = { navController.popBackStack() }
        )
      }

      composable("budgets") {
        BudgetScreen(
          viewModel = viewModel,
          budgets = budgets,
          expenses = expenses,
          onBack = { navController.popBackStack() }
        )
      }

      composable("settings") {
        SettingsScreen(
          viewModel = viewModel,
          expenses = filteredExpenses,
          onNavigateToCategories = { navController.navigate("categories") },
          onLogout = {
            navController.navigate("login") {
              popUpTo(0) { inclusive = true }
            }
          }
        )
      }

      composable("categories") {
        CategoriesScreen(
          viewModel = viewModel,
          categories = categories,
          onBack = { navController.popBackStack() }
        )
      }
    }
  }
}
}

data class NavigationItem(
  val route: String,
  val title: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)
