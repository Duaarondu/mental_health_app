package com.mentalhealth.mentalhealth_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mentalhealth.mentalhealth_backend.model.JournalEntry;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {

    @Query("select j from JournalEntry j where j.client.userId = :clientId order by j.entryDate desc, j.entryId desc")
    List<JournalEntry> findForClient(@Param("clientId") Integer clientId);
}
