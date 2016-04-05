package com.fantasia.controller;

import com.fantasia.Context;
import com.fantasia.TabManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class PreviewController implements Initializable{

    @FXML
    private Button back;
    @FXML
    private ImageView preview,spinner;
    @FXML
    private Label loading;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getPreviewImage();
        back.setOnAction(ae -> {
            try {
                TabManager.getInstance().getInformation().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/information.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void getPreviewImage(){
        try {
            URL url = new URL("https://api.twitch.tv/kraken/streams/" + Context.getInstance().getChannel());
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            String imageUrl = new JSONObject(str).getJSONObject("stream").getJSONObject("preview").getString("large");
            Platform.runLater(() -> {
                this.preview.setImage(new Image(imageUrl));
                this.spinner.setVisible(false);
                this.loading.setVisible(false);
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
