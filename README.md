# 🥗 AI Calorie Tracker

An intelligent, beautiful, and highly animated Android application that leverages Generative AI (Google Gemini) to seamlessly track your daily meals, calculate precise macronutrients and micronutrients, and sync with your physical activity via Android Health Connect. 

Built entirely with modern Android development practices, this app features a fluid Jetpack Compose UI, Shared Element Transitions, and a robust MVVM architecture.

App Demo Video:-

https://github.com/user-attachments/assets/27c9bf92-4623-4970-ba29-8bd70ac4321a





## ✨ Features in Detail

### 🧠 Generative AI Meal Analysis (Powered by Gemini)
* **Natural Language Logging:** Simply type what you ate (e.g., "A large bowl of Dal Makhani with 2 rotis") and the app's integration with the `gemini-2.5-flash` model will calculate the exact nutritional breakdown.
* **Visual Food Recognition:** Snap a photo or upload an image from your gallery. The AI analyzes the image to detect the food and estimates its nutritional value.
* **Comprehensive Data:** Extracts not just calories, but detailed **Macronutrients** (Protein, Carbs, Fats, Fiber, Sugar) and **Micronutrients** (Vitamin A, Vitamin C, Vitamin D, Iron, Calcium, Sodium, Potassium).

### 🏃‍♂️ Health Connect Integration
* **Active Calorie Tracking:** Securely connects to Android's Health Connect API to read your `TotalCaloriesBurnedRecord` and `ActiveCaloriesBurnedRecord`.
* **Dynamic Goals:** Compares your calories consumed against your active calories burned, giving you a complete picture of your daily energy expenditure.

### 📊 Advanced Analytics & Charts
* **Daily Breakdown:** A beautiful daily overview featuring animated concentric calorie rings and dynamic vertical progress bars for macro splits.
* **Interactive Macro Trends:** Custom-built bar charts to visualize your nutrition over time. Filter by **Weekly, Monthly, or Yearly** ranges.
* **Deep Insights:** View the Average, Maximum, and Total consumption for specific metrics (Calories, Protein, Carbs, Fat, Fiber, Sugar) with smooth, squishy tab selectors and animated chart columns.

### 🎨 Fluid UI & UX (Material 3)
* **Shared Element Transitions:** Seamlessly smooth animations when clicking a meal card to view its detailed nutritional breakdown overlay.
* **Custom Bouncy Interactions:** Uses a custom `bouncyClick` modifier with haptic feedback to make buttons and cards feel tactile and alive.
* **Smart Quantity Selectors:** Easily scale a meal's macros by adjusting the quantity via a custom bottom sheet wheel picker or numeric text input.
* **Dynamic Theming:** Fully supports Material 3 Light and Dark modes.

### 💾 Smart Data Management
* **Offline-First Architecture:** Uses Room Database to store all your meal logs locally. 
* **Saved/Favorite Meals:** Frequently eat the same thing? Save meals to your favorites and quick-log them later without making an API call to save time and API quota.
* **Secure API Key Storage:** Your Gemini API key is stored locally on the device using Android's `EncryptedSharedPreferences` for maximum security.

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/44173cd7-2b15-4597-bfbc-f838d525f2e9"width="250" alt="Main Dashboard & Health Connect Progress" />
  <img src="https://github.com/user-attachments/assets/c3141a1b-04b0-4410-a2a4-b1a9fca63cb5"width="250" alt="Smart Attachment Options" />
  <img src="https://github.com/user-attachments/assets/8e0fe9c7-4245-44e0-a4b2-f8a87bae8f31" width="250" alt="Daily Macro Breakdown" />
  <img src="https://github.com/user-attachments/assets/ddb105c9-c3b5-4adc-918c-150a69140ca6"width="250" alt="Interactive Macro Trends Chart" />
  <img src="https://github.com/user-attachments/assets/1b21bf4b-3591-4430-b7ab-442ddfdd6492" width="250" alt="Saved Meals Quick Logging" />
  <img  src="https://github.com/user-attachments/assets/98dcf4a7-c42a-49fb-a9f9-1c8a51fb00cc" width="250" alt="Fluid Quantity Selector" />
  <img src="https://github.com/user-attachments/assets/b8a1d333-3cb0-4ca9-b477-ce130f9a5091" width="250" alt="Secure API Key Setup" />
</p>

---

## 🛠️ Tech Stack & Architecture

This project is built using the latest industry standards for native Android development.

**Architecture:** Model-View-ViewModel (MVVM) with Repository Pattern.

* **UI Framework:** Jetpack Compose (Material 3)
* **Language:** Kotlin
* **Asynchronous Programming:** Kotlin Coroutines & Flow
* **Dependency Injection:** Manual App Container pattern (highly scalable)
* **Local Database:** Room Database (with custom TypeConverters and automated migrations)
* **AI Integration:** Google Generative AI SDK (`Client.builder()`)
* **Health Data:** Android Health Connect API
* **Image Loading:** Coil (`AsyncImage`)
* **Security:** Jetpack Security Crypto (`EncryptedSharedPreferences`)
* **Animations:** Compose Animation API (`SharedTransitionScope`, `AnimatedVisibility`, `animateFloatAsState`, custom physics-based `spring` animations)

---

## 📂 Project Structure Overview

* `local/` - Contains the Room Database setup, DAOs (`MealDao`, `SavedMealDao`), and Entity data classes.
* `network/` - Houses the `AiService` responsible for formatting prompts and communicating with the Gemini API.
* `repository/` - The single source of truth for data. Includes `MealRepository`, `HealthRepository`, and `UserPreferencesRepository`.
* `ui/` - Contains all Jetpack Compose screens, broken down into logical packages:
    * `home/` - Dashboard, Day Views, Input Area, and Meal Cards.
    * `analytics/` - Daily breakdowns, custom Canvas/Box-based charts, and macro stat cards.
    * `dialogs/` - UI for inputting API keys and editing daily calorie goals.
    * `theme/` - Material 3 color schemes, typography, and dynamic theming.
* `health/` - Contains `HealthConnectManager` to handle permissions and data aggregation from the Google Health platform.

---

## 🚀 Getting Started

### Prerequisites
1. Android Studio (Latest Stable or Ladybug recommended)
2. A physical Android device or Emulator running API level 26+ (API 34+ recommended for Health Connect features).
3. A Google Gemini API Key. (You can get one for free at [Google AI Studio](https://aistudio.google.com/)).

