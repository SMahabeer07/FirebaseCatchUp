package com.fake.firebasebasics

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fake.firebasebasics.repository.SwapRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class SwapViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().getReference("listings")
    private val repository = SwapRepository()
    private val auth = FirebaseAuth.getInstance()

    val itemList = mutableStateListOf<SwapItem>()
    var isUploading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var activeListener: ValueEventListener? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                listenForItems(uid)
            } else {
                itemList.clear()
                stopListening()
            }
        }
    }

    private fun listenForItems(uid: String) {
        stopListening()
        val query = db.orderByChild("userId").equalTo(uid)

        activeListener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(SwapItem::class.java)
                    if (item != null) itemList.add(item)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = error.message
            }
        })
    }

    private fun stopListening() {
        activeListener?.let {
            db.removeEventListener(it)
            activeListener = null
        }
    }

    fun uploadNewListing(title: String, price: String, uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                repository.uploadItem(title, price, uri, resolver)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isUploading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}