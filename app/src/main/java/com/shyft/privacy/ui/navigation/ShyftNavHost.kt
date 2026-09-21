package com.shyft.privacy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.screens.audioprivacy.AudioPrivacyScreen
import com.shyft.privacy.ui.screens.dashboard.HomeDashboardScreen
import com.shyft.privacy.ui.screens.demo.CallDemoScreen
import com.shyft.privacy.ui.screens.demo.DemoModeScreen
import com.shyft.privacy.ui.screens.demo.MediaDemoScreen
import com.shyft.privacy.ui.screens.demo.PaymentDemoScreen
import com.shyft.privacy.ui.screens.protectedapps.ProtectedAppsScreen
import com.shyft.privacy.ui.screens.protectedapps.ProtectedAppsViewModel
import com.shyft.privacy.ui.screens.sensors.SensorsScreen
import com.shyft.privacy.ui.screens.settings.SettingsScreen
import com.shyft.privacy.ui.screens.visualprivacy.VisualPrivacyScreen
import com.shyft.privacy.ui.screens.zones.PrivacyZonesScreen

@Composable
fun ShyftNavHost(
    navController: NavHostController,
    protectedAppsViewModel: ProtectedAppsViewModel,
    privacyStateRepository: IPrivacyStateRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.HomeDashboard.route,
        modifier = modifier
    ) {
        composable(ScreenRoute.HomeDashboard.route) {
            HomeDashboardScreen(
                protectedAppsViewModel = protectedAppsViewModel,
                privacyStateRepository = privacyStateRepository,
                onNavigateToProtectedApps = {
                    navController.navigate(ScreenRoute.ProtectedApps.route)
                },
                onNavigateToDemoMode = {
                    navController.navigate(ScreenRoute.DemoMode.route)
                },
                onNavigateToVisualPrivacy = {
                    navController.navigate(ScreenRoute.VisualPrivacy.route)
                },
                onNavigateToAudioPrivacy = {
                    navController.navigate(ScreenRoute.AudioPrivacy.route)
                }
            )
        }

        composable(ScreenRoute.ProtectedApps.route) {
            ProtectedAppsScreen(viewModel = protectedAppsViewModel)
        }

        composable(ScreenRoute.VisualPrivacy.route) {
            VisualPrivacyScreen(
                privacyStateRepository = privacyStateRepository,
                onNavigateToPaymentDemo = {
                    navController.navigate(ScreenRoute.PaymentDemo.route)
                }
            )
        }

        composable(ScreenRoute.AudioPrivacy.route) {
            AudioPrivacyScreen(
                privacyStateRepository = privacyStateRepository,
                onNavigateToCallDemo = {
                    navController.navigate(ScreenRoute.CallDemo.route)
                },
                onNavigateToMediaDemo = {
                    navController.navigate(ScreenRoute.MediaDemo.route)
                }
            )
        }

        composable(ScreenRoute.PrivacyZones.route) {
            PrivacyZonesScreen(privacyStateRepository = privacyStateRepository)
        }

        composable(ScreenRoute.Sensors.route) {
            SensorsScreen(privacyStateRepository = privacyStateRepository)
        }

        composable(ScreenRoute.DemoMode.route) {
            DemoModeScreen(
                privacyStateRepository = privacyStateRepository,
                onNavigateToProtectedApps = { navController.navigate(ScreenRoute.ProtectedApps.route) },
                onNavigateToVisualPrivacy = { navController.navigate(ScreenRoute.VisualPrivacy.route) },
                onNavigateToPaymentDemo = { navController.navigate(ScreenRoute.PaymentDemo.route) },
                onNavigateToMediaDemo = { navController.navigate(ScreenRoute.MediaDemo.route) },
                onNavigateToCallDemo = { navController.navigate(ScreenRoute.CallDemo.route) },
                onNavigateToSensors = { navController.navigate(ScreenRoute.Sensors.route) }
            )
        }

        composable(ScreenRoute.PaymentDemo.route) {
            PaymentDemoScreen(privacyStateRepository = privacyStateRepository)
        }

        composable(ScreenRoute.CallDemo.route) {
            CallDemoScreen(privacyStateRepository = privacyStateRepository)
        }

        composable(ScreenRoute.MediaDemo.route) {
            MediaDemoScreen(privacyStateRepository = privacyStateRepository)
        }

        composable(ScreenRoute.Settings.route) {
            SettingsScreen()
        }
    }
}
