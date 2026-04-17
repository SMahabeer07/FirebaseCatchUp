package com.fake.firebasebasics.repository

import android.content.ContentResolver
import android.net.Uri
import com.fake.firebasebasics.SwapItem
import com.fake.firebasebasics.SupabaseProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class SwapRepository {
    private val db = FirebaseDatabase.getInstance().getReference("listings")
    private val bucketName = "product-images"

    suspend fun uploadItem(
        title: String,
        price: String,
        imageUri: Uri,
        contentResolver: ContentResolver
    ) = withContext(Dispatchers.IO) {

        // 1. Get User ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        // 2. Read bytes from the URI
        val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read image data")

        // 3. Upload to Supabase
        val fileName = "${UUID.randomUUID()}.jpg"
        val path = "uploads/$fileName"
        val storage = SupabaseProvider.client.storage.from(bucketName)

        storage.upload(path, bytes) { upsert = true }

        // 4. Get Public URL
        val publicUrl = storage.publicUrl(path)

        // 5. Save to Firebase with userId
        val itemId = db.push().key ?: UUID.randomUUID().toString()
        val item = SwapItem(
            id = itemId,
            userId = userId,
            title = title,
            price = price,
            imageUrl = publicUrl
        )

        db.child(itemId).setValue(item).await()
    }
}