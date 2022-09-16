package com.tharindutech.pos.controller;

import com.jfoenix.controls.JFXButton;
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
            boolean isSaved = DataBase.itemTable.add(i);
            if (isSaved) {
                searchItems(searchText);
                clearFields();
                new Alert(Alert.AlertType.INFORMATION, "Customer Saved Successfully").show();
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again !").show();
            }
        } else {
            for (int j = 0; j < DataBase.itemTable.size(); j++) {
                if (txtCode.getText().equalsIgnoreCase(DataBase.itemTable.get(j).getCode())) {
                    DataBase.itemTable.get(j).setDescription(txtDescription.getText());
                    DataBase.itemTable.get(j).setUnitPrice(Double.parseDouble(txtUnitPrice.getText()));
                    DataBase.itemTable.get(j).setQtyOnHand(Integer.parseInt(txtQtyOnHand.getText()));

                    searchItems(searchText);
                    new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully").show();
                    clearFields();
                }
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
        ObservableList<ItemTM> tmList = FXCollections.observableArrayList();
        for (Item i : DataBase.itemTable) {

            if (i.getDescription().contains(text)) {
                Button btn = new Button("DELETE");
                tmList.add(new ItemTM(i.getCode(), i.getDescription(), i.getUnitPrice(), i.getQtyOnHand(), btn));

                btn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure that you want to delete?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get() == ButtonType.YES) {
                        boolean isDeleted = DataBase.itemTable.remove(i);
                        if (isDeleted) {
                            searchItems(searchText);
                            new Alert(Alert.AlertType.INFORMATION, "Item Deleted Successfully").show();
                        } else {
                            new Alert(Alert.AlertType.WARNING, "Try Again !").show();
                        }
                    }
                });
            }

        }

        tblItem.setItems(tmList);
    }
}
