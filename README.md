# 🚀 CivicTrack

**Smart Civic Issue Reporting with AI**

CivicTrack is an Android application that enables citizens to report civic issues like potholes, garbage, and infrastructure problems using AI-powered automation and real-time tracking.

---

## 📌 Features

* 📸 Upload photo or video of civic issues
* 🤖 AI-powered auto-fill (category & description)
* 📍 Automatic location detection (GPS)
* 📊 Real-time issue tracking
* 🔐 Secure user authentication (Firebase)
* 🛠️ Admin panel for managing reports

---

## 🧠 AI Integration

* Detects issue type from image
* Generates description automatically
* Suggests appropriate category
* Reduces manual effort and improves accuracy

👉 *Just upload → AI handles the rest*

---

## 🏗️ Tech Stack

### 📱 Frontend

* Android (Kotlin)

### ☁️ Backend

* Firebase (Authentication + Firestore)

### 🗄️ Database

* Cloud Firestore (NoSQL)

### 🔗 APIs & Services

* Google AI (Gemini / Vision API)
* ImgBB / Firebase Storage (Image hosting)
* Google Maps API (Location detection)

---

## 🔄 How It Works

1. User logs in / signs up
2. Uploads image of issue
3. AI analyzes the image
4. Details are auto-filled
5. User submits report
6. Admin reviews and updates status

---

## 🛠️ Installation

1. Clone the repository:

```bash
git clone https://github.com/Skadekar2703/CivicTrack.git
```

2. Open in Android Studio

3. Add your API keys in `local.properties`:

```
GEMINI_API_KEY=your_key_here
IMGBB_API_KEY=your_key_here
```

4. Add `google-services.json` inside `/app` folder

5. Sync Gradle and run the app

---

## 🔐 Admin Demo Credentials

To test admin features, use the following demo accounts:

Email IDs:
- demo1@gmail.com  
- demo2@gmail.com  
- demo3@gmail.com  

Password:
123456

> Note: These are test accounts created for demo purposes only.

## 🔐 Security Note

* API keys and sensitive data are not included in the repository
* Use `local.properties` to store secrets

---

## 🚀 Future Scope

* Push notifications for updates
* AI-based issue severity detection
* Government system integration
* Map-based issue heatmaps
* Multi-language support

---

## 👥 Team LazyRookies

* Soham Kadekar (Team Leader)
* Atharv Shinde
* Maithili Katikar
* Shweta Sonawane

---

## 🏆 Hackathon

**Rockverse Hackathon**

---

## 🌍 Vision

**Empowering citizens. Improving cities.**

---

## ⭐ Contribute

Feel free to fork this repo and improve CivicTrack!

---
