package com.molly.courseplanner.controller;

import com.molly.courseplanner.dto.AboutDTO;
import com.molly.courseplanner.dto.CourseDTO;
import com.molly.courseplanner.dto.CourseOfferingDTO;
import com.molly.courseplanner.dto.DepartmentDTO;
import com.molly.courseplanner.dto.SectionDTO;
import com.molly.courseplanner.model.Course;
import com.molly.courseplanner.model.CoursePlannerManager;
import com.molly.courseplanner.model.CourseOffering;
import com.molly.courseplanner.model.Department;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for the Course Planner API.
 *
 * All endpoints are prefixed with /api (set by @RequestMapping below).
 *
 * Endpoints:
 * - GET /api/about
 * - GET /api/departments
 * - GET /api/departments/{deptId}/courses
 * - GET /api/departments/{deptId}/courses/{courseId}/offerings
 * - GET /api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}
 *
 * @RestController combines @Controller and @ResponseBody, meaning every method
 * automatically serializes its return value to JSON.
 */
@RestController
@RequestMapping("/api")
public class CourseController {

    // The manager loads the CSV at construction time and holds the full model.
    // Since this controller is a Spring singleton, the CSV is only parsed one at application startup.
    private final CoursePlannerManager manager = new CoursePlannerManager();

    // -----------------------------------------------------------------------
    // GET /api/about
    // -----------------------------------------------------------------------

    /**
     * Returns basic information about this application.
     *
     * Example response:
     * { "appName": "Course Planner", "author": "Molly McAlpine" }
     */
    @GetMapping("/about")
    public AboutDTO getAbout() {
        return new AboutDTO("Course Planner", "Molly McAlpine");
    }

    // -----------------------------------------------------------------------
    // GET /api/departments
    // -----------------------------------------------------------------------

