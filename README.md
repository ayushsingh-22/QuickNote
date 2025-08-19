# 📝 QuickNote - SelfNote

A beautiful, secure note-taking Android application built with **Jetpack Compose** and **Firebase**. QuickNote provides an elegant, modern interface for capturing and organizing your thoughts with end-to-end encryption.

![Version](https://img.shields.io/badge/version-1.0.5-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

---

## 📸 App Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/66e05b29-b6bf-4ecd-b2f8-751658606a52" width="30%" alt="Screenshot 1"/>
  <img src="https://github.com/user-attachments/assets/1ec0e35f-16f9-4de6-beda-0e6deae0b4de" width="30%" alt="Screenshot 2"/>
  <img src="https://github.com/user-attachments/assets/b982b439-b67b-473d-a9be-b514403a8e6b" width="30%" alt="Screenshot 3"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7084c082-e1ac-4a99-b163-9e557684d5de" width="30%" alt="Screenshot 4"/>
  <img src="https://github.com/user-attachments/assets/3d5e2150-3095-4f12-a098-89a57caa747d" width="30%" alt="Screenshot 5"/>
  <img src="https://github.com/user-attachments/assets/df33d1aa-21f3-47a2-8949-9bd509331d7d" width="30%" alt="Screenshot 6"/>
</p>


## ✨ Features

### 🔐 Security & Privacy
- End-to-End Encryption with AES
- Device-Based Encryption using unique device ID
- Secure Firebase storage with encrypted notes
- Privacy-first: Only you can decrypt your notes

### 📱 Modern UI/UX
- Material Design 3 theming
- Built fully with Jetpack Compose
- Smooth animations and transitions
- Custom dynamic theming
- Responsive layouts for all devices

### 🚀 Smart Features
- Real-time Firebase sync
- Dedicated View and Edit modes
- Character counting with validation
- Auto-save with confirmation dialogs
- Powerful search and note organization

### 💡 User Experience
- Input validation with feedback
- Haptic responses for actions
- Empty state hints
- Graceful error handling with friendly messages

---

## 🏗️ Architecture

### Modern Android Development Stack
- **Jetpack Compose** - UI Toolkit  
- **MVVM Pattern** - Clean separation of concerns  
- **Repository Pattern** - Data layer management  
- **Coroutines** - Async programming  
- **Material 3** - Design guidelines  

### Modular Project Structure
```

📦 com.amvarpvtltd.selfnote
├── design/              # UI Screens
├── components/          # Reusable UI Components
├── repository/          # Data Layer
├── security/            # Encryption utilities
├── utils/               # Helpers & Constants
└── dataclass/           # Data Models

````

---

## 🛠️ Tech Stack

- **Language**: Kotlin  
- **UI**: Jetpack Compose  
- **Architecture**: MVVM + Repository  
- **Backend**: Firebase Realtime Database  
- **Encryption**: AES (Device-specific key)  
- **Build System**: Gradle (Kotlin DSL)  

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer  
- JDK 8+  
- Android SDK 26+  
- Firebase project setup  

### Installation
```bash
git clone https://github.com/yourusername/quicknote.git
cd quicknote
````

1. Add `google-services.json` in the `app/` directory
2. Enable Firebase Realtime Database
3. Build & run:

   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔐 Security Highlights

* AES encryption before storing in Firebase
* Device-specific encryption keys
* No plaintext stored in cloud
* Local validation before encryption

---

## 🎨 Design System

* **Primary**: Indigo (#6366F1)
* **Secondary**: Cyan (#06B6D4)
* **Success**: Green (#10B981)
* **Warning**: Amber (#F59E0B)
* **Error**: Red (#EF4444)
* **Background**: Light Gray (#FAFAFA)

Animations include: smooth transitions, FAB animations, card hover effects, and progress indicators.

---

## 🧪 Testing

```bash
./gradlew test               # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests
```

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch
3. Commit your changes
4. Push & open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Ayush Kumar**
📧 [ayushkumar2205@gmail.com](mailto:ayushkumar2205@gmail.com)
💼 [LinkedIn](https://www.linkedin.com/in/ayush-kumar-a2880a258/)

---

✨ Made with ❤️ using Jetpack Compose & Firebase
