package br.com.essampaio.nearnode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearNodeTheme {
                MainScreen()

            }
        }
    }
}

@Composable
fun MainScreen(){
    val viewModel = koinViewModel<ViewContactsViewModel>()

    val state = viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.start()
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.close()
        }
    }
    MainScreenContent(state.value){action ->
        viewModel.onAction(action)
    }
}

@Composable
fun MainScreenContent(state: ViewContactsViewModelState,
                      onAction: (action: ViewContactsViewModelAction) -> Unit) {

    val isSearching = remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("status: ${state.status}")
            Button(onClick = {
                if(isSearching.value){
                    onAction(ViewContactsViewModelAction.StopSearch)
                }else{
                    onAction(ViewContactsViewModelAction.StartSearch)
                }
                isSearching.value = !isSearching.value

            }) { Text( if (isSearching.value) "Stop searching" else "Search") }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.availableContacts) { contact ->
                ContactItem(contact)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = contact.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = contact.ip,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NearNodeTheme {
        MainScreenContent(
            ViewContactsViewModelState(
                AvailableStatus.ONLINE,
                listOf(
                    Contact("User 1", "192.168.0.1"),
                    Contact("User 2", "192.168.0.2"),
                )
            )
        ){

        }
    }
}