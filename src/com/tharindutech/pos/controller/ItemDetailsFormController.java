package com.tharindutech.pos.controller;

import com.tharindutech.pos.db.DBConnection;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        try {
           String sql= "SELECT itemCode,unitPrice,qty FROM `Order Details` WHERE orderId=? ";
            PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
             statement.setString(1,id);
            ResultSet set = statement.executeQuery();
            ObservableList<ItemDetailTM> tmList= FXCollections.observableArrayList();
            while (set.next()){
                double tempUnitPrice=set.getDouble(2);
                int tempQty=set.getInt(3);
                double tmpTotal=tempQty*tempUnitPrice;
                tmList.add(new ItemDetailTM(set.getString(1),set.getDouble(2),set.getInt(3),tmpTotal));
            }
            tblItems.setItems(tmList);
        } catch (Exception e){
           e.printStackTrace();
        }
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) itemDetailsContext.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/DashboardForm.fxml"))));
    }
}
