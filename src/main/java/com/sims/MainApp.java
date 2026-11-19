package com.sims;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Main window: sidebar + Dashboard view + Students view.
 * Event handling: ActionEvents on buttons, KeyEvent on search, selection listener on table.
 */
public class MainApp extends Application {

    private final StudentDAO dao = new StudentDAO();
    private final ObservableList<Student> data = FXCollections.observableArrayList();
    private final TableView<Student> table = new TableView<>();

    // Form fields
    private final TextField regNo = new TextField();
    private final TextField name = new TextField();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final ComboBox<String> dept = new ComboBox<>();
    private final ComboBox<Integer> year = new ComboBox<>();
    private final TextField cgpa = new TextField();  // free-text, validated 1-10
    private final TextField searchField = new TextField();
    private final Label status = new Label("Ready");

    // Sidebar buttons
    private Button btnDashboard;
    private Button btnStudents;

    // Dashboard stat labels
    private Label statTotal = new Label("0");
    private Label statAvgCgpa = new Label("0.00");
    private Label statDepts = new Label("0");

    // Content area
    private StackPane contentArea;

    @Override
    public void start(Stage stage) {
        try { DBConnection.init(); }
        catch (Exception e) { showError("Cannot connect to MySQL.\n" + e.getMessage()); }

        BorderPane root = new BorderPane();

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Top bar
        HBox topbar = buildTopBar();
        root.setTop(topbar);

        // Content
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        // Status bar
        HBox statusBar = new HBox(status);
        statusBar.getStyleClass().add("status-bar");
        root.setBottom(statusBar);

        refresh();
        showDashboard();

        Scene scene = new Scene(root, 1120, 640);
        scene.getStylesheets().add(getClass().getResource("/com/sims/style.css").toExternalForm());
        stage.setTitle("Student Information Management System");
        stage.setScene(scene);
        stage.show();
    }

    // ===== Sidebar =====

