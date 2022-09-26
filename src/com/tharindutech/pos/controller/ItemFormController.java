package com.tharindutech.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.tharindutech.pos.db.DBConnection;
import com.tharindutech.pos.db.DataBase;
import com.tharindutech.pos.model.Customer;
import com.tharindutech.pos.model.Item;
import com.tharindutech.pos.view.tm.CustomerTM;
import com.tharindutech.pos.view.tm.ItemTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ItemFormController {
    public AnchorPane ItemFormContext;
    public TextField txtCode;
    public TextField txtDescription;
    public TextField txtUnitPrice;
    public TextField txtQtyOnHand;
    public TextField txtSearch;
    public JFXButton btnSaveItem;
    public TableView tblCustomer;
    public TableColumn colCode;
    public TableColumn colDescription;
    public TableColumn colUnitPrice;
    public TableColumn colQtyOnHand;
    public TableColumn colOptions;
    public TableView tblItem;

    private String searchText = "";


    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQtyOnHand.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colOptions.setCellValueFactory(new PropertyValueFactory<>("btn"));
        searchItems(searchText);
        tblItem.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (null != newValue) //better than (newValue!=null)
                setData((ItemTM) newValue);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    searchText = newValue;
                    searchItems(searchText);
                }
        );


    }

    private void setData(ItemTM tm) {
        txtCode.setText(tm.getCode());
        txtDescription.setText(tm.getDescription());
        txtUnitPrice.setText(String.valueOf(tm.getUnitPrice()));
        txtQtyOnHand.setText(String.valueOf(tm.getQtyOnHand()));
        btnSaveItem.setText("Update Item");
    }


    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ItemFormContext.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/DashboardForm.fxml"))));
    }

    public void newItemOnAction(ActionEvent actionEvent) {
        btnSaveItem.setText("Save Item");
    }

    public void saveItemOnAction(ActionEvent actionEvent) {
        Item i = new Item(txtCode.getText(), txtDescription.getText(), Double.parseDouble(txtUnitPrice.getText()), Integer.parseInt(txtQtyOnHand.getText()));
        if (btnSaveItem.getText().equalsIgnoreCase("Save Item")) {

            try {
                String sql = "INSERT INTO Item VALUES(?,?,?,?)";
                PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
                statement.setString(1, i.getCode());
                statement.setString(2, i.getDescription());
                statement.setDouble(3, i.getUnitPrice());
                statement.setInt(4, i.getQtyOnHand());

                if (statement.executeUpdate() > 0) {
                    searchItems(searchText);
                    clearFields();
                    new Alert(Alert.AlertType.INFORMATION, "Item  Saved Successfully").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

        } else {
            try {
                String sql = "UPDATE Item SET description=?,unitPrice=?,qtyOnHand=? WHERE code= ?";
                PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
                statement.setString(1, i.getDescription());
                statement.setDouble(2, i.getUnitPrice());
                statement.setDouble(3, i.getQtyOnHand());
                statement.setString(4, i.getCode());

                if (statement.executeUpdate() > 0) {
                    searchItems(searchText);
                    clearFields();
                    new Alert(Alert.AlertType.INFORMATION, "Item Updated Successfully").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private void clearFields() {
        txtCode.clear();
        txtDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
    }

    private void searchItems(String text) {
        String searchText = "%" + text + "%";
        try {
            ObservableList<ItemTM> tmList = FXCollections.observableArrayList();
            String sql = "SELECT * FROM Item WHERE description LIKE ? ";
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
            statement.setString(1, searchText);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Button btn = new Button("DELETE");
                ItemTM tm = new ItemTM(set.getString(1), set.getString(2), set.getDouble(3), set.getInt(4), btn);
                tmList.add(tm);

                btn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure that you want to delete?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get() == ButtonType.YES) {
                        try {
                            //ObservableList<CustomerTM> tmList = FXCollections.observableArrayList();
                            String sql1 = "DELETE FROM Item WHERE code=?";
                            PreparedStatement statement1 = DBConnection.getInstance().getConnection().prepareStatement(sql1);
                            statement1.setString(1, tm.getCode());
                            if (statement1.executeUpdate() > 0) {
                                searchItems(searchText);
                                new Alert(Alert.AlertType.INFORMATION, "Item Deleted Successfully").show();
                            } else {
                                new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                            }

                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            tblItem.setItems(tmList);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        }
}
