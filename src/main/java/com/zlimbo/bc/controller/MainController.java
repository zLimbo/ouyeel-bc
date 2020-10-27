package com.zlimbo.bc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button myButton;

    @FXML
    private TextField myTextField;


    public void initialize(URL location, ResourceBundle resources) {

    }

    public void showDateTime(javafx.event.ActionEvent actionEvent) {
        System.out.println("myButton Clicked!");
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        String dateTimeString = dateFormat.format(now);
        myTextField.setText(dateTimeString);
    }
}
