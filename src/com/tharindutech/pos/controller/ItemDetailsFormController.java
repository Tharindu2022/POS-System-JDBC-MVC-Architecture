package com.tharindutech.pos.controller;

import com.tharindutech.pos.db.DataBase;
import com.tharindutech.pos.model.ItemDetails;
import com.tharindutech.pos.model.Order;
import com.tharindutech.pos.view.tm.ItemDetailTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ItemDetailsFormController {


    public TableView tblItems;
    public TableColumn colItemCode;
    public TableColumn colUnitPrice;
    public TableColumn colQty;
    public TableColumn colTotal;
    public AnchorPane itemDetailsContext;


    public void initialize(){
        colItemCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }



    public  void loadOrderDetails(String id){
        for (Order o: DataBase.orderTable) {
            if(o.getOrderId().equals(id)){
                ObservableList<ItemDetailTM> tmList= FXCollections.observableArrayList();
                for ( ItemDetails d:o.getItemDetails()){
                    double tempUnitPrice=d.getUnitPrice();
                    int qty=d.getQty();
                    double tmpTotal=tempUnitPrice*qty;
                    tmList.add(new ItemDetailTM(d.getCode(),d.getUnitPrice(),d.getQty(),tmpTotal));
                }
                tblItems.setItems(tmList);
                return;
            }
        }
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) itemDetailsContext.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/DashboardForm.fxml"))));
    }
}
