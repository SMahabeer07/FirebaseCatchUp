package com.fake.firebasebasics

import com.fake.firebasebasics.BuildConfig
//import io.github.jan.supabase.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Storage)
    }
}