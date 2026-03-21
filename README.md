Course Planner API

A RESTful API built with Java and Spring Boot, that parses SFU course enrollment data and exposes it through a structured set of HTTP endpoints.

Technical Overview:
The API loads a CSV dataset of SFU course offerings at startup, and builds a fully normalized in-memory data model organized as a four-level hierarchy:
Department -> Course -> CourseOffering -> Section

All data is served as JSON through a REST controller.

Endpoints:
GET /api/about
GET /api/departments
GET /api/departments/{deptId}/courses
GET /api/departments/{deptId}/courses/{courseId}/offerings
GET /api/departments/{deptId}/courses/{courseId}/offerings/{offeringId}

Skills Demonstrated:
1. REST API Design:
- Desined and implemented a multi-level REST API in Spring Boot using @RestController, @GetMapping, and @PathVariable.
- Endpoints follow a consistent nested resource structure, and return appropriate HTTP status codes (200, 201, and 404).

2. Object-Oriented Design:
- The data model is cleanly separated into Department, Course, CourseOffering, and Section classes, each with a single responsibility.
- A dedicated CoursePlannerManager class handles all CSV parsing and model construction, keeping business logic out of the controller.

3. Data Parsing:
- Implemented a CSV parser that handles real-world data inconsistencies, including trailing whitespace, blank fields, literal <null> values, and comma-separated instructor names within a single column.
- Used defensive parsing with fallback handling throughout.

4. DTO Pattern:
- Used Data Transfer Objects to decouple the internal model from the API response shape, following standard Spring Boot conventions.

5. Readable Code Practices:
- Prioritized named boolean variables and descriptive method named over condensed logic, keeping the codebase easy to follow & review.

Tech Stack:
- Java 21
- Spring Boot 3
- Maven
- Jackson (JSON serialization)

Running Locally:
1. Clone the repo.
2. Open in IntelliJ IDEA.
3. Run CoursePlannerApplication.java
4. API is available at http://localhost:8080

Developed as part of a course project at SFU.
