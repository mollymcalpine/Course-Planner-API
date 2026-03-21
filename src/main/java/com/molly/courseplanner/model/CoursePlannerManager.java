package com.molly.courseplanner.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CoursePlannerManager is the central data manager for the application.
 *
 * Responsibilities:
 * 1. Load and parse the CSV file at startup.
 * 2. Build and maintain the full in-memory model:
 *     Department -> Course -> CourseOffering -> Section
 * 3. Provide lookup methods used by the REST controller.
 *
 * Data Hierarchy:
 * - Each Department has many Courses.
 * - Each Course has many CourseOfferings (one per semester + location).
 * - Each CourseOffering has many Sections (one per CSV row).
 */
public class CoursePlannerManager {

    // The top-level list of all departments discovered in the CSV:
    // Departments are created on-the-fly as new SUBJECT values are encountered.
    private final List<Department> departments = new ArrayList<>();

    // Path to the CSV file inside src/main/resources/
    // Spring Boot automatically makes this folder available on the classpath.
    private static final String CSV_FILE = "/course_data_2018.csv";

    /**
     * Constructor - immediately loads and parses the CSV when the manager is created.
     *
     * The controller creates one instance of this class, so parsing happens once
     * at application startup.
     */
    public CoursePlannerManager() {
        loadCSV();
    }

    // ------------------------------------------------------
    // CSV Parsing:
    // ------------------------------------------------------

    /**
     * Reads the CSV file from the classpath, and builds full in-memory model.
     *
     * CSV columns (0-indexed):
     *  0: SEMESTER - 4-digit semester code (e.g. 1181).
     *  1: SUBJECT  - Department name (e.g. "CMPT").
     *  2: CATALOGNUMBER - Course number (e.g. "310").
     *  3: LOCATION - Campus (e.g. "BURNABY").
     *  4: ENROLMENTCAPACITY
     *  5: ENROLMENTTOTAL
     *  6: INSTRUCTORS - Comma-separated names within the field
     *  7: COMPONENTCODE - Section type ("LEC", "TUT", etc.).
     *
     * For each row:
     * 1. Find or create the Department for that SUBJECT.
     * 2. Find or create the Course for that CATALOGNUMBER within the dept.
     * 3. Find or create the CourseOffering for that SEMESTER + LOCATION.
     * 4. Always create a new Section and attach it to the offering.
     */
    private void loadCSV() {
        // Load the file from src/main/resources using the classpath:
        InputStream inputStream = getClass().getResourceAsStream(CSV_FILE);

        if (inputStream == null) {
            System.err.println("ERROR: Could not find " + CSV_FILE + " in resources. Make sure it's in src/main/resources/");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip the header row:
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Skip blank lines:
                if (line.trim().isEmpty()) {
                    continue;
                }

                parseLine(line);
            }
        } catch (Exception e) {
            System.err.println("ERROR reading CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses one CSV row and integrates it into the model.
     *
     * NOTE - split on commas carefully:
     * - The INSTRUCTORS field has comma-separated names, but the whole field is one CSV column.
     * - This means CSV uses a fixed 8-column structure, where column 6 may have internal commas.
     * - Hande this by splitting into at most 8 tokens.
     *
     * @param line - a single non-header CSV row.
     */
    private void parseLine(String line) {
        // Split into at most 8 parts so that instructor commas don't break parsing.
        String[] parts = line.split(",", 8);

        if (parts.length < 8) {
            // Malformed row, so skip silently:
            return;
        }

        // Extract and clean each field:
        int semesterCode = Integer.parseInt(parts[0].trim());
        String subject       = parts[1].trim();
        String catalogNumber = parts[2].trim();
        String location      = parts[3].trim();
        int enrollCap        = parseIntSafe(parts[4].trim());
        int enrollTotal      = parseIntSafe(parts[5].trim());
        String instructorRaw = parts[6].trim();
        String componentCode = parts[7].trim();

        // Parse instructor names (comma-separated):
        List<String> instructorNames = Arrays.asList(instructorRaw.split(","));

        // 1. Find or create the Department:
        Department dept = findOrCreateDepartment(subject);

        // 2. Find or create the Course within that Department:
        Course course = findOrCreateCourse(dept, catalogNumber);

        // 3. Find or create the CourseOffering for this semester+location:
        CourseOffering offering = course.findOffering(semesterCode, location);
        if (offering == null) {
            offering = new CourseOffering(semesterCode, location);
            course.addOffering(offering);
        }

        // Merge in any new instructors from this row:
        offering.mergeInstructors(instructorNames);

        // 4. Always add a new Section for this row:
        Section section = new Section(componentCode, enrollCap, enrollTotal);
        offering.addSection(section);
    }

    // ------------------------------------------------------
    // Find-Or-Create Helpers:
    // ------------------------------------------------------

    /**
     * Finds the Department with the given name, or creates it if it doesn't exist yet.
     * Departments are created in the order they are first encountered in the CSV.
     *
     * @param name - the department subject code (e.g. "CMPT").
     * @return the existing or newly created Department.
     */
    private Department findOrCreateDepartment(String name) {
        for (Department d : departments) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        Department newDept = new Department(name);
        departments.add(newDept);
        return newDept;
    }

    /**
     * Finds a Course within a Department by catalog number,
     * or creates it if it doesn't exist yet.
     *
     * @param dept          - the department to search within
     * @param catalogNumber - the catalog number string (e.g. "310").
     * @return the existing or newly created Course.
     */
    private Course findOrCreateCourse(Department dept, String catalogNumber) {
        for (Course c : dept.getCourses()) {
            if (c.getCatalogNumber().equals(catalogNumber)) {
                return c;
            }
        }
        Course newCourse = new Course(catalogNumber);
        dept.addCourse(newCourse);
        return newCourse;
    }

    // ------------------------------------------------------
    // Utility:
    // ------------------------------------------------------

    /**
     * Safely parses an integer string, returning 0 if the string is blank or non-numeric.
     * Some enrollment fields in the CSV are empty.
     *
     * @param s - the string to parse.
     * @return parsed integer, or 0 on failure.
     */
    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ------------------------------------------------------
    // Public API (used by the controller):
    // ------------------------------------------------------

    /**
     * Returns all departments loaded from the CSV, sorted alphabetically by name.
     *
     * @return sorted list of all departments.
     */
    public List<Department> getDepartments() {
        departments.sort((a, b) -> a.getName().compareTo(b.getName()));
        return departments;
    }
}
