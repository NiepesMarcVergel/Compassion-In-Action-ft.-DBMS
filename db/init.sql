CREATE DATABASE compassion_in_action;

USE compassion_in_action;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    preferred_causes TEXT,
    skills TEXT
);

CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE donations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donor_name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    details TEXT NOT NULL
);

CREATE TABLE volunteer_works (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT NOT NULL
);

CREATE TABLE volunteer_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    applicant_name VARCHAR(255) NOT NULL,
    work_description TEXT NOT NULL,
    applicant_skills TEXT,
    applicant_preferred_causes TEXT,
    status VARCHAR(50) DEFAULT 'Pending'
);