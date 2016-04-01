package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

public class ControlsController implements Initializable{

    @FXML
    private Label slowStatus,subStatus;
    @FXML
    private ToggleButton toggleSlow,toggleSub;
    @FXML
    private TextField banInput,timeoutInput,unbanInput;
    @FXML
    private Button ban,timeout,unban;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toggleSlow.setOnAction(ae -> {
            if(toggleSlow.isSelected()){
                try {
                    Context.getInstance().sendMessage("/slow");
                    slowStatus.setText(slowStatus.getText().replace("off","on"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(!toggleSlow.isSelected()){
                try {
                    Context.getInstance().sendMessage("/slowoff");
                    slowStatus.setText(slowStatus.getText().replace("on","off"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        toggleSub.setOnAction(ae -> {
            if(toggleSub.isSelected()){
                try {
                    Context.getInstance().sendMessage("/subscribers");
                    subStatus.setText(slowStatus.getText().replace("off","on"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(!toggleSub.isSelected()){
                try {
                    Context.getInstance().sendMessage("/subscribersoff");
                    subStatus.setText(slowStatus.getText().replace("on","off"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timeout.setOnAction(ae -> {
            if(!timeoutInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/timeout " + timeoutInput.getText());
                    timeoutInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ban.setOnAction(ae -> {
            if(!banInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/ban " + banInput.getText());
                    banInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        unban.setOnAction(ae -> {
            if(!unbanInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/unban " + unbanInput.getText());
                    unbanInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
