package com.fake.firebasebasics

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fake.firebasebasics.ui.theme.FirebaseBasicsTheme

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
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            if (vm.itemList.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No items found") }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(vm.itemList) { item ->
                        Card(Modifier.fillMaxWidth()) {
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

    Column(Modifier.padding(16.dp).fillMaxWidth(),
        Alignment.CenterHorizontally as Arrangement.Vertical
    ) {
        OutlinedTextField(t, { t = it }, label = { Text("Title") })
        OutlinedTextField(p, { p = it }, label = { Text("Price") })
        Button(onClick = { picker.launch("image/*") }) { Text(if (uri == null) "Select Image" else "Image Selected") }
        Button(onClick = { onUpload(t, p, uri!!) }, enabled = t.isNotBlank() && p.isNotBlank() && uri != null) { Text("Post") }
    }
}