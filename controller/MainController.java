package com.fantasia.controller;

import com.fantasia.ChatListener;
import com.fantasia.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable{


    @FXML
    private Tab information,controls,commands,giveaway,options;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //load vars and start chat listener
        try {
            if (Context.getInstance().getCommands().isEmpty())
                loadCommands();
            loadOptions();
            if (!Context.getInstance().isRunning()) {
                Thread t = new Thread(new ChatListener());
                t.start();
            }
            information.setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/information.fxml")));
            controls.setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/controls.fxml")));
            commands.setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/commands.fxml")));
            giveaway.setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/giveaway.fxml")));
            options.setContent(FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/options.fxml")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            Context.getInstance().getStage().setOnCloseRequest(we -> {
                Context.getInstance().setRunning(false);
                try {
                    if(Context.getInstance().isAutoSaveEnabled()){
                        CommandController.getInstance().saveCommands();
                    }
                    Context.getInstance().sendMessage("/subscribersoff");
                    Context.getInstance().sendMessage("/slowoff");
                    //get information of yourself to trigger chat listener and stop thread
                    Context.getInstance().getOutputStream().write("WHOIS botfantasia \r\n");
                    Context.getInstance().getOutputStream().flush();
                    Context.getInstance().stopTimer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        Context.getInstance().setRunning(true);
    }

    private void loadOptions() throws Exception{
        File file = new File(Context.getInstance().getOptionsFile());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        for(int i = 0;i<list.getLength();i++){
            if(list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                if(list.item(i).getNodeName().equals("general")){
                    NodeList l = list.item(i).getChildNodes();
                    switch(l.item(0).getTextContent()){
                        case "1":
                            Context.getInstance().setAutoSaveEnabled(true);
                            break;
                        case "0":
                            Context.getInstance().setAutoSaveEnabled(false);
                            break;
                    }
                }
            }
        }
    }

    public void loadCommands() throws Exception{
        File file = new File(Context.getInstance().getCommandsFile());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        String name = "", output = "";
        for(int i = 0;i<list.getLength();i++){
            if(list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                NodeList l = list.item(i).getChildNodes();
                for(int j = 0;j<l.getLength();j++){
                    if(!l.item(j).getNodeName().equals("#text")){
                        Element e = (Element) l.item(j);
                        switch(e.getTagName()){
                            case "name":
                                name = e.getTextContent();
                                break;
                            case "output":
                                output = e.getTextContent();
                                break;
                            default:
                                break;
                        }
                        Context.getInstance().getCommands().put(name,output);
                    }
                }
            }
        }
    }
}
