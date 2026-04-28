# Firebase Guide from Scratch for a Simple App

> **Note:** Your empty views activity should be made in Android Studio first.

## 1. Firebase Project Setup

Go to the Firebase Console and set up a new Firebase project.

Give your project a name. Remember that projects can store many apps so you can go ahead and name it **PROG7313** for all/most of this semester's apps. Accept terms by checking the box and decline to join the Google Dev program.

Next up is your choice. You can add Gemini to Firebase if you want but it is not necessary.

Say no to analytics, then hit **Create Project**.

## 2. Add Your Android App

Once the project has been created you can go ahead and add an app. Go to **Add App** then select the Android platform. For package name, copy and paste it from the top of `MainActivity.kt`. Next give your app a nickname.

After this you will get a `google-services.json` file to download. Download it and drag and drop it into your app folder as shown in Firebase.

Then go next, next and continue to console.

## 3. Android Studio Firebase Setup

Go to the spyglass and search **Firebase** to open the IDE's built-in tools. Select **Authentication** and authenticate using Google. Next click on **Connect to Firebase**. You should see a message saying the app is already connected — this verifies correct placement of the `google-services.json` file. Hit cancel.

Next, add the **Firebase Authentication SDK** to your app with the second button. You can do the same with the Realtime Database — click the option, get started, and go to step 2: **Add the Realtime Database SDK to your app**. Accept Gradle changes when prompted for both.

## 4. Supabase Setup

Now you have Firebase set up in your Android app. Next let's do Supabase. Make your account any way you like — just hit **Continue with GitHub**. Most things don't need to be changed in the initial setup, just make a password for your database and create your project.

## 5. Connecting Everything

Now we have the platforms and the mobile app set up. Go to Firebase and enable Auth and the Realtime DB, then go to Supabase and create your storage bucket and add the following policy going **field by field**.

> ⚠️ **DO NOT copy and paste the whole thing into the textbox at the bottom.** Also make sure to use `anon` if you do it this way. You can also run it directly in the SQL Editor:

```sql
CREATE POLICY "Public read for product-images"
ON storage.objects
FOR SELECT
TO anon
USING ( bucket_id = 'product-images' );
```

Without this you will not be able to upload images to Supabase. This creates one large, public, all-access bucket. This is not ideal for privacy — we remedy this with UUIDs.

## 6. Firebase Realtime Database Rules

### Recommended Rules

```json
{
  "rules": {
    "listings": {
      ".indexOn": ["userId"],
      ".read": "auth != null",
      ".write": "auth != null",
      "$item_id": {
        ".write": "auth != null && (!data.exists() || data.child('userId').val() === auth.uid)"
      }
    }
  }
}
```

> Only allows a user to edit/delete if they own the item.

### Alternative Fix (Simpler Rules)

```json
{
  "rules": {
    "listings": {
      ".indexOn": ["userId"],
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```
