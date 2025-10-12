# Android Project: OTAMS
Created for: SEG2105 – Introduction to Software Engineering Fall 2025

*Project Group 28*

## Project Description
In this project, you will implement the Online Tutoring Appointment Management System (OTAMS). OTAMS is a mobile application designed to streamline the process of tutoring appointment scheduling at the University of Ottawa Help Centre. OTAMS supports three types of users: Student, Tutor, and Administrator.
* The Student browses available tutoring slots so they can book or cancel sessions and rate Tutors.
* The Tutor manages their availability and responds to session requests.
* The Administrator approves the account‑registration requests of Students and Tutors.
In the next sections, we describe the application in detail from the perspective of each user.

### Student
To become a Student, a user submits a registration request that must be approved by the  Administrator. Once registered, a Student can:
• View their upcoming sessions.
• Cancel an existing session (only if it starts in > 60 minutes).
• View their past sessions.
• Rate a Tutor following a completed session.
• Book a session by selecting an available 30‑minute slot for a desired course.

### Tutor
To become a Tutor, a user submits a registration request that must be approved by the Administrator. Once registered, a Tutor can:
* Specify and modify the 30‑minute availability slots they would like to offer.
* View their upcoming sessions.
* View their past sessions.
* Cancel sessions.
* Approve or reject session requests.
The Tutor may also elect to auto‑approve all session requests, so they do not have to act on each one individually. Requests are simply accepted automatically.  Administrator  The Administrator is a pre-registered user (their username and password are seeded in the  database when the system is first launched). The Administrator can approve or reject  registration requests from Students and Tutors