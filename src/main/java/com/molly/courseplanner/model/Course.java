package com.molly.courseplanner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single course within a department.
 *
 * A course is identified by its catalog number (e.g. "310", "454W").
 * Each course can have multiple offerings - one per semester it was taught.
 *
 * Example: CMPT 310 is one course. It might have been offered in Fall 2018,
 * Spring 2019, etc.
 * Each one of those is a separate CourseOffering.
 */
public class Course {

    // Auto-incremented ID assigned when this course is created:
    // Used by the REST API so clients can reference courses by a stable numeric ID.
    private static long nextId = 1;
    private final long courseId;

    // The catalog number as it appears in the CSV (e.g. "310", "1XX", "454W"):
    private final String catalogNumber;

    // All semesters this course has been offered:
    // Each CourseOffering represents one semester + location combination.
    private final List<CourseOffering> offerings = new ArrayList<>();

    /**
     * Constructor: called when a new unique course is discovered while parsing the CSV.
     *
     * @param catalogNumber - the catalog number string from the CATALOGNUMBER column.
     */
    public Course(String catalogNumber) {
        this.courseId = nextId++;
        this.catalogNumber = catalogNumber;
    }

    /**
     * Adds a new offering to this course.
     * Called during CSV parsing whenever we see a new semester/location for this course.
     *
     * @param offering - the CourseOffering to attach to this course.
     */
    public void addOffering(CourseOffering offering) {
        offerings.add(offering);
    }

    /**
     * Finds an existing offering for this course that matches the given semester code & location.
     * Returns null if none exist yet.
     *
     * This is used during CSV parsing to group rows that belong to the same offering
     * (e.g. LEC + TUT sections of the same course in the same semester).
     *
     * @param semesterCode - the 4-digit semester code (e.g. 1181).
     * @param location     - the campus location string (e.g. "BURNABY").
     * @return Matching CourseOffering, or null if not found.
     */
    public CourseOffering findOffering(int semesterCode, String location) {
        for (CourseOffering o : offerings) {
            if (o.getSemesterCode() == semesterCode && o.getLocation().equals(location)) {
                return o;
            }
        }
        return null;
    }

    // ------------------------------------------------------
    // Getters:
    // ------------------------------------------------------

    public long getCourseId() {
        return courseId;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public List<CourseOffering> getOfferings() {
        return offerings;
    }
}
