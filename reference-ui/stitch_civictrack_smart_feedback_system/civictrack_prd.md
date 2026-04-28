# 📄 PRODUCT REQUIREMENTS DOCUMENT (PRD)
## 🏙 Smart City Public Works Feedback System — Android App (API-Based)
---
## 🧠 1. Product Overview
Product Name: CivicTrack Mobile
Platform: Android Application (API-driven)
Description:
CivicTrack Mobile is a fully API-integrated Android application that allows citizens to report civic issues such as potholes, garbage dumping, and streetlight failures using images and GPS location. All data is stored and managed via a centralized backend (Firebase), enabling real-time synchronization, transparency, and efficient issue resolution.
---
## 🎯 2. Objectives
* Enable citizens to easily report civic issues
* Provide real-time issue tracking and updates
* Improve transparency in public works
* Reduce issue resolution time (target: < 7 days)
* Build a centralized system for issue management
---
## 🚨 3. Problem Statement
Citizens currently face:
* No simple platform to report civic issues
* Lack of visibility into issue status
* Delayed response from authorities
* No centralized tracking or analytics system
---
## 💡 4. Solution
The system provides:
* A mobile app for reporting issues with images and location
* A backend system to store and manage complaints
* Admin tools to update issue status
* A public dashboard for transparency and analytics
---
## 🧩 5. Features
### 📱 5.1 Citizen Mobile App
Issue Reporting:
* Capture or upload photo
* Enter title and description
* Auto-detect GPS location
* Select issue category (pothole, garbage, streetlight, etc.)
* Submit issue
Issue Tracking:
* View submitted complaints
* Track status: Pending / In Progress / Resolved
Issue Feed:
* View all issues
* Filter by category, status, location
Map View:
* Display issues on Google Maps
* Click markers for details
Notifications:
* Get notified on status updates
---
### 🛠 5.2 Admin Dashboard
Issue Management:
* View all reported issues
* Filter by category, status, location
Status Control:
* Update issue status
  * Pending → In Progress → Resolved
Priority Assignment:
* Set priority (Low / Medium / High)
Analytics:
* Total issues
* Resolved issues
* Average resolution time
* Area-wise distribution
---
### 🌐 5.3 Public Dashboard
* Public issue visibility
* Map-based visualization
* Resolution statistics
---
## ⚙️ 6. Technical Architecture
Frontend (Android):
* Kotlin
* XML / Jetpack Compose
* Retrofit
* Google Maps SDK
Backend (API-based using Firebase):
* Firebase Authentication
* Firestore Database
* Firebase Storage (image upload)
* Firebase Cloud Messaging (notifications)
---
## 🗄 7. Data Model
Collection: issues
{
  "id": "string",
  "title": "string",
  "description": "string",
  "category": "string",
  "imageUrl": "string",
  "location": {
    "latitude": number,
    "longitude": number
  },
  "status": "Pending | In Progress | Resolved",
  "priority": "Low | Medium | High",
  "createdAt": timestamp,
  "updatedAt": timestamp,
  "userId": "string"
}
---
## 🔄 8. User Flow
Issue Reporting Flow:
1. User opens app
2. Clicks "Report Issue"
3. Uploads/captures image
4. Enters details
5. Location auto-detected
6. Submits issue
7. Data stored via API (Firebase)
Issue Resolution Flow:
1. Admin views issue
2. Updates status
3. Notification sent to user
4. Status updated in system
---
## 🔔 9. Notifications
* Trigger: Issue status updates
* Example:
  "Your reported issue has been resolved"
---
## 📊 10. Success Metrics
* Number of issues reported
* Average resolution time (< 7 days)
* Percentage of resolved issues
* Daily/Monthly active users
---
## 🧪 11. MVP Scope
Must Have:
* Issue reporting (image + location)
* Firebase integration (API-based)
* Issue listing
* Status tracking
Nice to Have:
* Map view
* Notifications
* Admin dashboard
---
## 🚫 12. Out of Scope
* AI-based detection
* Government API integration
* Complex authentication system
* Offline functionality
---
## 🔮 13. Future Enhancements
* AI-based issue classification
* Duplicate issue detection
* Heatmap visualization
* Role-based admin system
---
## ⚠️ 14. Risks & Mitigation
* Time constraint → Focus on MVP
* Backend complexity → Use Firebase
* UI delays → Use templates
* Team coordination → Clear task division
---
## 👥 15. Team Roles
* Member 1: Android UI
* Member 2: Firebase/API integration
* Member 3: Dashboard
* Member 4: Maps + Notifications
---
## 🏁 16. Conclusion
The Smart City Public Works Feedback System improves civic engagement, transparency, and efficiency in resolving public issues. A working prototype demonstrating real-time reporting, tracking, and analytics will effectively showcase the solution’s impact and feasibility in a hackathon environment.
