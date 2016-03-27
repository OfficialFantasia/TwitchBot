package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AddController implements Initializable{

    @FXML
    private Button cancel,add;
    @FXML
    private TextField command,output;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //UI-Elements
        cancel.setOnAction(ae -> {
            Context.getInstance().switchToCommandsTab(ae);
        });
        add.setOnAction(ae -> {
            if(!command.getText().equals("") && !output.getText().equals("")){
                if(!Context.getInstance().getCommands().isEmpty()) {
                    if (!Context.getInstance().getCommands().containsKey(command.getText())) {
                        Context.getInstance().getCommands().put(command.getText(),output.getText());
                        System.out.println("Command added!");
                    } else {
                        System.out.println("Command already used!");
                    }
                } else {
                    Context.getInstance().getCommands().put(command.getText(),output.getText());
                    System.out.println("Command added!");
                }
                Context.getInstance().switchToCommandsTab(ae);
            }
        });
    }
}
