# Hospital Appointment System  
A full-featured desktop application built in **Java Swing** with a **SQLite (standalone) database**, packaged as a **Windows EXE with bundled JRE**, requiring **zero installations** on the user’s machine.

<p align="center">
  <img src="https://img.shields.io/badge/Java-Swing-blue" />
  <img src="https://img.shields.io/badge/SQLite-embedded-success" />
  <img src="https://img.shields.io/badge/Platform-Windows-brightgreen" />
  <img src="https://img.shields.io/badge/Status-Completed-green" />
</p>

---

## Overview

This project is a complete **Hospital Appointment System** designed to manage:

- Patients  
- Doctors  
- Appointments  
- User accounts (Admin, Receptionist & Patient roles)

It is designed to run **fully offline** with a **self-creating SQLite database** (`hospital.db`) and includes:

- CSV Export  
- PDF Export (via Apache PDFBox)  
- Printing support  
- Role-based access control  
- Clean & modern UI  
- Windows Installer + EXE (no prerequisites)

This project was built by **Muhammad Fazil** ([@muhdfazilcode](https://github.com/muhdfazilcode)) as part of a school IT project.  
It represents my **first real-world full application**, built from scratch with patience & dedication.

---

## Features

### **Admin Features**
- Add/View/Delete Patients  
- Add/View/Delete Doctors  
- Create user accounts for patients  
- Book, Edit & Delete appointments  
- Export all tables to CSV  
- Export appointments to PDF  
- Print reports

### **Receptionist Features**
- Add/View/Delete patients  
- Book appointments  
- View appointments  
- Create patient user accounts  
- Export/Print reports  

### **Patient Features**
- Login securely  
- Book appointments (only for themselves)  
- View or delete their own appointments  
- Clean & restricted UI

---

## Installation (End-User)

No Java, no SQL, no prerequisites required.

1. Download the installer from [**Releases**](https://github.com/muhdfazilcode/Hospital-Appointment-System/releases)
2. Run the setup wizard  
3. Launch the application from desktop/start menu  
4. The database (`hospital.db`) will be auto-created on first launch  

---

## Default Credentials

### **Admin**

username: admin
password: admin123


### **Receptionist**

username: receptionist
password: recep123


### **Patient Sample Account**

username: patient
password: patient123


*(These can be changed or deleted inside the app.)*

---

## Export Options
The application supports:

-  **Export Patients → CSV**  
-  **Export Doctors → CSV**  
-  **Export Appointments → CSV**  
-  **Export Appointments → PDF**  

---

##  Screenshots

Add screenshots here:

/screenshots/login.png
/screenshots/dashboard.png
/screenshots/book-appointment.png
/screenshots/view-patients.png

---

## Contributing

Since this is a school project, contributions are not required —  
but feedback, suggestions, or improvements are always welcome!

---

## License

This project is for educational purposes only.  
Feel free to fork it or learn from it.