package com.molly.courseplanner.dto;

/**
 * Response shape for a department.
 * Returned by GET /api/departments
 *
 * Example JSON:
 * { "deptId": 1, "name": "CMPT" }
 */
public class DepartmentDTO {
    public long deptId;
    public String name;
}
