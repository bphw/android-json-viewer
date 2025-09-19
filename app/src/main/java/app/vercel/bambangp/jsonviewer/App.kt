package app.vercel.bambangp.jsonviewer

// File: app/src/main/java/app/vercel/bambangp/jsonviewer/App.kt
import android.app.Application
import android.util.Log

class App : Application() {  // Extends Android's base Application class
    override fun onCreate() {
        super.onCreate()
        // Initialize libraries here (Firebase, Analytics, etc.)
        Log.d("APP", "Custom Application started")
    }
}