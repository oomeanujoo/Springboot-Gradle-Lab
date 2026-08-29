package com.gradle.microservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/*
 * ============================================================
 * WEATHER DATA ENTITY - WITH AUDITING
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * Stores weather information for cities (temperature, condition, provider)
 *
 * WHY AUDITING IS ENABLED:
 * -------------------------
 * Tracks every change to weather data for:
 * - Debugging: Who changed what and when
 * - Compliance: Required for SLA monitoring
 * - Data recovery: Can restore old values if needed
 *
 * AUDIT TABLE:
 * ------------
 * Changes are stored in "weather_data_history" table
 * Original table stays clean with only current data
 *
 * FIX APPLIED (targetAuditMode = NOT_AUDITED):
 * --------------------------------------------
 * Problem: Envers tried to audit ApiAudit relationship but ApiAudit is NOT @Audited
 * Solution: Tell Envers to ignore ApiAudit during auditing
 * Result: WeatherData is still audited, error is gone
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */

@Entity
@Table(name = "weather_data")
@Audited  // ENABLES AUDITING - All changes tracked automatically
@AuditTable(value = "weather_data_history")  // Audit history goes to separate table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;  // City name (e.g., "Mumbai")

    private Double temperature;  // Temperature in Celsius

    @Column(name = "weather_condition")
    private String weatherCondition;  // e.g., "Haze", "Sunny", "Rain"

    private String provider;  // Which API provided data (OPEN_WEATHER, WEATHER_API, etc.)

    /*
     * RELATIONSHIP WITH API AUDIT
     * ---------------------------
     * One WeatherData record has exactly ONE ApiAudit record
     * Foreign key "audit_id" in weather_data table references api_audit table
     *
     * WHY @Audited(targetAuditMode = NOT_AUDITED):
     * -------------------------------------------
     * Without this: Envers tries to audit ApiAudit -> Error because ApiAudit is not @Audited
     * With this: Envers ignores ApiAudit for auditing -> Works fine
     *
     * THIS LINE FIXED THE COMPILATION ERROR!
     */
    @OneToOne
    @JoinColumn(name = "audit_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ApiAudit apiAudit;
}