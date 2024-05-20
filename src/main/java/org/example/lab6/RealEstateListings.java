package org.example.lab6;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class RealEstateListings extends Application {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final String URL = "jdbc:postgresql://localhost:5433/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "67134510"; // Замініть пароль на свій

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label addressLabel = new Label("Адреса помешкання:");
        TextField addressField = new TextField();
        gridPane.addRow(0, addressLabel, addressField);

        Label roomsLabel = new Label("Кількість кімнат:");
        TextField roomsField = new TextField();
        gridPane.addRow(1, roomsLabel, roomsField);

        Label areaLabel = new Label("Житлова площа:");
        TextField areaField = new TextField();
        gridPane.addRow(2, areaLabel, areaField);

        Label floorLabel = new Label("Поверх:");
        TextField floorField = new TextField();
        gridPane.addRow(3, floorLabel, floorField);

        Label rentLabel = new Label("Вартість оренди (місячно):");
        TextField rentField = new TextField();
        gridPane.addRow(4, rentLabel, rentField);

        CheckBox utilitiesIncludedCheckBox = new CheckBox("Комунальні послуги включені");
        gridPane.addRow(5, utilitiesIncludedCheckBox);

        Label phoneLabel = new Label("Телефон власника:");
        TextField phoneField = new TextField();
        gridPane.addRow(6, phoneLabel, phoneField);

        Button addAdvertisementButton = new Button("Додати оголошення");
        gridPane.add(addAdvertisementButton, 0, 7, 2, 1);

        Button displayAllButton = new Button("Показати всі оголошення");
        gridPane.add(displayAllButton, 0, 8, 2, 1);

        Button searchByCriteriaButton = new Button("Пошук за критеріями");
        gridPane.add(searchByCriteriaButton, 0, 9, 2, 1);

        TextArea resultArea = new TextArea();
        resultArea.setPrefRowCount(10);
        resultArea.setPrefColumnCount(50);
        gridPane.add(resultArea, 0, 10, 2, 1);

        addAdvertisementButton.setOnAction(event -> {
            String address = addressField.getText();
            int rooms = Integer.parseInt(roomsField.getText());
            double area = Double.parseDouble(areaField.getText());
            int floor = Integer.parseInt(floorField.getText());
            double rent = Double.parseDouble(rentField.getText());
            boolean utilitiesIncluded = utilitiesIncludedCheckBox.isSelected();
            String phone = phoneField.getText();

            String query = "INSERT INTO real_estate_listing (address, number_of_rooms, area, floor, rent_cost, utilities_included, owner_phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, address);
                preparedStatement.setInt(2, rooms);
                preparedStatement.setDouble(3, area);
                preparedStatement.setInt(4, floor);
                preparedStatement.setDouble(5, rent);
                preparedStatement.setBoolean(6, utilitiesIncluded);
                preparedStatement.setString(7, phone);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    resultArea.setText("Оголошення успішно додано.");
                } else {
                    resultArea.setText("Не вдалося додати оголошення.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        displayAllButton.setOnAction(event -> {
            String query = "SELECT * FROM real_estate_listing";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("Всі оголошення:\n");

                while (resultSet.next()) {
                    sb.append("Адреса: ").append(resultSet.getString("address")).append("\n")
                            .append("Кімнати: ").append(resultSet.getInt("number_of_rooms")).append("\n")
                            .append("Площа: ").append(resultSet.getDouble("area")).append("\n")
                            .append("Поверх: ").append(resultSet.getInt("floor")).append("\n")
                            .append("Вартість оренди: ").append(resultSet.getDouble("rent_cost")).append("\n")
                            .append("Комунальні послуги включені: ").append(resultSet.getBoolean("utilities_included")).append("\n")
                            .append("Телефон власника: ").append(resultSet.getString("owner_phone_number")).append("\n\n");
                }

                if (sb.toString().isEmpty()) {
                    resultArea.setText("Оголошень не знайдено.");
                } else {
                    resultArea.setText(sb.toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        searchByCriteriaButton.setOnAction(event -> {
            int minRooms = Integer.parseInt(roomsField.getText());
            double minRent = Double.parseDouble(rentField.getText());

            String query = "SELECT * FROM real_estate_listing WHERE number_of_rooms >= ? AND rent_cost >= ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, minRooms);
                preparedStatement.setDouble(2, minRent);

                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder includedUtilities = new StringBuilder();
                StringBuilder excludedUtilities = new StringBuilder();

                while (resultSet.next()) {
                    String address = resultSet.getString("address");
                    String number_of_rooms = resultSet.getString("number_of_rooms");
                    double area = resultSet.getDouble("area");
                    int floor = resultSet.getInt("floor");
                    double rent = resultSet.getDouble("rent_cost");
                    boolean utilitiesIncluded = resultSet.getBoolean("utilities_included");
                    String owner_phone_number = resultSet.getString("owner_phone_number");

                    String listingInfo = "Адреса: " + address + ", Кількість кімнат: " + number_of_rooms +
                            ", Житлова площа: " + area + ", Поверх: " + floor +
                            ", Вартість оренди: " + rent + ", Комунальні послуги включені: " +
                            utilitiesIncluded + ", Телефон власника: " + owner_phone_number + "\n";

                    if (utilitiesIncluded) {
                        includedUtilities.append(listingInfo);
                    } else {
                        excludedUtilities.append(listingInfo);
                    }
                }


                StringBuilder result = new StringBuilder();
                result.append("Помешкання з включеними комунальними послугами:\n")
                        .append(includedUtilities.toString())
                        .append("\nПомешкання без комунальних послуг:\n")
                        .append(excludedUtilities.toString());

                if (result.toString().isEmpty()) {
                    resultArea.setText("Оголошень не знайдено за вказаними критеріями.");
                } else {
                    resultArea.setText(result.toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(gridPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Система оголошень ріелтора");
        primaryStage.show();
    }
}
