package com.tharindutech.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.tharindutech.pos.db.DBConnection;
import com.tharindutech.pos.db.DataBase;
import com.tharindutech.pos.model.Customer;
import com.tharindutech.pos.view.tm.CustomerTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class CustomerFormController {


    public TextField txtId;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtSalary;
    public TableView<CustomerTM> tblCustomer;
    public TableColumn colId;
    public TableColumn colName;
    public TableColumn colAddress;
    public TableColumn colSalary;
    public TableColumn colOptions;
    public JFXButton btnSaveCustomer;
    public AnchorPane customerFormContext;
    public TextField txtSearch;
    private String searchText = "";

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colOptions.setCellValueFactory(new PropertyValueFactory<>("btn"));
        searchCustomers(searchText);

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (null != newValue) //better than (newValue!=null)
                setData(newValue);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText = newValue;
            searchCustomers(searchText);
        });


    }

    private void setData(CustomerTM tm) {
        txtId.setText(tm.getId());
        txtName.setText(tm.getName());
        txtAddress.setText(tm.getAddress());
        txtSalary.setText(String.valueOf(tm.getSalary()));
        btnSaveCustomer.setText("Update Customer");
    }

    private void searchCustomers(String text) {
        String searchText = "%" + text + "%";
        try {
            ObservableList<CustomerTM> tmList = FXCollections.observableArrayList();
            String sql = "SELECT * FROM Customer WHERE name LIKE ? || address LIKE ?";
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
            statement.setString(1, searchText);
            statement.setString(2, searchText);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Button btn = new Button("DELETE");
                CustomerTM tm = new CustomerTM(set.getString(1), set.getString(2), set.getString(3), set.getDouble(4), btn);
                tmList.add(tm);

                btn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure that you want to delete?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get() == ButtonType.YES) {

                        try {
                            String sql1 = "DELETE FROM Customer WHERE id=?";
                            PreparedStatement statement1 = DBConnection.getInstance().getConnection().prepareStatement(sql1);
                            statement1.setString(1, tm.getId());
                            if (statement1.executeUpdate() > 0) {
                                searchCustomers(searchText);
                                new Alert(Alert.AlertType.INFORMATION, "Customer Deleted Successfully").show();
                            } else {
                                new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                            }

                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }


                    }
                });

            }
            tblCustomer.setItems(tmList);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCustomerOnAction(ActionEvent actionEvent) {
        Customer c = new Customer(txtId.getText(), txtName.getText(), txtAddress.getText(), Double.parseDouble(txtSalary.getText()));
        if (btnSaveCustomer.getText().equalsIgnoreCase("Save Customer")) {
            try {
                String sql = "INSERT INTO Customer VALUES(?,?,?,?)";
                PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
                statement.setString(1, c.getId());
                statement.setString(2, c.getName());
                statement.setString(3, c.getAddress());
                statement.setDouble(4, c.getSalary());

                if (statement.executeUpdate() > 0) {
                    searchCustomers(searchText);
                    clearFields();
                    new Alert(Alert.AlertType.INFORMATION, "Customer Saved Successfully").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }


        } else {

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Thogakade", "root", "1234");
                String sql = "UPDATE Customer SET name=?,address=?,salary=? WHERE id= ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, c.getName());
                statement.setString(2, c.getAddress());
                statement.setDouble(3, c.getSalary());
                statement.setString(4, c.getId());

                if (statement.executeUpdate() > 0) {
                    searchCustomers(searchText);
                    clearFields();
                    new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

//            for (int i = 0; i < DataBase.customerTable.size(); i++) {
//                if (txtId.getText().equalsIgnoreCase(DataBase.customerTable.get(i).getId())) {
//                    DataBase.customerTable.get(i).setName(txtName.getText());
//                    DataBase.customerTable.get(i).setAddress(txtAddress.getText());
//                    DataBase.customerTable.get(i).setSalary(Double.parseDouble(txtSalary.getText()));
//                    searchCustomers(searchText);
//                    new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully").show();
//                    clearFields();
//                }
//            }

        }
    }

    private void clearFields() {
        txtId.clear();
        txtName.clear();
        txtAddress.clear();
        txtSalary.clear();
    }

    public void newCustomerOnAction(ActionEvent actionEvent) {
        btnSaveCustomer.setText("Save Customer");
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) customerFormContext.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/DashboardForm.fxml"))));
    }


}
