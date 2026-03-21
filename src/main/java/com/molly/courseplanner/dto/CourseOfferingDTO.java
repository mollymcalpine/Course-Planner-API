package com.molly.courseplanner.dto;

/**
 * Response shape for one offering of a course.
 * Returned by GET /api/departments/{deptId}/courses/{courseId}/offerings
 *
 * Example JSON:
 * {
 *   "courseOfferingId": 12,
 *   "location": "BURNABY",
 *   "instructors": "Alice Smith, Bob Jones",
 *   "term": "Fall",
 *   "year": 2018
 * }
 */
public class CourseOfferingDTO {
    public long courseOfferingId;
    public String location;
    public String instructors; // Joined into a single comma-separated string.
    public String term;        // "Spring", "Summer", or "Fall".
    public int year;           // e.g. 2018.
    public int semesterCode;   // e.g. 1181
}
