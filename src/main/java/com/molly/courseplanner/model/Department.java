package com.molly.courseplanner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a department, idenitified by its subject code.
 * Examples: "CMPT", "ENSC", "MSE", etc.
 *
 * A department is the top level of the data hierarchy:
 *  Department -> Course -> CourseOffering -> Section
 *
 * Each department contains all courses that share that subject code in the CSV.
 */
public class Department {

    // Auto-incremented ID used by the REST API to reference this department:
    private static long nextId = 1;
    private final long deptId;

    // The subject code as it appears in the CSV (e.g. "CMPT"):
    private final String name;

    // All courses belonging to this department, populated during
    // CSV parsing as new catalog numbers are encountered:
    private final List<Course> courses = new ArrayList<>();

    /**
     * Constructor - called the first time a new SUBJECT value is seen in the CSV.
     *
     * @param name - the subject code string (e.g. "CMPT").
     */
    public Department(String name) {
        this.deptId = nextId++;
        this.name = name;
    }

    /**
     * Adds a course to this department.
     * Called during CSV parsing when a new catalog number is
     * encountered for this subject.
     *
     * @param course - the course to add.
     */
    public void addCourse(Course course) {
        courses.add(course);
    }

    // ------------------------------------------------------
    // Getters:
    // ------------------------------------------------------

    public long getDeptId() {
        return deptId;
    }

    public String getName() {
        return name;
    }

    public List<Course> getCourses() {
        return courses;
    }
}

