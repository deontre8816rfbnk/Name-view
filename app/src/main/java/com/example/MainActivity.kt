package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainDatabaseScreen
import com.example.ui.UnlinkedScreen
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LexendFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate900
import com.example.viewmodel.MainDatabaseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainDatabaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            !uiState.isLinked -> {
                                UnlinkedScreen(
                                    onLinkFile = { uri -> viewModel.linkFile(uri) }
                                )
                            }
                            uiState.errorMessage != null -> {
                                ErrorScreen(
                                    errorMessage = uiState.errorMessage ?: "Unknown error",
                                    onRetry = { viewModel.refresh() },
                                    onReLink = { viewModel.unlinkFile() }
                                )
                            }
                            else -> {
                                MainDatabaseScreen(
                                    uiState = uiState,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onTagSelect = { viewModel.setSelectedTag(it) },
                                    onToggleSort = { viewModel.toggleSortOrder() },
                                    onAddEntry = { viewModel.addEntry(it) },
                                    onUpdateEntry = { old, new -> viewModel.updateEntry(old, new) },
                                    onDeleteEntry = { viewModel.deleteEntry(it) },
                                    onRefresh = { viewModel.refresh() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onReLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Could Not Open Markdown File",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                fontFamily = LexendFontFamily,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Retry", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onReLink,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Choose Different File", fontFamily = LexendFontFamily)
            }
        }
    }
}
