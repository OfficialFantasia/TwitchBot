package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class EditController implements Initializable {

    @FXML
    private Button cancel,save;
    @FXML
    private TextField command,output;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //UI-Elements
        cancel.setOnAction(ae -> {
            Context.getInstance().switchToCommandsTab(ae);
        });
        save.setOnAction(ae -> {
            if(!command.getText().equals("") && !output.getText().equals("")){
                Context.getInstance().getCommands().remove(command.getText());
                Context.getInstance().getCommands().put(command.getText(),output.getText());
                Context.getInstance().switchToCommandsTab(ae);
            }
        });
        //set data
        command.setText(Context.getInstance().getSelectedCommand());
        command.setDisable(true);
        output.setText(Context.getInstance().getCommands().get(Context.getInstance().getSelectedCommand()));
    }
}
