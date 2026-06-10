package com.example.dosagecalc.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dosagecalc.presentation.calculator.CalculatorViewModel
import com.example.dosagecalc.presentation.calculator.RemindersViewModel
import com.example.dosagecalc.presentation.calculator.screen.AddDataScreen
import com.example.dosagecalc.presentation.calculator.screen.DosageResultScreen
import com.example.dosagecalc.presentation.calculator.screen.DrugSelectionScreen
import com.example.dosagecalc.presentation.calculator.screen.PatientInputScreen
import com.example.dosagecalc.presentation.calculator.screen.RemindersScreen
import com.example.dosagecalc.presentation.history.HistoryViewModel
import com.example.dosagecalc.presentation.history.screen.HistoryScreen
import com.example.dosagecalc.presentation.onboarding.OnboardingViewModel
import com.example.dosagecalc.presentation.onboarding.screen.OnboardingScreen
import com.example.dosagecalc.presentation.patient.PatientsViewModel
import com.example.dosagecalc.presentation.patient.screen.PatientsScreen

private fun NavHostController.safePop() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}

sealed class AppRoute(
    val route: String,
) {
    object Onboarding : AppRoute("onboarding")

    object DrugSelection : AppRoute("drug_selection")

    object PatientInput : AppRoute("patient_input")

    object DosageResult : AppRoute("dosage_result")

    object PatientsList : AppRoute("patients_list")

    object GlobalHistory : AppRoute("global_history")

    object HistoryAnalytics : AppRoute("history_analytics")

    object Reminders : AppRoute("reminders")

    object AddData : AppRoute("add_data")

    object DrugDetail : AppRoute("drug_detail")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onToggleTheme: () -> Unit = {},
) {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val onboardingDone by onboardingVm.isCompleted.collectAsStateWithLifecycle()

    // Show a loading screen until DataStore emits its first value
    if (onboardingDone == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination =
        if (onboardingDone == true) {
            AppRoute.DrugSelection.route
        } else {
            AppRoute.Onboarding.route
        }

    val viewModel: CalculatorViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280)) + fadeIn(tween(280)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(280)) + fadeOut(tween(280)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280)) + fadeIn(tween(280)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280)) + fadeOut(tween(280)) },
    ) {
        composable(route = AppRoute.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    onboardingVm.completeOnboarding()
                    navController.navigate(AppRoute.DrugSelection.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(route = AppRoute.DrugSelection.route) {
            DrugSelectionScreen(
                viewModel = viewModel,
                onNavigateToInput = { navController.navigate(AppRoute.PatientInput.route) },
                onNavigateToPatients = { navController.navigate(AppRoute.PatientsList.route) },
                onNavigateToHistory = { navController.navigate(AppRoute.GlobalHistory.route) },
                onNavigateToReminders = { navController.navigate(AppRoute.Reminders.route) },
                onNavigateToAddData = { drugId ->
                    if (drugId != null) {
                        navController.navigate(AppRoute.AddData.route + "?drugId=$drugId")
                    } else {
                        navController.navigate(AppRoute.AddData.route)
                    }
                },
                onNavigateToDetail = { drugId ->
                    navController.navigate(AppRoute.DrugDetail.route + "/$drugId")
                },
                onToggleTheme = onToggleTheme,
            )
        }

        composable(route = AppRoute.PatientsList.route) {
            val patientsViewModel: PatientsViewModel = hiltViewModel()
            PatientsScreen(
                viewModel = patientsViewModel,
                onNavigateBack = { navController.safePop() },
                onNavigateToHistory = { patientId ->
                    navController.navigate(AppRoute.GlobalHistory.route + "?patientId=$patientId")
                },
            )
        }

        composable(
            route = AppRoute.GlobalHistory.route + "?patientId={patientId}",
            arguments =
                listOf(
                    navArgument("patientId") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                patientId = patientId,
                onNavigateBack = { navController.safePop() },
                onNavigateToAnalytics = {
                    val route =
                        if (patientId != null) {
                            AppRoute.HistoryAnalytics.route + "?patientId=$patientId"
                        } else {
                            AppRoute.HistoryAnalytics.route
                        }
                    navController.navigate(route)
                },
            )
        }

        composable(
            route = AppRoute.HistoryAnalytics.route + "?patientId={patientId}",
            arguments =
                listOf(
                    navArgument("patientId") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val historyViewModel: HistoryViewModel = hiltViewModel()
            com.example.dosagecalc.presentation.history.screen.HistoryAnalyticsScreen(
                historyViewModel = historyViewModel,
                calculatorViewModel = viewModel,
                initialPatientId = patientId,
                onNavigateBack = { navController.safePop() },
            )
        }

        composable(route = AppRoute.Reminders.route) {
            val remindersViewModel: RemindersViewModel = hiltViewModel()
            RemindersScreen(
                viewModel = remindersViewModel,
                onNavigateBack = { navController.safePop() },
            )
        }

        composable(
            route = AppRoute.AddData.route + "?drugId={drugId}",
            arguments =
                listOf(
                    navArgument("drugId") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
        ) { backStackEntry ->
            AddDataScreen(
                drugId = backStackEntry.arguments?.getString("drugId"),
                onNavigateBack = { navController.safePop() },
            )
        }

        composable(
            route = AppRoute.DrugDetail.route + "/{drugId}",
            arguments = listOf(navArgument("drugId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val drugId = backStackEntry.arguments?.getString("drugId") ?: return@composable
            com.example.dosagecalc.presentation.calculator.screen.DrugDetailScreen(
                drugId = drugId,
                onNavigateBack = { navController.safePop() },
            )
        }

        composable(route = AppRoute.PatientInput.route) {
            PatientInputScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.safePop() },
                onNavigateToResult = { navController.navigate(AppRoute.DosageResult.route) },
            )
        }

        composable(route = AppRoute.DosageResult.route) {
            DosageResultScreen(
                viewModel = viewModel,
                onNewCalculation = {
                    navController.popBackStack(AppRoute.DrugSelection.route, inclusive = false)
                    viewModel.resetCalculation()
                },
                onNavigateBackToInput = { navController.safePop() },
            )
        }
    }
}
