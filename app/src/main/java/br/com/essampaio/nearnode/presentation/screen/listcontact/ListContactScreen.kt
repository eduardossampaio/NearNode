package br.com.essampaio.nearnode.presentation.screen.listcontact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ListContactScreen(
    onContactClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ListContactViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ListContactContent(
        uiState = uiState,
        onContactClick = onContactClick,
        onNewChatClick = onNewChatClick
    )
}


@Composable
fun ListContactContent(
    uiState: ListContactUiState,
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "My Messages",
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
                Icon(Icons.Default.Search, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent",
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
                items(uiState.conversations) { conversation ->
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
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
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
                text = "15 Mins", // TODO: Format timestamp
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
            uiState = ListContactUiState(
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
