package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChannelController implements Initializable{

    //FXML
    @FXML
    private Button submitChannel;
    @FXML
    private TextField channelInput;
    @FXML
    private Label error;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //UI-Elements
        submitChannel.setOnAction(ae -> {
            if(!channelInput.getText().equals("")){
                Context.getInstance().setChannel(channelInput.getText());
                try {
                    if(!joinChannel()){
                        error.setVisible(true);
                    } else {
                        Context.getInstance().switchTo("controls",ae);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean joinChannel() throws Exception{
        Context.getInstance().getOutputStream().write("JOIN #" + Context.getInstance().getChannel() + " \r\n");
        Context.getInstance().getOutputStream().flush();
        String line;
        while((line = Context.getInstance().getInputStream().readLine()) != null){
            if(line.indexOf("366") >= 0){
                System.out.println("Joined channel: " + Context.getInstance().getChannel());
                return true;
            } else if(line.equals("")){
                System.out.println("Couldn't join channel: " + Context.getInstance().getChannel());
                return false;
            }
        }
        return false;
    }
}
