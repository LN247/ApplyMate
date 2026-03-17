# ApplyMate: Pure Native Kotlin App

This directory contains the complete source code for the **ApplyMate** Android application, built using modern Android development practices.

## 🚀 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (Local Persistence)
- **Navigation**: Jetpack Navigation
- **Asynchrony**: Kotlin Coroutines & Flow

## 📂 Project Structure
- `app/src/main/java/com/applymate/app/MainActivity.kt`: Entry point with Navigation Host.
- `app/src/main/java/com/applymate/app/ui/screens/`: Contains `DashboardScreen` and `AddOpportunityScreen`.
- `app/src/main/java/com/applymate/app/viewmodel/`: `MainViewModel` for business logic and state management.
- `app/src/main/java/com/applymate/app/data/`: `Database.kt` with Room Entity, DAO, and Database definition.
- `app/src/main/java/com/applymate/app/ui/theme/`: Material 3 Theme, Typography, and Color definitions.

## 🛠 How to Build the APK
1.  **Download** this `/android_source` folder to your computer.
2.  **Open Android Studio** (Hedgehog or newer recommended).
3.  Select **"Open"** and navigate to the `/android_source` folder.
4.  Wait for **Gradle Sync** to complete.
5.  Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6.  The generated APK will be located in `app/build/outputs/apk/debug/app-debug.apk`.

## ✨ Features Implemented
- **Dashboard**: Real-time stats and list of applications.
- **Add Opportunity**: Full form to track new roles.
- **Local Storage**: All data is saved on the device using Room.
- **Material 3**: Modern, premium look with Indigo accents.
