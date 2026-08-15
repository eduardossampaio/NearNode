package br.com.essampaio.nearnode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.essampaio.nearnode.presentation.MainViewModel
import br.com.essampaio.nearnode.presentation.navigation.Route
import br.com.essampaio.nearnode.presentation.screen.chat.ChatScreen
import br.com.essampaio.nearnode.presentation.screen.listcontact.ListContactScreen
import br.com.essampaio.nearnode.presentation.screen.newchat.NewChatScreen
import br.com.essampaio.nearnode.presentation.screen.registration.RegistrationScreen
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearNodeTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: MainViewModel = koinViewModel()) {
    val startDestination by viewModel.startDestination.collectAsState()
    val navController = rememberNavController()

    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Route.Registration> {
                RegistrationScreen(
                    onRegistrationSuccess = {
                        navController.navigate(Route.ListContact) {
                            popUpTo(Route.Registration) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.ListContact> {
                ListContactScreen(
                    onContactClick = { contactId ->
                        navController.navigate(Route.Chat(contactId))
                    },
                    onNewChatClick = {
                        navController.navigate(Route.NewChat)
                    }
                )
            }

            composable<Route.NewChat> {
                NewChatScreen(
                    onBackClick = { navController.popBackStack() },
                    onContactClick = { contactId ->
                        navController.navigate(Route.Chat(contactId)) {
                            popUpTo(Route.NewChat) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Chat> { backStackEntry ->
                val chat: Route.Chat = backStackEntry.toRoute()
                ChatScreen(
                    contactId = chat.contactId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
