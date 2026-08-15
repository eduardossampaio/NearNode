package br.com.essampaio.nearnode.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import br.com.essampaio.nearnode.domain.model.Message
import br.com.essampaio.nearnode.presentation.component.NearNodeTextField
import br.com.essampaio.nearnode.presentation.component.ProfileAvatar
import br.com.essampaio.nearnode.ui.theme.BubbleReceived
import br.com.essampaio.nearnode.ui.theme.BubbleSent
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import br.com.essampaio.nearnode.ui.theme.StatusOnline
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatScreen(
    contactId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(contactId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendClick = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    uiState: ChatUiState,
    onBackClick: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileAvatar(size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = uiState.contactName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = stringResource(R.string.chat_active_now),
                                fontSize = 12.sp,
                                color = StatusOnline
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.new_chat_back_desc)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.chat_more_options_desc)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInput(
                text = uiState.messageText,
                onTextChanged = onMessageTextChanged,
                onSendClick = onSendClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(message = message, isMe = message.senderId == "me")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isMe) BubbleSent else BubbleReceived,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            ),
            tonalElevation = 1.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        tonalElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NearNodeTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_send_desc),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    NearNodeTheme {
        ChatContent(
            uiState = ChatUiState(
                messages = listOf(
                    Message("1", "contact", "me", "Hello Vincent, thank you for calling Provide.", 12345),
                    Message("2", "me", "contact", "Perfect, i am really glad to hear that!", 12345)
                ),
                contactName = "Vincent Nelson"
            ),
            onBackClick = {},
            onMessageTextChanged = {},
            onSendClick = {}
        )
    }
}
