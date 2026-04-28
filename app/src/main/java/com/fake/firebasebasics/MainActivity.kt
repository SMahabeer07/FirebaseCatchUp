package com.fake.firebasebasics

import android.R.attr.delay
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fake.firebasebasics.ui.theme.FirebaseBasicsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseBasicsTheme {
                val authVm: AuthViewModel = viewModel()
                val swapVm: SwapViewModel = viewModel()
                val authState by authVm.uiState.collectAsState()

                if (authState.isAuthenticated) {
                    SwapShopScreen(swapVm) { authVm.logout() }
                } else {
                    AuthScreen(authVm) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapShopScreen(vm: SwapViewModel, onLogout: () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Items") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    }
                }
            )
        },
        // FIX 1: Move SnackbarHost here. Scaffold handles alignment automatically.
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            if (vm.itemList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vm.itemList) { item ->
                        Card(Modifier.fillMaxWidth().clickable {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Hello there",
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Short
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    println("Snackbar action clicked")
                                }
                            }
                        }) {
                            Column {
                                AsyncImage(item.imageUrl, null, Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
                                Text(item.title, Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                                Text("R${item.price}", Modifier.padding(start = 8.dp, bottom = 8.dp))
                            }
                        }
                    }
                }
            }
            if (vm.isUploading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                AddItemSheetContent { t, pr, uri ->
                    vm.uploadNewListing(t, pr, uri, context.contentResolver)
                    showSheet = false
                }
            }
        }
    }
}

@Composable
fun AddItemSheetContent(onUpload: (String, String, Uri) -> Unit) {
    var t by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri = it }

    // FIX 2: Corrected the Alignment/Arrangement confusion
    Column(
        Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(t, { t = it }, label = { Text("Title") })
        OutlinedTextField(p, { p = it }, label = { Text("Price") })
        Button(onClick = { picker.launch("image/*") }) {
            Text(if (uri == null) "Select Image" else "Image Selected")
        }
        Button(
            onClick = { onUpload(t, p, uri!!) },
            enabled = t.isNotBlank() && p.isNotBlank() && uri != null
        ) {
            Text("Post")
        }
    }
}