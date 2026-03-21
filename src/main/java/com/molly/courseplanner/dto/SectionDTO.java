package com.molly.courseplanner.dto;

/**
 * Response shape for one section within an offering.
 * Returned by GET /api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}
 *
 * Example JSON:
 * { "type": "LEC", "enrollmentCap": 150, "enrollmentTotal": 95 }
 */
public class SectionDTO {
    public String type; // Component code: LEC, TUT, LAB, etc.
    public int enrollmentCap;
    public int enrollmentTotal;
}
