package com.fake.firebasebasics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fake.firebasebasics.AuthViewModel
import androidx.compose.foundation.background

@Composable
fun AuthScreen(
    authVm: AuthViewModel = viewModel(),
    onAuthenticated: () -> Unit
) {
    val state by authVm.uiState.collectAsState()

    if (state.isAuthenticated) {
        onAuthenticated()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = authVm::onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = authVm::onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { authVm.login() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }

                    TextButton(
                        onClick = { authVm.register() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create account")
                    }
                }
            }
        }
    }
}