package com.fantasia.controller;

import com.fantasia.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LoginController implements Initializable{

    //FXML
    @FXML
    private Pane root;
    @FXML
    private WebView loginPage;
    @FXML
    private Label loading;
    @FXML
    private ImageView spinner;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Init vars
        try {
            File file;
            if(!(file = new File(Context.getInstance().getFilePath())).exists()){
                file.mkdirs();
            }
            if(!(file = new File(Context.getInstance().getCommandsFile())).exists()){
                createDefaultCommands();
            }
            if(!(file = new File(Context.getInstance().getOptionsFile())).exists()){
                createDefaultOptions();
            }
            loadToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //check for login information
        Platform.runLater(() -> {
            checkToken();
        });
    }

    private void checkToken(){
        if(!Context.getInstance().getAccess_token().equals("null")){
            try {
                if(!connect() || !joinChannel()){
                    //todo error handling
                } else {
                    Context.getInstance().switchTo("main");
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        } else {
            spinner.setVisible(false);
            loading.setVisible(false);
            Context.getInstance().getStage().setHeight(root.getPrefHeight() + 29);
            WebEngine engine = loginPage.getEngine();
            engine.load("https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=34owd8ft79nb9rsahepr3agcqvvk11k&redirect_uri=http://twitchbot.hol.es/token.php&scope=channel_subscriptions%20channel_editor%20channel_commercial%20user_read");
            engine.getLoadWorker().stateProperty().addListener(cl -> {
                String location = engine.getLocation();
                if(location.contains("#access_token")){
                    try{
                        Context.getInstance().setAccess_token(location.substring(location.indexOf("=")+1,location.indexOf("&")));
                        saveToken();
                        if(connect() && joinChannel()){
                            Context.getInstance().getStage().setHeight(227 + 29);
                            Context.getInstance().switchTo("main");
                        }
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean connect() throws Exception{
        Socket server = new Socket("irc.twitch.tv", 6667);
        Context.getInstance().setInputStream(new BufferedReader(new InputStreamReader(server.getInputStream())));
        Context.getInstance().setOutputStream(new BufferedWriter(new OutputStreamWriter(server.getOutputStream())));
        Context.getInstance().getOutputStream().write("PASS oauth:m0htof1j4l2c6e5dx8w4az87bwbnfc \r\n");
        Context.getInstance().getOutputStream().flush();
        Context.getInstance().getOutputStream().write("NICK botfantasia \r\n");
        Context.getInstance().getOutputStream().flush();
        Context.getInstance().getOutputStream().write("USER botfantasia 8 * : Twitch Bot \r\n");
        Context.getInstance().getOutputStream().flush();

        String line;
        while((line = Context.getInstance().getInputStream().readLine()) != null){
            if(line.contains("004")){
                System.out.println("Connected to: botfantasia");
                return true;
            } else if(line.contains("433") || line.contains("NOTICE * :")) {
                System.out.println("Couldn't connect!");
                return false;
            }
        }
        return false;
    }

    private boolean joinChannel() throws Exception{
        URL url = new URL("https://api.twitch.tv/kraken/user?oauth_token=" + Context.getInstance().getAccess_token());
        Scanner scan = new Scanner(url.openStream());
        String json = "";
        while (scan.hasNext()){
            json += scan.nextLine();
        }
        JSONObject obj = new JSONObject(json);
        String channel = obj.getString("display_name");
        Context.getInstance().getOutputStream().write("JOIN #" + channel + " \r\n");
        Context.getInstance().getOutputStream().flush();
        String line;
        while((line = Context.getInstance().getInputStream().readLine()) != null){
            if(line.indexOf("366") >= 0){
                System.out.println("Joined channel: " + channel);
                Context.getInstance().setChannel(channel);
                return true;
            } else if(line.equals("")){
                System.out.println("Couldn't join channel: " + channel);
                return false;
            }
        }
        return false;
    }

    private void loadToken() throws Exception{
        File file;
        file = new File(Context.getInstance().getOptionsFile());
        if(!file.exists()){
            file = new File(getClass().getResource("/com/fantasia/res/options.txml").toURI());
        }
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        for(int i = 0;i<list.getLength();i++){
            if(list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                if(list.item(i).getNodeName().equals("access_token")){
                    String token = list.item(i).getTextContent();
                    if(!token.equals("null"))
                        Context.getInstance().setAccess_token(new String(Base64.getDecoder().decode(token.getBytes())));
                    else
                        Context.getInstance().setAccess_token(token);
                }
            }
        }
    }

    private void saveToken() throws Exception{
        File file = new File(Context.getInstance().getOptionsFile());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        for(int i = 0;i<list.getLength();i++){
            if(list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                if(list.item(i).getNodeName().equals("access_token")){
                    list.item(i).setTextContent(Base64.getEncoder().encodeToString(Context.getInstance().getAccess_token().getBytes()));
                }
            }
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getOptionsFile());
        transformer.transform(src,result);
    }

    private void createDefaultCommands() throws Exception{
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("commands");
        doc.appendChild(root);
        Element command = doc.createElement("command");
        Element cname = doc.createElement("name");
        cname.appendChild(doc.createTextNode("!commands"));
        command.appendChild(cname);
        Element coutput = doc.createElement("output");
        coutput.appendChild(doc.createTextNode("null"));
        command.appendChild(coutput);
        root.appendChild(command);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getCommandsFile());
        transformer.transform(src,result);
    }

    private void createDefaultOptions() throws Exception{
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("options");
        doc.appendChild(root);
        Element version = doc.createElement("version");
        version.appendChild(doc.createTextNode("" + Context.getInstance().getVERSION()));
        root.appendChild(version);
        Element general = doc.createElement("general");
        Element autosave = doc.createElement("autosave");
        autosave.appendChild(doc.createTextNode("0"));
        general.appendChild(autosave);
        root.appendChild(general);
        Element giveaway = doc.createElement("giveaway");
        Element mods = doc.createElement("mods");
        mods.appendChild(doc.createTextNode("1"));
        giveaway.appendChild(mods);
        Element subs = doc.createElement("onlysubsandmods");
        subs.appendChild(doc.createTextNode("0"));
        giveaway.appendChild(subs);
        Element subluck = doc.createElement("subluck");
        subluck.appendChild(doc.createTextNode("0"));
        giveaway.appendChild(subluck);
        root.appendChild(giveaway);
        Element access_token = doc.createElement("access_token");
        access_token.appendChild(doc.createTextNode("null"));
        root.appendChild(access_token);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getOptionsFile());
        transformer.transform(src,result);
    }
}
