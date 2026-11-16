DROP DATABASE IF EXISTS mental_health_db;
CREATE DATABASE mental_health_db;
USE mental_health_db;

CREATE TABLE USER (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    isVerified BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE CLIENT_PROFILE (
    clientId INT PRIMARY KEY,
    profileData TEXT,
    medicalHistorySummary TEXT,
    isCrisisActive BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_client_user FOREIGN KEY (clientId) REFERENCES USER(userId) ON DELETE CASCADE
);

CREATE TABLE THERAPIST_PROFILE (
    therapistId INT PRIMARY KEY,
    specializations VARCHAR(255),
    credentialData TEXT,
    availabilitySchedule TEXT,
    CONSTRAINT fk_therapist_user FOREIGN KEY (therapistId) REFERENCES USER(userId) ON DELETE CASCADE
);

CREATE TABLE JOURNAL_ENTRY (
    entryId INT AUTO_INCREMENT PRIMARY KEY,
    clientId INT NOT NULL,
    entryDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    moodScore FLOAT NOT NULL,
    encryptedContent TEXT NOT NULL,
    CONSTRAINT fk_journal_client FOREIGN KEY (clientId) REFERENCES USER(userId) ON DELETE CASCADE
);

CREATE TABLE APPOINTMENT (
    appointmentId INT AUTO_INCREMENT PRIMARY KEY,
    clientId INT NOT NULL,
    therapistId INT NOT NULL,
    scheduledTime TIMESTAMP NOT NULL,
    duration INT NOT NULL, -- duration in minutes
    status ENUM('Scheduled', 'Cancelled', 'Completed') DEFAULT 'Scheduled',
    CONSTRAINT fk_appt_client FOREIGN KEY (clientId) REFERENCES USER(userId) ON DELETE CASCADE,
    CONSTRAINT fk_appt_therapist FOREIGN KEY (therapistId) REFERENCES USER(userId) ON DELETE CASCADE
);

CREATE TABLE SESSION_NOTES (
    notesId INT AUTO_INCREMENT PRIMARY KEY,
    appointmentId INT NOT NULL UNIQUE,
    therapistId INT NOT NULL,
    clientId INT NOT NULL,
    encryptedNotes TEXT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_appointment FOREIGN KEY (appointmentId) REFERENCES APPOINTMENT(appointmentId) ON DELETE CASCADE,
    CONSTRAINT fk_notes_therapist FOREIGN KEY (therapistId) REFERENCES USER(userId) ON DELETE CASCADE,
    CONSTRAINT fk_notes_client FOREIGN KEY (clientId) REFERENCES USER(userId) ON DELETE CASCADE
);
