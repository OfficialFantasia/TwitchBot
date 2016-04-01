package com.fantasia.controller;

import com.fantasia.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class UpdateController implements Initializable {

    @FXML
    private Button update,cancel;
    @FXML
    private Label found,check,currentversion,newversion,newval,currval;
    @FXML
    private ImageView spinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //UI-Elements
        update.setOnAction(ae -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URL("http://twitchbot.hol.es/download.html").toURI());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            System.exit(0);
        });
        cancel.setOnAction(ae -> {
            try {
                Context.getInstance().switchTo("login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            checkForUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForUpdate() throws Exception{
        URL url = new URL("http://twitchbot.hol.es/api/v1/version.php");
        Scanner scan = new Scanner(url.openStream());
        String str = "";
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();
        JSONObject obj = new JSONObject(str);
        double version = obj.getDouble("version");
        if(version != Context.getInstance().getVERSION()){
            Platform.runLater(() -> {
                update.setVisible(true);
                cancel.setVisible(true);
                found.setVisible(true);
                check.setVisible(false);
                spinner.setVisible(false);
                newversion.setVisible(true);
                currentversion.setVisible(true);
                newval.setText(version+"");
                newval.setVisible(true);
                currval.setText(Context.getInstance().getVERSION()+"");
                currval.setVisible(true);
            });
        } else {
            Platform.runLater(() -> {
                try {
                    Context.getInstance().switchTo("login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
