# Student Information Management System
JavaFX (GUI + event handling) + JDBC with MySQL.

## Setup
1. Install JDK 17+ and MySQL; Maven installed.
2. Edit USER / PASS in `src/main/java/com/sims/DBConnection.java`.
3. (Optional) run `schema.sql`; the app also creates the DB and table itself.
4. Run: `mvn javafx:run`

## Features
Add, update, delete, view and search students (reg no, personal details, department, year, CGPA) with input validation.

## Concepts used (for the report/video)
- Event handling: ActionEvent on buttons, KeyEvent (Enter to search), selection listener on TableView
- JDBC: DriverManager, PreparedStatement, ResultSet, try-with-resources
- DAO pattern: Student (model), StudentDAO (database), MainApp (view/controller)
