package com.shyft.privacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shyft.privacy.data.repository.PrivacyStateRepository
import com.shyft.privacy.ui.navigation.ScreenRoute
import com.shyft.privacy.ui.navigation.ShyftNavHost
import com.shyft.privacy.ui.screens.protectedapps.ProtectedAppsViewModel
import com.shyft.privacy.ui.theme.SHYFTTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val protectedAppsViewModel: ProtectedAppsViewModel by viewModels()
    private val privacyStateRepository = PrivacyStateRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SHYFTTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoute.HomeDashboard.route

                val bottomNavItems = listOf(
                    ScreenRoute.HomeDashboard,
                    ScreenRoute.ProtectedApps,
                    ScreenRoute.VisualPrivacy,
                    ScreenRoute.AudioPrivacy,
                    ScreenRoute.DemoMode
                )

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "SHYFT Privacy",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Privacy that adapts to your surroundings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ScreenRoute.ALL_SCREENS.forEach { screen ->
                                NavigationDrawerItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, fontWeight = FontWeight.Medium) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    val currentScreen = ScreenRoute.ALL_SCREENS.find { it.route == currentRoute }
                                    Text(
                                        text = currentScreen?.title ?: "SHYFT",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentRoute == screen.route
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                                        label = { Text(screen.title) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        ShyftNavHost(
                            navController = navController,
                            protectedAppsViewModel = protectedAppsViewModel,
                            privacyStateRepository = privacyStateRepository,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        privacyStateRepository.startSensorEngine(this)
    }

    override fun onPause() {
        super.onPause()
        privacyStateRepository.stopSensorEngine()
    }

    override fun onStop() {
        super.onStop()
        // Ensure camera and microphone resources are cleanly released when exiting the application
        if (isFinishing) {
            privacyStateRepository.stopVisualPrivacyEngine()
            privacyStateRepository.stopAudioPrivacyEngine()
        }
    }
}
