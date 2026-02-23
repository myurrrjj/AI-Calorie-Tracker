<img  alt="Screenshot_20260203-202219_AI Calorie Tracker" src="https://github.com/user-attachments/assets/8f3cde4d-47c1-481d-b217-f8c0c9337f2e" width="300"  />
<img  alt="Screenshot_20260203-202216_AI Calorie Tracker" src="https://github.com/user-attachments/assets/d0ae7765-9a1d-4770-b1d6-4b40cb9045bc" width="300"  />
<!-- <img  alt="Screenshot_20260203-202209_AI Calorie Tracker" src="https://github.com/user-attachments/assets/357b91ae-ea0b-4001-8c94-9f28a6012e0d" width="300"  /> -->
<img  alt="Screenshot_20260203-202202_AI Calorie Tracker" src="https://github.com/user-attachments/assets/10134a00-2615-41c7-8bbe-a323237c2a77" width="300"  />
<img width="300"  alt="Screenshot_20260223-212036_AI Calorie Tracker" src="https://github.com/user-attachments/assets/175af6e0-2a04-4431-b43c-580ef2188b1d" />


https://github.com/user-attachments/assets/94ceca83-b2f4-48e3-8cac-ffbd320e2eba




<h1 align="center">🥗 AI Calorie Tracker</h1>

<p align="center">
  <strong>A modern, AI-powered health and nutrition tracking application built natively for Android.</strong>
</p>

## 📖 About the Project

AI Calorie Tracker revolutionizes how you log your meals. Instead of manually searching databases for ingredients, simply snap a picture or type a brief description of what you ate. The app uses advanced generative AI to instantly analyze the food, estimate portion sizes, and break down the complete nutritional profile—including calories, macronutrients, and key micronutrients. 

Built with a focus on premium user experience, the app features buttery-smooth animations, haptic feedback, and a local-first architecture that keeps your data fast and private.

## ✨ Key Features

* 🧠 **Intelligent Meal Logging:** Powered by Google's `gemini-2.5-flash` model, the app accepts text descriptions or images and returns highly accurate, structured JSON data containing calories, protein, carbs, fats, fiber, sugar, and vitamins.
* 💫 **Premium UI & Animations:** * **Shared Element Transitions:** Utilizes Compose's `SharedTransitionLayout` to seamlessly animate meal cards expanding into full detail overlays.
    * **Interactive Feedback:** Features a custom `bouncyClick` modifier that responds to user taps with realistic scaling physics and precise haptic feedback.
    * **Dynamic Progress Visualization:** Uses the new Material 3 Expressive `CircularWavyProgressIndicator` alongside animated pulsing effects to display daily calorie goals.
* ⚡ **Optimistic UI Updates:** New meal logs instantly appear in your daily timeline with an "Analysing..." state while the AI processes the image in the background, ensuring the app never feels frozen.
* 📊 **Comprehensive Analytics:** A dedicated daily breakdown screen provides a visual hierarchy of your macro splits and detailed micronutrient tracking using custom vertical progress cards.
* ⏳ **Intermittent Fasting Tracker (WIP):** Core logic and database structures are in place to track active fasts, calculate progress percentages against customizable goals, and store fasting history.
* 💾 **Local-First Architecture:** All meal logs and images are processed and securely stored on-device using a Room Database, allowing you to view your history instantly without an internet connection.

## 🛠 Tech Stack & Architecture

This project is built using modern Android development best practices:

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3 Expressive APIs)
* **Architecture:** MVVM (Model-View-ViewModel) with a Repository Pattern
* **Local Database:** Room Persistence Library
* **AI / Machine Learning:** Google Generative AI SDK (`GenerativeModel`)
* **Image Loading:** Coil (via `AsyncImage`)
* **Concurrency:** Kotlin Coroutines & Flows


