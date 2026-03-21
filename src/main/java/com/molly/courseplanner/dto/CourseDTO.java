package com.molly.courseplanner.dto;

/**
 * Response shape for a course within a department.
 * Returned by GET /api/departments/{deptId}/courses
 *
 * Example JSON:
 * { "courseId": 4, "catalogNumber": "310" }
 */
public class CourseDTO {
    public long courseId;
    public String catalogNumber;
}
