package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import database.database_utility;
import java.sql.Connection;

public class addstocksController {
    @FXML
    private Pane addPane;
    @FXML @SuppressWarnings("all")
    public TextField text_field1;
    @FXML
    public TextField textfield2;
    @FXML @SuppressWarnings("all")
    public TextField text_field3;
    @FXML @SuppressWarnings("all")
    public TextField text_field4;
    @FXML
    private Text selectedItem;
    @FXML
    public TextField newstock;
    @FXML
    public Button continueButton;
    @FXML
    private Text monthLabel;

    private int itemCode = -1;
    private int currentSoh = 0;
    private dashboard.dashboardController dashboardControllerRef;

    @FXML
    private void handleExit() {
        // Get the window/stage the button is in and close it
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }


    public void setSelectedItemDescription(String description) {
        if (selectedItem != null) {
            selectedItem.setText(description);
        }
    }

    // Call this from dashboardController when opening the form
    public void setItemCodeAndSoh(int itemCode, int soh) {
        this.itemCode = itemCode;
        this.currentSoh = soh;
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
        if (monthLabel != null && dashboardControllerRef != null) {
            String selectedMonth = dashboardControllerRef.monthComboBox.getValue();
            monthLabel.setText("New Stocks (" + selectedMonth + "):");
        }
    }

    @FXML
    private void initialize() {
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
        }
    }

    private void handleContinue() {
        if (itemCode == -1) {
            showAlert("Error", "No item selected.");
            return;
        }
        String newStockStr = newstock.getText();
        int addStock;
        try {
            addStock = Integer.parseInt(newStockStr);
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please enter a valid number for new stocks.");
            return;
        }
        int updatedSoh = currentSoh + addStock;
        Connection connect = null;
        try {
            // Get the selected month from dashboardController
            String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();
            
            // Update stock_onhand table with the correct month column
            Object[] result = database_utility.update(
                String.format("UPDATE stock_onhand SET %s1 = ? WHERE item_code = ?", selectedMonth),
                updatedSoh, itemCode
            );
            if (result != null) {
                connect = (Connection) result[0];
            }
            showAlert("Success", "Stocks updated successfully.");
            // Auto-refresh the table in dashboard
            if (dashboardControllerRef != null) {
                dashboardControllerRef.inventory_management_query();

                // Add recent notification
                String description = selectedItem != null ? selectedItem.getText() : "";
                dashboardControllerRef.addRecentStockNotification(addStock, description);
            }
            // Optionally close the window
            Stage stage = (Stage) continueButton.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            showAlert("Database Error", "Failed to update stocks: " + ex.getMessage());
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