    /**
     * Returns all departments, sorted alphabetically by name.
     *
     * Example response:
     * [
     *  { "deptId"}: 1, "name": "CMPT" },
     *  { "deptId"}: 2, "name": "ENSC" },
     *  ...
     * ]
     *
     */
    @GetMapping("/departments")
    public List<DepartmentDTO> getDepartments() {
        return manager.getDepartments()
                .stream()
                .map(dept -> {
                    DepartmentDTO dto = new DepartmentDTO();
                    dto.deptId = dept.getDeptId();
                    dto.name = dept.getName();
                    return dto;
                }).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // GET /api/departments/{deptId}/courses
    // -----------------------------------------------------------------------

    /**
     * Returns all courses within a department, sorted by catalog number.
     *
     * @PathVariable extracts the {deptId} from the URL automatically.
     *
     * Returns 404 if no department exists with that ID.
     *
     * Example response:
     * [
     *  { "courseId": 3, "catalogNumber": "110" },
     *  { "courseId": 4, "catalogNumber": "310" }.
     *  ...
     * ]
     */
    @GetMapping("/departments/{deptId}/courses")
    public List<CourseDTO> getCourses(@PathVariable long deptId) {
        Department dept = findDepartment(deptId);

        return dept.getCourses()
                .stream()
                // Sort catalog numbers numerically where possible, and alphabetically otherwise:
                .sorted((c1, c2) -> compareCatalogNumbers(c1.getCatalogNumber(), c2.getCatalogNumber()))
                .map(course -> {
                    CourseDTO dto = new CourseDTO();
                    dto.courseId = course.getCourseId();
                    dto.catalogNumber = course.getCatalogNumber();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // GET /api/departments/{deptId}/courses/{courseId}/offerings
    // -----------------------------------------------------------------------

    /**
     * Returns all offerings of a specific course, sorted chronologically by semester code.
     *
     * Returns 404 if the department or course is not found.
     *
     * Example response:
     * [
     *  {
     *      "courseOfferingId": 12,
     *      "location": "BURNABY",
     *      "instructors": "Alice Smith, Bob Jones",
     *      "term": "Fall",
     *      "year": 2018,
     *      "semesterCode": 1187
     *  },
     *  ...
     * ]
     */
    @GetMapping("/departments/{deptId}/courses/{courseId}/offerings")
    public List<CourseOfferingDTO> getOfferings(@PathVariable long deptId, @PathVariable long courseId) {
        Department dept = findDepartment(deptId);
        Course course = findCourse(dept, courseId);

        return course.getOfferings()
                .stream()
                // Sort chronologically using the semester code (lower = earlier):
                .sorted((o1, o2) -> Integer.compare(o1.getSemesterCode(), o2.getSemesterCode()))
                .map(offering -> {
                    CourseOfferingDTO dto = new CourseOfferingDTO();
                    dto.courseOfferingId = offering.getCourseOfferingId();
                    dto.location = offering.getLocation();
                    // Join instructor list into a single readable string:
                    dto.instructors = String.join(", ", offering.getInstructors());
                    dto.term = offering.getTerm();
                    dto.year = offering.getYear();
                    dto.semesterCode = offering.getSemesterCode();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // GET /api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}
    // -----------------------------------------------------------------------

    /**
     * Returns all sections (LEC, TUT, LAB, etc.) for a specific offering.
     *
     * Returns 404 if the department, course, or offering is not found.
     *
     * Example response:
     * [
     *  { "type": "LEC", "enrollmentCap": 150, "enrollmentTotal": 95 },
     *  { "type": "TUT", "enrollmentCap": 30, "enrollmentTotal": 28 },
     * ]
     *
     */
    @GetMapping("/departments/{deptId}/courses/{courseId}/offerings/{offeringId}")
    public List<SectionDTO> getSections(@PathVariable long deptId, @PathVariable long courseId, @PathVariable long offeringId) {
        Department dept = findDepartment(deptId);
        Course course = findCourse(dept, courseId);
        CourseOffering offering = findOffering(course, offeringId);

        return offering.getSections()
                .stream()
                .map(section -> {
                    SectionDTO dto = new SectionDTO();
                    dto.type = section.getType();
                    dto.enrollmentCap = section.getEnrollmentCap();
                    dto.enrollmentTotal = section.getEnrollmentTotal();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Private Lookup Helpers:
    // -----------------------------------------------------------------------

    /**
     * Finds a department by ID, or throws a 404 if not found.
     * (Centralizing this logic to avoid repeating the or-Else-Throw
     * pattern in every endpoint).
     */
    private Department findDepartment(long deptId) {
        return manager.getDepartments()
                .stream()
                .filter(d -> d.getDeptId() == deptId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found: " + deptId));
    }

    /**
     * Finds a course within a department by ID, or throws a 404.
     */
    private Course findCourse(Department dept, long courseId) {
        return dept.getCourses()
                .stream()
                .filter(c -> c.getCourseId() == courseId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + courseId));
    }

    /**
     * Finds an offering within a course by ID, or throws a 404.
     */
    private CourseOffering findOffering(Course course, long offeringId) {
        return course.getOfferings()
                .stream()
                .filter(o -> o.getCourseOfferingId() == offeringId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offering not found: " + offeringId));
    }

    /**
     * Compares two catalog number string for sorting purposes.
     * First tries to compare numerically (e.g. 110 < 310 < 454),
     * and goes back to alphabetical for non-numeric values (e.g. "1XX").
     */
    private int compareCatalogNumbers(String a, String b) {
        try {
            // Strip any trailing letters (e.g. "454W" -> 454) for numeric comparison:
            int numA = Integer.parseInt(a.replaceAll("[^0-9]", ""));
            int numB = Integer.parseInt(b.replaceAll("[^0-9]", ""));
            if (numA != numB) {
                return Integer.compare(numA, numB);
            }
        } catch (NumberFormatException e) {
            // Go to alphabetical.
        }
        return a.compareTo(b);
    }
}

