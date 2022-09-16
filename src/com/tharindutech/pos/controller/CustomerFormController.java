package com.tharindutech.pos.controller;

import com.jfoenix.controls.JFXButton;
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
    private String searchText="";

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
                    searchText=newValue;
                    searchCustomers(searchText);
                }
        );


    }

    private void setData(CustomerTM tm) {
        txtId.setText(tm.getId());
        txtName.setText(tm.getName());
        txtAddress.setText(tm.getAddress());
        txtSalary.setText(String.valueOf(tm.getSalary()));
        btnSaveCustomer.setText("Update Customer");
    }

    private void searchCustomers(String text) {
        ObservableList<CustomerTM> tmList = FXCollections.observableArrayList();
        for (Customer c : DataBase.customerTable) {

            if(c.getName().contains(text) || c.getAddress().contains(text)){
                Button btn = new Button("DELETE");
                tmList.add(new CustomerTM(c.getId(), c.getName(), c.getAddress(), c.getSalary(), btn));

                btn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure that you want to delte?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get() == ButtonType.YES) {
                        boolean isDeleted = DataBase.customerTable.remove(c);
                        if (isDeleted) {
                            searchCustomers(searchText);
                            new Alert(Alert.AlertType.INFORMATION, "Customer Deleted Successfully").show();
                        } else {
                            new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                        }
                    }
                });
            }

        }

        tblCustomer.setItems(tmList);
    }

    public void saveCustomerOnAction(ActionEvent actionEvent) {
        Customer c = new Customer(txtId.getText(), txtName.getText(), txtAddress.getText(), Double.parseDouble(txtSalary.getText()));
        if (btnSaveCustomer.getText().equalsIgnoreCase("Save Customer")) {
            boolean isSaved = DataBase.customerTable.add(c);
            if (isSaved) {
                searchCustomers(searchText);
                clearFields();
                new Alert(Alert.AlertType.INFORMATION, "Customer Saved Successfully").show();
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again !").show();
            }
        } else {
            for (int i = 0; i < DataBase.customerTable.size(); i++) {
                if (txtId.getText().equalsIgnoreCase(DataBase.customerTable.get(i).getId())) {
                    DataBase.customerTable.get(i).setName(txtName.getText());
                    DataBase.customerTable.get(i).setAddress(txtAddress.getText());
                    DataBase.customerTable.get(i).setSalary(Double.parseDouble(txtSalary.getText()));
                    searchCustomers(searchText);
                    new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully").show();
                    clearFields();
                }

            }

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
