package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class GiveawayController implements Initializable{

    @FXML
    private Button rollGiveaway;
    @FXML
    private ListView giveawayEntries;
    @FXML
    private Label giveawayWinner;
    @FXML
    private TextField giveawayInput;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rollGiveaway.setOnAction(ae -> {
            int winnerNum = ThreadLocalRandom.current().nextInt(0, Context.getInstance().getGiveawayEntries().size());
            giveawayWinner.setText(Context.getInstance().getGiveawayEntries().get(winnerNum));
            String winner = Context.getInstance().getGiveawayEntries().get(winnerNum);
            if(isFollowing(winner))
                System.out.println("Follows");
            else
                System.out.println("Not following");
            if(isSubscribed(winner))
                System.out.println("Is subscribed");
            else
                System.out.println("Is not subscribed");
        });
        giveawayInput.textProperty().addListener(cl -> {
            if(!giveawayInput.getText().equals("")){
                Context.getInstance().setGiveawayCommand(giveawayInput.getText());
                giveawayEntries.getItems().clear();
                Context.getInstance().getGiveawayEntries().clear();
            }
        });
        Context.getInstance().setGiveawayEntriesList(giveawayEntries);
    }

    private boolean isFollowing(String winner){
        try{
            URL url = new URL("https://api.twitch.tv/kraken/users/" + winner + "/follows/channels/" + Context.getInstance().getChannel());
            url.openStream();
            return true;
        } catch(Exception ex){
            return false;
        }
    }

    private boolean isSubscribed(String winner){
        try {
            URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel() + "/subscriptions/" + winner + "/?oauth_token=" + Context.getInstance().getAccess_token());
            url.openStream();
            return true;
        } catch (Exception ex){
            return false;
        }
    }
}
