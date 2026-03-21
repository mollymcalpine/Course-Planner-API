package com.molly.courseplanner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one specific offering of a course (i.e. a course taught in
 * a particular semester at a particular location).
 *
 * Example: CMPT 310 offered in Fall 2018 at Burnaby is one CourseOffering.
 * That same semester, CMPT 310 offered at Surrey would be a separate offering.
 *
 * A single CourseOffering can have multiple sections (LEC, TUT, LAB, etc.),,
 * each of which comes from its own row in the CSV.
 */
public class CourseOffering {

    // Auto-incremented ID, used by the REST API to reference this offering:
    private static long nextId = 1;
    private final long courseOfferingId;

    // Campus location (e.g. "BURNABY", "SURREY", "VAN"),
    // trimmed from the CSV since the raw data has trailing whitespace:
    private final String location;

    // The 4-digit semester code (e.g. 1181):
    // - Encoding: first digit = era (1 = modern), next 2 = year, last = term.
    // - Term digit: 1 = Spring, 4 = Summer, 7 = Fall.
    // - Example: 1181 = Spring 2018, 1184 = Summer 2018, 1187 = Fall 2028.
    private final int semesterCode;

    // Instructors are stored as a list since a single offering can
    // have multiple instructors listed in the CSV (comma-separated in the field):
    private final List<String> instructors = new ArrayList<>();

    // All the sections belonging to this offering (LEC, TUT, LAB, OPL, etc.),
    // where each section maps to one row in the CSV:
    private final List<Section> sections = new ArrayList<>();

    /**
     * Constructor: called when we first encounter a new semester + location
     * combination for a course during CSV parsing.
     *
     * @param semesterCode - 4-digit code identifying the semester (e.g. 1181).
     * @param location     - Campus location string (e.g. "BURNABY").
     */
    public CourseOffering(int semesterCode, String location) {
        this.courseOfferingId = nextId++;
        this.semesterCode = semesterCode;
        this.location = location;
    }

    // ------------------------------------------------------
    // Section and instructor management:
    // ------------------------------------------------------

    /**
     * Adds a section to this offering.
     * Each CSV row that belongs to this offering produces one Section.
     *
     * @param section - the section to add (e.g. a LEC with 150 capacity, 95 enrolled).
     */
    public void addSection(Section section) {
        sections.add(section);
    }

    /**
     * Merges in instructor names from a CSV row into this offering's instructor list.
     * Skips blank entries and avoids duplicates.
     *
     * The raw CSV INSTRUCTORS column can look like:
     *      "Alice Smith, Bob Jones" -> adds both.
     *      ", Bob Jones"            -> skips the blank, adds Bob.
     *      (already have Alice)     -> doesn't add Alice twice.
     *
     * @param names - a list of instructor name strings parsed from one CSV row.
     */
    public void mergeInstructors(List<String> names) {
        for (String name : names) {
            String trimmedName = name.trim();
            boolean isBlank = trimmedName.isEmpty();
            boolean isNull = trimmedName.equalsIgnoreCase("<null>");
            boolean isDuplicate = instructors.contains(trimmedName);
            if (!isBlank && !isNull && !isDuplicate) {
                instructors.add(trimmedName);
            }
        }
    }

    // ------------------------------------------------------
    // Semester helper methods:
    // ------------------------------------------------------

    /**
     * Extracts the calendar year from the semester code.
     *
     * This year is encoded in digits 2-3 of the 4-digit code.
     * Example: 1181 -> _18_ -> 18 -> 2018
     *
     * @return The full 4-digit calendar year (e.g. 2018).
     */
    public int getYear() {
        // Strip the last digit (term), then take the last 2 digits of what remains:
        int withoutTerm = semesterCode / 10;
        int shortYear = withoutTerm % 100;
        return 2000 + shortYear;
    }

    /**
     * Extracts the term (Spring / Summer / Fall) from the semester code.
     *
     * The last digit of the code encodes the term:
     *      1 -> Spring
     *      4 -> Summer
     *      7 -> Fall
     *
     * @return a human-readable term string (e.g. "Spring").
     */
    public String getTerm() {
        int termDigit = semesterCode % 10;
        return switch (termDigit) {
            case 1 -> "Spring";
            case 4 -> "Summer";
            case 7 -> "Fall";
            default -> "Unknown";
        };
    }

    // ------------------------------------------------------
    // Getters:
    // ------------------------------------------------------

    public long getCourseOfferingId() {
        return courseOfferingId;
    }

    public String getLocation() {
        return location;
    }

    public int getSemesterCode() {
        return semesterCode;
    }

    public List<String> getInstructors() {
        return instructors;
    }

    public List<Section> getSections() {
        return sections;
    }
}
