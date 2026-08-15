package br.com.essampaio.nearnode.presentation.screen.listcontact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
import br.com.essampaio.nearnode.presentation.component.NearNodeTextField
import br.com.essampaio.nearnode.presentation.component.ProfileAvatar
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ListContactScreen(
    onContactClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ListContactViewModel = koinViewModel()
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

    ListContactContent(
        state = state,
        onContactClick = onContactClick,
        onNewChatClick = onNewChatClick
    )
}

@Composable
fun ListContactContent(
    state: ListContactState,
    onContactClick: (String) -> Unit,
    onNewChatClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.list_contact_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.list_contact_new_chat_desc)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NearNodeTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.list_contact_search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.list_contact_recent_label),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.conversations) { conversation ->
                    ConversationItem(conversation, onContactClick)
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onContactClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick(conversation.contactId) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(size = 56.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.contactName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = conversation.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.list_contact_mins_ago, 15), // TODO: Format timestamp
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (conversation.unreadCount > 0) {
                Badge(containerColor = Color(0xFF4CAF50)) {
                    Text(text = conversation.unreadCount.toString(), color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListContactPreview() {
    NearNodeTheme {
        ListContactContent(
            state = ListContactState(
                conversations = listOf(
                    Conversation("1", "Vincent Nelson", "Hello how are you...?", 12345, 3),
                    Conversation("2", "Francis Palmer", "Hahahaha thanks, i didnt know", 12345, 0)
                )
            ),
            onContactClick = {},
            onNewChatClick = {}
        )
    }
}
