package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.essampaio.nearnode.R
import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.presentation.component.ProfileAvatar
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import br.com.essampaio.nearnode.ui.theme.TextPrimary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit,
    viewModel: NewChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stop()
        }
    }

    NewChatContent(
        state = state,
        onBackClick = onBackClick,
        onContactClick = onContactClick,
        onAction = { action->
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatContent(
    state: NewChatState,
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit,
    onAction: (NewChatAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.safeContentPadding(),
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
                },
                actions = {
                    Button(
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = TextPrimary,
                        ),
                        onClick = {
                            if(state.isDiscovering){
                                onAction(NewChatAction.StopDiscovery)
                            }else {
                                onAction(NewChatAction.StartDiscovery)
                            }
                        }) {
                        Text(
                            text =  if(state.isDiscovering)
                                    stringResource(R.string.list_contact_stop_discover)
                            else
                                stringResource(R.string.list_contact_discover)
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
            if (state.isDiscovering) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.list_contact_search_placeholder),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.contacts) { node ->
                    ContactItem(node, onContactClick)
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    node: Profile,
    onContactClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick(node.username) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(size = 48.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = node.username,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = node.ip,
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
            state = NewChatState(
                contacts = listOf(
                    Profile(id="123", username = "Vincent Nelson", ip = "192.168.1.1", status = AvailableStatus.ONLINE),
                    Profile(id="abc", username = "Francis Palmer", ip = "192.168.1.2", status = AvailableStatus.OFFLINE),
                ),
                isDiscovering = true
            ),
            onBackClick = {},
            onContactClick = {},
            onAction = {}
        )
    }
}
