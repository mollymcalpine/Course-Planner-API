package com.molly.courseplanner.model;

/**
 * Represents a single section of a course offering.
 *
 * Each row in the CSV corresponds to one Section. A section has
 * a type (LEC, TUT, LAB, etc.), an enrollment capacity, and an enrollment total.
 *
 * Example: CMPT 310 Fall 2028 Burnaby might have:
 * - Section LEC, cap 150, total 95
 * - Section TUT, cap 30, total 30
 * - Section TUT, cap 30, total 25
 */
public class Section {

    // The component code from the CSV: LEC, TUT, LAB, OPL, PRA, etc.
    private final String type;

    // Maximum number of students allowed to enroll in this section:
    private final int enrollmentCap;

    // Actual number of students enrolled at time of data export:
    private final int enrollmentTotal;

    /**
     * Constructor - called once per CSV row when building the model.
     *
     * @param type            - component code string (e.g. "LEC").
     * @param enrollmentCap   - maximum enrollment for this section.
     * @param enrollmentTotal - actual enrollment for this section.
     */
    public Section(String type, int enrollmentCap, int enrollmentTotal) {
        this.type = type;
        this.enrollmentCap = enrollmentCap;
        this.enrollmentTotal = enrollmentTotal;
    }

    // ------------------------------------------------------
    // Getters:
    // ------------------------------------------------------

    public String getType() {
        return type;
    }

    public int getEnrollmentCap() {
        return enrollmentCap;
    }

    public int getEnrollmentTotal() {
        return enrollmentTotal;
    }
}
