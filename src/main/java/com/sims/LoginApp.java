package com.sims;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Login screen with simple text captcha.
 * Default credentials: admin / admin123
 */
public class LoginApp extends Application {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private TextField userField;
    private PasswordField passField;
    private TextField captchaInput;
    private Label captchaLabel;
    private Label errorLabel;
    private String currentCaptcha;

    @Override
    public void start(Stage stage) {
        try { DBConnection.init(); } catch (Exception e) { /* handled in MainApp */ }

        stage.setTitle("SIMS – Login");
        stage.setScene(buildScene(stage));
        stage.setResizable(false);
        stage.show();
    }

    private Scene buildScene(Stage stage) {
        // ---- Title area ----
        Label title = new Label("SIMS");
        title.getStyleClass().add("login-title");
        Label sub = new Label("Student Information Management System");
        sub.getStyleClass().add("login-sub");
        VBox titleBox = new VBox(4, title, sub);
        titleBox.setAlignment(Pos.CENTER);

        // ---- Fields ----
        userField = new TextField();
        userField.setPromptText("Username");
        userField.getStyleClass().add("text-field");

        passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("text-field");

        // ---- Captcha ----
        currentCaptcha = newCaptcha();
        captchaLabel = new Label(currentCaptcha);
        captchaLabel.getStyleClass().add("captcha-box");
        captchaLabel.setMinWidth(130);
        captchaLabel.setAlignment(Pos.CENTER);

        Button refreshCaptcha = new Button("↻");
        refreshCaptcha.getStyleClass().add("btn-secondary");
        refreshCaptcha.setOnAction(e -> { currentCaptcha = newCaptcha(); captchaLabel.setText(currentCaptcha); });

        captchaInput = new TextField();
        captchaInput.setPromptText("Enter captcha");
        captchaInput.getStyleClass().add("text-field");

        HBox captchaRow = new HBox(8, captchaLabel, refreshCaptcha);
        captchaRow.setAlignment(Pos.CENTER_LEFT);

        // ---- Error ----
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e05252; -fx-font-size: 12px;");

        // ---- Login button ----
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setPrefWidth(240);
        loginBtn.setOnAction(e -> doLogin(stage));
        passField.setOnAction(e -> doLogin(stage));
        captchaInput.setOnAction(e -> doLogin(stage));

        // ---- Hint ----
        Label hint = new Label("Default: admin / admin123");
        hint.setStyle("-fx-text-fill: #aab8c8; -fx-font-size: 11px;");

        // ---- Card ----
        VBox card = new VBox(14,
                titleBox,
                new Separator(),
                new Label("Username"), userField,
                new Label("Password"), passField,
                new Label("Captcha"), captchaRow, captchaInput,
                errorLabel,
                loginBtn,
                hint
        );
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(300);

        // ---- Root ----
        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-root");
        root.setPadding(new Insets(60));

        Scene scene = new Scene(root, 480, 580);
        scene.getStylesheets().add(getClass().getResource("/com/sims/style.css").toExternalForm());
        return scene;
    }

    private void doLogin(Stage stage) {
        String u = userField.getText().trim();
        String p = passField.getText();
        String c = captchaInput.getText().trim();

        if (!c.equalsIgnoreCase(currentCaptcha)) {
            errorLabel.setText("Captcha is wrong. Try again.");
            currentCaptcha = newCaptcha();
            captchaLabel.setText(currentCaptcha);
            captchaInput.clear();
            return;
        }
        if (!u.equals(ADMIN_USER) || !p.equals(ADMIN_PASS)) {
            errorLabel.setText("Invalid username or password.");
            currentCaptcha = newCaptcha();
            captchaLabel.setText(currentCaptcha);
            captchaInput.clear();
            return;
        }

        // Open main window and close login
        Stage main = new Stage();
        new MainApp().start(main);
        stage.close();
    }

    private String newCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public static void main(String[] args) { launch(args); }
}
