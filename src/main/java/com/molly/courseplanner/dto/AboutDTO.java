package com.molly.courseplanner.dto;

/**
 * Response shape for the /api/about endpoint.
 * A simple informational endpoint that identifies the app & its author.
 *
 * Example JSON:
 * { "appName": "Course Planner", "author": "Molly McAlpine" }
 */
public class AboutDTO {
    public String appName;
    public String author;

    // Constructor for convenience (the controller can build this in one line):
    public AboutDTO(String appName, String author) {
        this.appName = appName;
        this.author = author;
    }
}
