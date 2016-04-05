package com.fantasia.controller;

import com.fantasia.Context;
import com.fantasia.TabManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class WinnerController implements Initializable {

    @FXML
    private Button back;
    @FXML
    private ImageView winnerImage,spinner;
    @FXML
    private Label winner,subscribed,follows,followsLabel,subscribedLabel,winnerLabel,loading;

    Group group = new Group();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        back.setOnAction(ae -> {
            try {
                TabManager.getInstance().getGiveaway().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/giveaway.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Platform.runLater(() -> {
            displayData();
        });
    }

    private Image getWinnerImage(){
        try {
            URL url = new URL("https://api.twitch.tv/kraken/users/" + Context.getInstance().getWinner());
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            String imageUrl = new JSONObject(str).getString("logo");
            return new Image(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void displayData(){
        winnerImage.setImage(getWinnerImage());
        winner.setText(Context.getInstance().getWinner());
        if(isSubscribed())
            subscribed.setText("yes");
        else
            subscribed.setText("no");
        if(isFollowing())
            follows.setText("yes");
        else
            follows.setText("no");
        winner.setVisible(true);
        winnerLabel.setVisible(true);
        subscribed.setVisible(true);
        subscribedLabel.setVisible(true);
        follows.setVisible(true);
        followsLabel.setVisible(true);
        winnerImage.setVisible(true);
        spinner.setVisible(false);
        loading.setVisible(false);
    }

    private boolean isFollowing(){
        try{
            URL url = new URL("https://api.twitch.tv/kraken/users/" + Context.getInstance().getWinner() + "/follows/channels/" + Context.getInstance().getChannel());
            url.openStream();
            return true;
        } catch(Exception ex){
            return false;
        }
    }

    private boolean isSubscribed(){
        try {
            URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel() + "/subscriptions/" + Context.getInstance().getWinner() + "/?oauth_token=" + Context.getInstance().getAccess_token());
            url.openStream();
            return true;
        } catch (Exception ex){
            return false;
        }
    }
}
