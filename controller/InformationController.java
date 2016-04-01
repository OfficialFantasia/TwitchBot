package com.fantasia.controller;

import com.fantasia.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TimerTask;

public class InformationController implements Initializable {

    @FXML
    private Button saveTitle,saveGame;
    @FXML
    private TextField game,title;
    @FXML
    private Label streamStatus,viewers,infoLabel;
    @FXML
    private ImageView spinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        title.textProperty().addListener(cl -> {
            if(!title.getText().equals(Context.getInstance().getTitle())){
                saveTitle.setVisible(true);
                title.setPrefWidth(188);
            } else {
                saveTitle.setVisible(false);
                title.setPrefWidth(235);
            }
        });
        saveTitle.setOnAction(ae -> {
            if(!title.getText().equals("") && !title.getText().equals(Context.getInstance().getTitle())){
                try {
                    changeInfos(true,false);
                    Context.getInstance().setTitle(title.getText());
                    saveTitle.setVisible(false);
                    title.setPrefWidth(235);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        game.textProperty().addListener(cl -> {
            if(!game.getText().equals(Context.getInstance().getGame())){
                saveGame.setVisible(true);
                game.setPrefWidth(188);
            } else {
                saveGame.setVisible(false);
                game.setPrefWidth(235);
            }
        });
        saveGame.setOnAction(ae -> {
            if(!game.getText().equals("") && !game.getText().equals(Context.getInstance().getTitle())){
                try {
                    changeInfos(false,true);
                    Context.getInstance().setGame(game.getText());
                    saveGame.setVisible(false);
                    game.setPrefWidth(235);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //start timer for stream information
        Context.getInstance().startTimer(new TimerTask() {
            @Override
            public void run() {
                if((title.getText().equals(Context.getInstance().getTitle()) && game.getText().equals(Context.getInstance().getGame())) || Context.getInstance().getTitle() == null){
                    try {
                        updateStreamerInfo();
                        updateStreamInfo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String fetchStreamerInfo() throws Exception{
        URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel() + "?oauth_token=" + Context.getInstance().getAccess_token());
        Scanner scan = new Scanner(url.openStream());
        String str = "";
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();
        return str;
    }

    private String fetchStreamInfo() throws Exception{
        URL url = new URL("https://api.twitch.tv/kraken/streams/" + Context.getInstance().getChannel());
        Scanner scan = new Scanner(url.openStream());
        String str = "";
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();
        return str;
    }

    private void updateStreamerInfo() throws Exception{
        Platform.runLater(() -> {
            spinner.setVisible(true);
            infoLabel.setVisible(true);
        });
        JSONObject obj = new JSONObject(fetchStreamerInfo());
        Context.getInstance().setPartner(obj.getBoolean("partner"));
        Context.getInstance().setGame(obj.getString("game"));
        Context.getInstance().setTitle(obj.getString("status"));
        Platform.runLater(() -> {
            game.setText(Context.getInstance().getGame());
            title.setText(Context.getInstance().getTitle());
        });
    }

    private void updateStreamInfo() throws Exception{
        JSONObject obj = new JSONObject(fetchStreamInfo());
        if(obj.isNull("stream")){
            Context.getInstance().setLive(false);
            Platform.runLater(() -> {
                streamStatus.setText("Not live");
                spinner.setVisible(false);
                infoLabel.setVisible(false);
            });
            ControlsController.getInstance().setDisabled(true);
            return;
        }
        Context.getInstance().setLive(true);
        JSONObject stream = obj.getJSONObject("stream");
        Context.getInstance().setViewers(stream.getInt("viewers"));
        Context.getInstance().setStartedAt(stream.getString("created_at"));
        Platform.runLater(() -> {
            streamStatus.setText("Live");
            viewers.setText(Context.getInstance().getViewers());
            spinner.setVisible(false);
            infoLabel.setVisible(false);
        });
        ControlsController.getInstance().setDisabled(false);
    }

    private void changeInfos(boolean title,boolean game) throws Exception{
        URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel());
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestProperty("Accept","application/vnd.twitchtv.v2+json");
        httpCon.setRequestProperty("Authorization","OAuth " + Context.getInstance().getAccess_token());
        System.setProperty("http.agent", "");
        httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        //httpCon.setRequestProperty("Content-Length", "" + newTitle.getBytes().length);
        httpCon.setRequestMethod("PUT");
        httpCon.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        if(title)
            out.write("channel[status]=" + this.title.getText());
        if(game)
            out.write("channel[game]=" + this.game.getText());
        out.close();
        httpCon.getResponseCode();
    }
}
