package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.essampaio.nearnode.R
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.presentation.component.ProfileAvatar
import br.com.essampaio.nearnode.ui.theme.GrayLight
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import br.com.essampaio.nearnode.ui.theme.Primary
import br.com.essampaio.nearnode.ui.theme.Secondary
import br.com.essampaio.nearnode.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit,
    viewModel: NewChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start()
//        delay(3000)
//        viewModel.onAction(ViewContactsViewModelAction.StartSearch)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDiscovery()
        }
    }

    NewChatContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onContactClick = onContactClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatContent(
    uiState: NewChatUiState,
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_chat_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.new_chat_back_desc)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading ) {
                Row (modifier = Modifier.fillMaxWidth()
                    .background(Primary)
                    .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = stringResource(R.string.searching),
                        color= TextPrimary)
                    Spacer(modifier = Modifier.size(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary
                    )
                }
            }
//            } else if (uiState.contacts.isEmpty()) {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(stringResource(R.string.new_chat_no_contacts), color = Color.Gray)
//                }
//            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.contacts) { node ->
                        ContactItem(node, onContactClick)

                    }
                }
//            }
        }
    }
}

@Composable
fun ContactItem(
    node: Node,
    onContactClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick(node.name) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(size = 48.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column() {
            Text(
                text = node.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = node.ipAddress,
                fontSize = 12.sp,
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewChatPreview() {
    NearNodeTheme {
        NewChatContent(
            uiState = NewChatUiState(
                contacts = listOf(
                    Node("Vincent Nelson", "192.168.1.1", 9876),
                    Node("Francis Palmer", "192.168.1.2", 9876)
                ),
                isLoading = true
            ),
            onBackClick = {},
            onContactClick = {}
        )
    }
}
