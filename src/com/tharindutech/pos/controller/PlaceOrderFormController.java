package com.tharindutech.pos.controller;

import com.tharindutech.pos.db.DBConnection;
import com.tharindutech.pos.db.DataBase;
import com.tharindutech.pos.model.Item;
import com.tharindutech.pos.model.ItemDetails;
import com.tharindutech.pos.model.Order;
import com.tharindutech.pos.view.tm.CartTM;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class PlaceOrderFormController {
    public TextField txtDate;
    public ComboBox<String> cmbCustomerIds;
    public ComboBox<String> cmbItemCodes;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtSalary;
    public TextField txtDescription;
    public TextField txtUnitPrice;
    public TextField txtQtyOnHand;
    public TextField txtQty;
    public TableColumn colCode;
    public TableView tblCart;
    public TableColumn colDescription;
    public TableColumn colUnitPrice;
    public TableColumn colQty;
    public TableColumn colTotal;
    public TableColumn colOption;
    public Label lblTotal;
    public TextField txtOrderId;
    public AnchorPane placeOrderFormContext;

    public void initialize() {

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));

        setDateAndOrderId();
        loadAllCustomerIds();
        loadAllItemCodes();
        setOrderId();

        cmbCustomerIds.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (null != newValue) {
                        setCustomerDetails();
                    }
                });

        cmbItemCodes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (null != newValue) {
                        setItemDetails();
                    }
                });

    }

    private void setOrderId() {
        try {
            String sql = "SELECT orderId FROM `Order` ORDER BY orderId DESC LIMIT 1"; // 10 - not working(Unsigned)
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
            ResultSet set = statement.executeQuery();
            if (set.next()){
               // gen id
                String tempOrderId = set.getString(1);
                String array[] = tempOrderId.split("-");//[D,3]
                int tempNumber = Integer.parseInt(array[1]);
                int finalizeOrderId = tempNumber + 1;
                txtOrderId.setText("D-" + finalizeOrderId);
            } else {
               // D-001
                txtOrderId.setText("D-1");
                return;
            }

        } catch (SQLException |ClassNotFoundException e){
        }
    }

    private void setItemDetails() {
        try {
            String sql="SELECT * FROM Item WHERE code=?";
            PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
            statement.setString(1,cmbItemCodes.getValue());
            ResultSet set= statement.executeQuery();
            if(set.next()){
                txtDescription.setText(set.getString(2));
                txtQtyOnHand.setText(String.valueOf(set.getInt(3)));
                txtUnitPrice.setText(String.valueOf(set.getDouble(4)));
            }
            } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void setCustomerDetails() {
        try{
            String sql = "SELECT * FROM Customer WHERE id=?";
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement(sql);
            statement.setString(1, cmbCustomerIds.getValue());
            ResultSet set = statement.executeQuery();
            if(set.next()){
                txtName.setText(set.getString(2));
                txtAddress.setText(set.getString(3));
                txtSalary.setText(set.getString(4));
            }
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private void loadAllItemCodes() {
        try {
            String sql="SELECT code FROM Item";
            PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
            ResultSet set= statement.executeQuery();
            ArrayList<String> codeList=new ArrayList<>();

            while (set.next()){
                codeList.add(set.getString(1));
            }
            ObservableList<String>obList= FXCollections.observableArrayList(codeList);
            cmbItemCodes.setItems(obList);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadAllCustomerIds(){
        try{
            String sql="SELECT id FROM Customer";
            PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
            ResultSet set= statement.executeQuery();

            ArrayList<String> idList=new ArrayList<>();
            while(set.next()){
                idList.add(set.getString(1));
            }
            ObservableList<String> obList = FXCollections.observableArrayList(idList);
            cmbCustomerIds.setItems(obList);
        }
        catch (SQLException | ClassNotFoundException e){
         e.printStackTrace();
        }
    }

    private void setDateAndOrderId() {
        /*Date date= new Date();
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd");
         String d=df.format(date);
         txtDate.setText(d);*/
        txtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    private boolean checkQty(String code, int qty) {
         try{
             String sql="SELECT qtyOnHand from Item WHERE code=?";
          PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
          statement.setString(1,code);
          ResultSet set=statement.executeQuery();
          if(set.next()){
              if (set.getInt(1)>=qty){
                  return  true;
              } else {
                  return false;
              }
          }
          } catch (SQLException | ClassNotFoundException e){
             e.printStackTrace();
         }
        return false;
    }




    ObservableList<CartTM> obList = FXCollections.observableArrayList();
    public void addToCartOnAction(ActionEvent actionEvent) {

        if (!checkQty(cmbItemCodes.getValue(), Integer.parseInt(txtQty.getText()))) {
            new Alert(Alert.AlertType.WARNING, "Out of Stock").show();
            return;
        }

        double unitPrice = Double.parseDouble(txtUnitPrice.getText());
        int qty = Integer.parseInt(txtQty.getText());
        double total = unitPrice * qty;
        Button btn = new Button("Delete");

        int row = isAlreadyExists(cmbItemCodes.getValue());
        if (row == -1) {
            CartTM cartTM = new CartTM(cmbItemCodes.getValue(), txtDescription.getText(), unitPrice, qty, total, btn);
            obList.add(cartTM);
            tblCart.setItems(obList);
        } else {
            int tmpQty = obList.get(row).getQty() + qty;
            double tmpTotal = unitPrice * tmpQty;

            if (!checkQty(cmbItemCodes.getValue(), tmpQty)) {
                new Alert(Alert.AlertType.WARNING, "Out of Stock").show();
                return;
            }
            obList.get(row).setQty(tmpQty);
            obList.get(row).setTotal(tmpTotal);
            tblCart.refresh();
        }
        calculateTotal();
        clearFields();
        cmbItemCodes.requestFocus();


        btn.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> buttonType = alert.showAndWait();

            if (buttonType.get() == ButtonType.YES) {
                for (CartTM tm : obList) {
                    obList.remove(tm);
                    calculateTotal();
                    tblCart.refresh();
                    return;
                }
            }
        });
    }


    private int isAlreadyExists(String code) {
        for (int i = 0; i < obList.size(); i++) {
            if (obList.get(i).getCode().equals(code)) {
                return i;
            }
        }
        return -1;
    }


    public void placeOrderOnAction(ActionEvent actionEvent) throws SQLException {
        if (obList.isEmpty()) return;
        ArrayList<ItemDetails> details = new ArrayList<>();
        for (CartTM tm : obList) {
            details.add(new ItemDetails(tm.getCode(), tm.getUnitPrice(), tm.getQty()));
        }

        Order order = new Order(txtOrderId.getText(), new Date(),
                Double.parseDouble(lblTotal.getText()), cmbCustomerIds.getValue(), details);
        Connection con=null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);
            String sql="INSERT INTO `Order` VALUES (?,?,?,?)";
            PreparedStatement statement= con.prepareStatement(sql);
            statement.setString(1,order.getOrderId());
            statement.setString(2,txtDate.getText());
            statement.setDouble(3,order.getTotalCost());
            statement.setString(4,order.getCustomer());

            boolean isOrderSaved=statement.executeUpdate()>0;
           if(isOrderSaved){
               boolean isAllUpdated=manageQty(details);
               if(isAllUpdated){
                   con.commit();
                   new Alert(Alert.AlertType.CONFIRMATION,"Order Placed").show();
                   clearAll();
               } else {
                   con.setAutoCommit(true);
                   con.rollback();
                   new Alert(Alert.AlertType.WARNING,"Try Again").show();
               }
           } else {
               con.setAutoCommit(true);
               con.rollback();
                new Alert(Alert.AlertType.WARNING,"Try Again!").show();
           }
        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        } finally {
            con.setAutoCommit(true);
        }
    }

    private boolean manageQty(ArrayList<ItemDetails>details) {
        try{
            for (ItemDetails d:details) {
                String sql="INSERT INTO `Order Details` VALUES (?,?,?,?)";
                PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
                statement.setString(1,d.getCode());
                statement.setString(2,txtOrderId.getText());
                statement.setDouble(3,d.getUnitPrice());
                statement.setInt(4,d.getQty());

                boolean isOrderDetailsSaved=statement.executeUpdate()>0;

                if(isOrderDetailsSaved){
                    boolean isQtyUpdated=update(d);
                    if(!isQtyUpdated){
                        return false;
                    }
                } else {
                    return false;
                }
            }

        } catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();

        }

        return true;
    }

    private boolean update(ItemDetails d) {
     try {
         String sql="UPDATE Item SET qtyOnHand = (qtyOnHand-?) WHERE code=? " ;
         PreparedStatement statement= DBConnection.getInstance().getConnection().prepareStatement(sql);
         statement.setInt(1,d.getQty());
         statement.setString(2,d.getCode());
         return statement.executeUpdate()>0;

     } catch (SQLException | ClassNotFoundException e){
          e.printStackTrace();
          return false;
     }
    }

    private void clearAll() {
        obList.clear();
        calculateTotal();

        txtName.clear();
        txtAddress.clear();
        txtSalary.clear();

        //========
        cmbCustomerIds.setValue(null);
        //=====
        cmbItemCodes.setValue(null);

        clearFields();
        cmbCustomerIds.requestFocus();
        setOrderId();

    }

    private void calculateTotal() {
        double total = 0.0;
        for (CartTM ctm : obList) {
            total += ctm.getTotal();
        }
        lblTotal.setText(String.valueOf(total));
    }

    private void clearFields() {
        txtDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        txtQty.clear();
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) placeOrderFormContext.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/DashboardForm.fxml"))));
    }
}
