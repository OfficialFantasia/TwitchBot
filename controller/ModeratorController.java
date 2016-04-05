package com.fantasia.controller;

import com.fantasia.Context;
import com.fantasia.TabManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ModeratorController implements Initializable {

    @FXML
    private Button back;
    @FXML
    private ListView moderators,subscribers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        back.setOnAction(ae -> {
            try {
                TabManager.getInstance().getInformation().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/information.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        moderators.setItems(FXCollections.observableArrayList(Context.getInstance().getModerators()));
        subscribers.setItems(FXCollections.observableArrayList(Context.getInstance().getSubs()));
    }
}
