package com.mentalhealth.mentalhealth_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps THERAPIST_PROFILE. therapistId is the same number as the therapist's USER.userId. */
@Entity
@Table(name = "THERAPIST_PROFILE")
public class TherapistProfile {

    @Id
    @Column(name = "therapistId")
    private Integer therapistId;

    @Column(name = "specializations", length = 255)
    private String specializations;

    @Column(name = "credentialData", columnDefinition = "TEXT")
    private String credentialData;

    @Column(name = "availabilitySchedule", columnDefinition = "TEXT")
    private String availabilitySchedule;

    public TherapistProfile() {
    }

    public TherapistProfile(Integer therapistId, String specializations) {
        this.therapistId = therapistId;
        this.specializations = specializations;
    }

    public Integer getTherapistId() { return therapistId; }
    public void setTherapistId(Integer therapistId) { this.therapistId = therapistId; }
    public String getSpecializations() { return specializations; }
    public void setSpecializations(String specializations) { this.specializations = specializations; }
    public String getCredentialData() { return credentialData; }
    public void setCredentialData(String credentialData) { this.credentialData = credentialData; }
    public String getAvailabilitySchedule() { return availabilitySchedule; }
    public void setAvailabilitySchedule(String availabilitySchedule) { this.availabilitySchedule = availabilitySchedule; }
}