    private VBox buildSidebar() {
        Label title = new Label("SIMS");
        title.getStyleClass().add("sidebar-title");

        btnDashboard = new Button("Dashboard");
        btnStudents  = new Button("Students");

        for (Button b : new Button[]{btnDashboard, btnStudents}) {
            b.getStyleClass().add("sidebar-btn");
            b.setMaxWidth(Double.MAX_VALUE);
        }

        btnDashboard.setOnAction(e -> { setActive(btnDashboard); showDashboard(); });
        btnStudents.setOnAction(e  -> { setActive(btnStudents);  showStudents();  });

        VBox box = new VBox(0, title, new Separator(), btnDashboard, btnStudents);
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(170);
        return box;
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnDashboard, btnStudents}) {
            b.getStyleClass().remove("sidebar-btn-active");
        }
        active.getStyleClass().add("sidebar-btn-active");
    }

    // ===== Top bar =====

    private HBox buildTopBar() {
        Label t = new Label("Student Information Management System");
        t.getStyleClass().add("topbar-title");
        Label s = new Label("SRM University – Advanced Java Project");
        s.getStyleClass().add("topbar-sub");
        VBox titles = new VBox(2, t, s);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label user = new Label("admin");
        user.setStyle("-fx-text-fill:#1a3a5c; -fx-font-weight:bold;");
        HBox bar = new HBox(14, titles, spacer, user);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // ===== Dashboard view =====

    private void showDashboard() {
        updateStats();
        VBox dash = new VBox(20);
        dash.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Dashboard");
        heading.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#1a3a5c;");

        HBox cards = new HBox(16,
            statCard("Total Students",  statTotal),
            statCard("Average CGPA",    statAvgCgpa),
            statCard("Departments",     statDepts)
        );
        cards.setAlignment(Pos.CENTER_LEFT);

        // Recent students mini-table
        Label recLabel = new Label("Recent Records");
        recLabel.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a3a5c;");

        TableView<Student> recent = new TableView<>();
        recent.setItems(data);
        recent.setPrefHeight(260);
        recent.getStyleClass().add("table-view");
        recent.getColumns().addAll(
            col("Reg. No",    "regNo",      130),
            col("Name",       "name",       160),
            col("Department", "department",  90),
            col("Year",       "year",        60),
            col("CGPA",       "cgpa",        70)
        );

        dash.getChildren().addAll(heading, cards, recLabel, recent);
        contentArea.getChildren().setAll(dash);
    }

    private VBox statCard(String labelText, Label number) {
        number.getStyleClass().add("stat-number");
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(6, number, lbl);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(200);
        return card;
    }

    private void updateStats() {
        int total = data.size();
        statTotal.setText(String.valueOf(total));
        double avg = data.stream().mapToDouble(Student::getCgpa).average().orElse(0);
        statAvgCgpa.setText(String.format("%.2f", avg));
        long deptCount = data.stream().map(Student::getDepartment)
            .filter(d -> d != null && !d.isBlank()).distinct().count();
        statDepts.setText(String.valueOf(deptCount));
    }

    // ===== Students view =====

    private void showStudents() {
        VBox view = new VBox(12);
        view.setMaxWidth(Double.MAX_VALUE);

        Label heading = new Label("Manage Students");
        heading.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#1a3a5c;");

        HBox content = new HBox(16, buildForm(), buildTablePanel());
        HBox.setHgrow(buildTablePanel(), Priority.ALWAYS);
        view.getChildren().addAll(heading, buildSearchBar(), content);

        // Rebuild to get fresh references in layout
        VBox form = buildForm();
        VBox tablePanel = buildTablePanel();
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        HBox mainRow = new HBox(16, form, tablePanel);
        view.getChildren().setAll(heading, buildSearchBar(), mainRow);

        contentArea.getChildren().setAll(view);
    }

    private HBox buildSearchBar() {
        searchField.setPromptText("Search by name, reg no or department…");
        searchField.getStyleClass().add("search-field");

        Button search = new Button("Search");
        search.getStyleClass().add("btn-primary");
        Button all = new Button("Show All");
        all.getStyleClass().add("btn-secondary");

        searchField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) doSearch(); });
        search.setOnAction(e -> doSearch());
        all.setOnAction(e -> { searchField.clear(); refresh(); });

        HBox bar = new HBox(10, searchField, search, all);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox buildForm() {
        // Populate combos fresh each time (avoid duplicate items on re-open)
        if (dept.getItems().isEmpty()) {
            dept.getItems().addAll("CSE", "ECE", "EEE", "MECH", "CIVIL", "BBA", "MBA", "MCA");
        }
        if (year.getItems().isEmpty()) {
            year.getItems().addAll(2023, 2024, 2025, 2026);
        }

        // Set prompt text
        regNo.setPromptText("e.g. AP24110011909");
        name.setPromptText("Full name");
        email.setPromptText("example@email.com");
        phone.setPromptText("10-digit mobile");
        dept.setPromptText("Select department");
        year.setPromptText("Admission year");
        cgpa.setPromptText("1.0 – 10.0");

        // Make combos full width
        dept.setMaxWidth(Double.MAX_VALUE);
        year.setMaxWidth(Double.MAX_VALUE);

        VBox fields = new VBox(6);
        String[][] pairs = {
            {"Reg. No *", null}, {"Name *", null}, {"Email", null},
            {"Phone", null}, {"Department", null}, {"Year", null}, {"CGPA (1–10)", null}
        };
        javafx.scene.Node[] controls = {regNo, name, email, phone, dept, year, cgpa};

        for (int i = 0; i < controls.length; i++) {
            Label lbl = new Label(pairs[i][0]);
            lbl.setStyle("-fx-text-fill:#4a6080; -fx-font-size:11px; -fx-font-weight:bold;");
            fields.getChildren().addAll(lbl, controls[i]);
        }

        Button add = new Button("Add");     add.getStyleClass().add("btn-primary");   add.setMinWidth(70);
        Button upd = new Button("Update");  upd.getStyleClass().add("btn-secondary"); upd.setMinWidth(70);
        Button del = new Button("Delete");  del.getStyleClass().add("btn-danger");    del.setMinWidth(70);
        Button clr = new Button("Clear");   clr.getStyleClass().add("btn-secondary"); clr.setMinWidth(70);

        add.setOnAction(e -> onAdd());
        upd.setOnAction(e -> onUpdate());
        del.setOnAction(e -> onDelete());
        clr.setOnAction(e -> clearForm());

        HBox buttons = new HBox(6, add, upd, del, clr);
        buttons.setPadding(new Insets(6, 0, 0, 0));

        Label formTitle = new Label("Student Details");
        formTitle.getStyleClass().add("form-title");

        VBox panel = new VBox(8, formTitle, fields, buttons);
        panel.getStyleClass().add("form-panel");
        panel.setPrefWidth(300);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTablePanel() {
        table.getColumns().clear();
        table.getColumns().addAll(
            col("ID",         "id",          45),
            col("Reg. No",    "regNo",       120),
            col("Name",       "name",        150),
            col("Email",      "email",       170),
            col("Phone",      "phone",       100),
            col("Dept",       "department",   70),
            col("Year",       "year",         50),
            col("CGPA",       "cgpa",         60)
        );
        table.setItems(data);
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        // Selection listener (delegation event model)
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                regNo.setText(s.getRegNo());
                name.setText(s.getName());
                email.setText(s.getEmail() != null ? s.getEmail() : "");
                phone.setText(s.getPhone() != null ? s.getPhone() : "");
                dept.setValue(s.getDepartment());
                year.setValue(s.getYear());
                cgpa.setText(String.valueOf(s.getCgpa()));
            }
        });

        VBox panel = new VBox(table);
        panel.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#d8e4f0; -fx-border-radius:10; -fx-border-width:1;");
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private TableColumn<Student, ?> col(String title, String prop, double width) {
        TableColumn<Student, Object> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    // ===== Event handlers =====

    private void onAdd() {
        Student s = readForm(0);
        if (s == null) return;
        try {
            dao.add(s);
            setStatus("Student added.");
            clearForm(); refresh(); updateStats();
        } catch (Exception ex) {
            showError("Could not add student (duplicate Reg. No?).\n" + ex.getMessage());
        }
    }

    private void onUpdate() {
        Student sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Select a row to update."); return; }
        Student s = readForm(sel.getId());
        if (s == null) return;
        try {
            dao.update(s);
            setStatus("Student updated.");
            clearForm(); refresh(); updateStats();
        } catch (Exception ex) {
            showError("Update failed.\n" + ex.getMessage());
        }
    }

    private void onDelete() {
        Student sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Select a row to delete."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + sel.getName() + "?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    dao.delete(sel.getId());
                    setStatus("Student deleted.");
                    clearForm(); refresh(); updateStats();
                } catch (Exception ex) {
                    showError("Delete failed.\n" + ex.getMessage());
                }
            }
        });
    }

    private void doSearch() {
        String k = searchField.getText().trim();
        try {
            List<Student> list = k.isEmpty() ? dao.getAll() : dao.search(k);
            data.setAll(list);
            setStatus(list.size() + " record(s) found.");
        } catch (Exception ex) {
            showError("Search failed.\n" + ex.getMessage());
        }
    }

    // ===== Helpers =====

    private Student readForm(int id) {
        if (regNo.getText().isBlank() || name.getText().isBlank()) {
            showError("Reg. No and Name are required.");
            return null;
        }
        if (!phone.getText().isBlank() && !phone.getText().matches("[0-9]{10}")) {
            showError("Phone must be exactly 10 digits.");
            return null;
        }
        double g;
        try {
            g = cgpa.getText().isBlank() ? 0 : Double.parseDouble(cgpa.getText());
            if (g < 1 || g > 10) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("CGPA must be a number between 1 and 10.");
            return null;
        }
        int y = year.getValue() == null ? 2024 : year.getValue();
        return new Student(id, regNo.getText().trim(), name.getText().trim(),
            email.getText().trim(), phone.getText().trim(), dept.getValue(), y, g);
    }

    private void refresh() {
        try {
            data.setAll(dao.getAll());
            setStatus(data.size() + " student(s) loaded.");
        } catch (Exception e) {
            setStatus("Database not available.");
        }
    }

    private void clearForm() {
        regNo.clear(); name.clear(); email.clear(); phone.clear(); cgpa.clear();
        dept.setValue(null); year.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void setStatus(String msg) { status.setText(msg); }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
