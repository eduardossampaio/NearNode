package br.com.essampaio.nearnode.presentation.screen.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import br.com.essampaio.nearnode.presentation.component.NearNodeTextField
import br.com.essampaio.nearnode.presentation.component.ProfileAvatar
import br.com.essampaio.nearnode.ui.theme.NearNodeTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onRegistrationSuccess()
        }
    }

    RegistrationContent(
        uiState = uiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onRegisterClick = viewModel::register
    )
}

@Composable
fun RegistrationContent(
    uiState: RegistrationUiState,
    onUsernameChanged: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.registration_welcome),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.registration_description),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        ProfileAvatar(size = 120.dp)

        Spacer(modifier = Modifier.height(32.dp))

        NearNodeTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(R.string.registration_username_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !uiState.isLoading && uiState.username.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(R.string.registration_button_text),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationPreview() {
    NearNodeTheme {
        RegistrationContent(
            uiState = RegistrationUiState(username = "John Doe"),
            onUsernameChanged = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationLoadingPreview() {
    NearNodeTheme {
        RegistrationContent(
            uiState = RegistrationUiState(username = "John Doe", isLoading = true),
            onUsernameChanged = {},
            onRegisterClick = {}
        )
    }
}
