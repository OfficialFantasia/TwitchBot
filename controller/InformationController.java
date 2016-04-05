package com.fantasia.controller;

import com.fantasia.Context;
import com.fantasia.TabManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class InformationController implements Initializable {

    @FXML
    private Button saveTitle,saveGame,preview,showMods;
    @FXML
    private TextField game,title;
    @FXML
    private Label streamStatus,viewers,infoLabel;
    @FXML
    private ImageView spinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if(Context.getInstance().getTitle() != null && Context.getInstance().getGame() != null){
            title.setText(Context.getInstance().getTitle());
            game.setText(Context.getInstance().getGame());
        }

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
                    changeInfo(true,false);
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
                    changeInfo(false,true);
                    Context.getInstance().setGame(game.getText());
                    saveGame.setVisible(false);
                    game.setPrefWidth(235);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        preview.setOnAction(ae -> {
            try {
                TabManager.getInstance().getInformation().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/preview.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        showMods.setOnAction(ae -> {
            try {
                TabManager.getInstance().getInformation().setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/mods.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //start timer for stream information
        Context.getInstance().startTimer(new TimerTask() {
            @Override
            public void run() {
                if((title.getText().equals(Context.getInstance().getTitle()) && game.getText().equals(Context.getInstance().getGame())) || Context.getInstance().getTitle() == null){
                    update();
                }
            }
        });
    }

    private void update(){
        Platform.runLater(() -> {
            spinner.setVisible(true);
            infoLabel.setVisible(true);
        });
        //streamer
        try{
            URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel() + "?oauth_token=" + Context.getInstance().getAccess_token());
            Scanner scan = new Scanner(url.openStream());
            String streamerjson = "";
            while (scan.hasNext())
                streamerjson += scan.nextLine();
            scan.close();
            JSONObject streamer = new JSONObject(streamerjson);
            Context.getInstance().setPartner(streamer.getBoolean("partner"));
            Context.getInstance().setGame(streamer.getString("game"));
            Context.getInstance().setTitle(streamer.getString("status"));
        } catch(Exception ex){
            ex.printStackTrace();
        }
        //stream
        try{
            URL url = new URL("https://api.twitch.tv/kraken/streams/" + Context.getInstance().getChannel());
            Scanner scan = new Scanner(url.openStream());
            String streamjson = "";
            while (scan.hasNext())
                streamjson += scan.nextLine();
            scan.close();
            JSONObject stream = new JSONObject(streamjson);
            if(stream.isNull("stream")){
                Context.getInstance().setLive(false);
            } else {
                Context.getInstance().setLive(true);
                JSONObject streaminfo = stream.getJSONObject("stream");
                Context.getInstance().setViewers(streaminfo.getInt("viewers"));
                Context.getInstance().setStartedAt(streaminfo.getString("created_at"));
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        //mods
        try {
            URL url = new URL("http://tmi.twitch.tv/group/user/" + Context.getInstance().getChannel() + "/chatters");
            Scanner scan = new Scanner(url.openStream());
            String chatterjson = "";
            while (scan.hasNext())
                chatterjson += scan.nextLine();
            scan.close();
            JSONObject chatters = new JSONObject(chatterjson);
            if(chatters.getInt("chatter_count") > 0){
                JSONArray mods = chatters.getJSONObject("chatters").getJSONArray("moderators");
                Context.getInstance().getModerators().clear();
                for(int i=0;i<mods.length();i++){
                    System.out.println(mods.get(i));
                    Context.getInstance().getModerators().add((String) mods.get(i));
                }
                Context.getInstance().getModerators().remove("botfantasia");
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        //subs
        try{
            URL url = new URL("https://api.twitch.tv/kraken/channels/" + Context.getInstance().getChannel() + "/subscriptions?oauth_token=" + Context.getInstance().getAccess_token());
            Scanner scan = new Scanner(url.openStream());
            String subsjson = "";
            while (scan.hasNext())
                subsjson += scan.nextLine();
            scan.close();
            JSONArray subs = new JSONObject(subsjson).getJSONArray("subscriptions");
            Context.getInstance().getSubs().clear();
            for(int i=0;i<subs.length();i++){
                JSONObject obj = (JSONObject) subs.get(i);
                String display_name = obj.getJSONObject("user").getString("display_name");
                Context.getInstance().getSubs().add(display_name);
            }
        } catch (Exception ex){
            Context.getInstance().getSubs().clear();
        }


        //ui
        if(Context.getInstance().isLive()){
            Platform.runLater(() -> {
                streamStatus.setText("Live");
                viewers.setText(Context.getInstance().getViewers());
                preview.setDisable(false);
            });
        } else {
            Platform.runLater(() -> {
                streamStatus.setText("Not live");
                viewers.setText(""+0);
                preview.setDisable(true);
            });
        }
        Platform.runLater(() -> {
            title.setText(Context.getInstance().getTitle());
            game.setText(Context.getInstance().getGame());
            if(!Context.getInstance().getModerators().isEmpty())
                showMods.setDisable(false);
            else
                showMods.setDisable(true);
            spinner.setVisible(false);
            infoLabel.setVisible(false);
        });
    }

    private void changeInfo(boolean title,boolean game) throws Exception{
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
