package com.mentalhealth.mentalhealth_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps CLIENT_PROFILE. clientId is the same number as the client's USER.userId. */
@Entity
@Table(name = "CLIENT_PROFILE")
public class ClientProfile {

    @Id
    @Column(name = "clientId")
    private Integer clientId;

    @Column(name = "profileData", columnDefinition = "TEXT")
    private String profileData;

    @Column(name = "medicalHistorySummary", columnDefinition = "TEXT")
    private String medicalHistorySummary;

    /** Set to true when the client presses the crisis button and chooses to notify their therapist. */
    @Column(name = "isCrisisActive")
    private Boolean isCrisisActive = false;

    public ClientProfile() {
    }

    public ClientProfile(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public String getProfileData() { return profileData; }
    public void setProfileData(String profileData) { this.profileData = profileData; }
    public String getMedicalHistorySummary() { return medicalHistorySummary; }
    public void setMedicalHistorySummary(String medicalHistorySummary) { this.medicalHistorySummary = medicalHistorySummary; }
    public Boolean getIsCrisisActive() { return isCrisisActive; }
    public void setIsCrisisActive(Boolean isCrisisActive) { this.isCrisisActive = isCrisisActive; }
}
