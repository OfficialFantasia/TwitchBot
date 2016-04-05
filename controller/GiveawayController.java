package com.fantasia.controller;

import com.fantasia.Context;
import com.fantasia.TabManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class GiveawayController implements Initializable{

    @FXML
    private Button rollGiveaway,showWinner;
    @FXML
    private ListView giveawayEntries;
    @FXML
    private Label giveawayWinner;
    @FXML
    private TextField giveawayInput;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(Context.getInstance().getWinner() != null){
            giveawayWinner.setText(Context.getInstance().getWinner());
            showWinner.setVisible(true);
        }
        if(Context.getInstance().getGiveawayCommand() != null)
            giveawayInput.setText(Context.getInstance().getGiveawayCommand());
        if(!Context.getInstance().getGiveawayEntries().isEmpty())
            giveawayEntries.setItems(FXCollections.observableArrayList(Context.getInstance().getGiveawayEntries()));
        rollGiveaway.setOnAction(ae -> Platform.runLater(this::rollWinner));
        giveawayInput.textProperty().addListener(cl -> {
            if(!giveawayInput.getText().equals("")){
                Context.getInstance().setGiveawayCommand(giveawayInput.getText());
                giveawayEntries.getItems().clear();
                Context.getInstance().getGiveawayEntries().clear();
            }
        });
        showWinner.setOnAction(ae -> {
            try {
                TabManager.getInstance().getGiveaway().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/winner.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Context.getInstance().setGiveawayEntriesList(giveawayEntries);
    }

    private void rollWinner(){
        Random rand = new Random();
        if(!Context.getInstance().getGiveawayEntries().isEmpty()){
            while(true){
                Context.getInstance().setWinner(Context.getInstance().getGiveawayEntries().get(rand.nextInt(Context.getInstance().getGiveawayEntries().size())));
                System.out.println(Context.getInstance().getWinner());
                if(Context.getInstance().isOnlySubsAndMods() || !Context.getInstance().isModsCanWin()){
                    if(Context.getInstance().isOnlySubsAndMods()){
                        if(isModerator() || isSubscribed())
                            break;
                        else
                            continue;
                    }
                    if(!Context.getInstance().isModsCanWin()) {
                        if(!isModerator())
                            break;
                    }
                } else {
                    break;
                }
            }
            showWinner.setVisible(true);
            giveawayWinner.setText(Context.getInstance().getWinner());
        }
    }

    private boolean isModerator(){
        if(!Context.getInstance().getModerators().isEmpty()){
            for(String c: Context.getInstance().getModerators()){
                if(c.equals(Context.getInstance().getWinner()))
                    return true;
            }
        }
        return false;
    }

    private boolean isSubscribed(){
        if(!Context.getInstance().getSubs().isEmpty()){
            for(String c: Context.getInstance().getSubs()){
                if(c.equals(Context.getInstance().getWinner()))
                    return true;
            }
        }
        return false;
    }
}
