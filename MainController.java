package com.fantasia.controller;

import com.fantasia.ChatListener;
import com.fantasia.Context;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTimeZone;
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
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainController implements Initializable{

    @FXML
    private TabPane root;
    @FXML
    private Button saveOptions,saveCommands,deleteCommand,editCommand,addCommand,importCommands,exportCommands,ban,timeout,unban;
    @FXML
    private ListView commandList;
    @FXML
    private CheckBox autoSave,autoLogin;
    @FXML
    private ToggleButton toggleSlow,toggleSub;
    @FXML
    private Label slowStatus,subStatus,infoLabel,streamStatus,viewers;
    @FXML
    private ImageView spinner;
    @FXML
    private TextField game,title,banInput,timeoutInput,unbanInput;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //load vars and start chat listener
        try {
            if(Context.getInstance().getCommands().isEmpty())
                loadCommands();
            loadOptions();
            if (!Context.getInstance().isRunning()){
                Thread t = new Thread(new ChatListener());
                t.start();
            }
            //Context.getInstance().setStarttime(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //UI-Elements
        saveOptions.setOnAction(ae -> {
            try {
                saveOptions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        saveCommands.setOnAction(ae -> {
            try {
                saveCommands();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        deleteCommand.setOnAction(ae -> {
            try {
                Context.getInstance().getCommands().remove(commandList.getSelectionModel().getSelectedItem().toString());
                commandList.setItems(FXCollections.observableArrayList(Context.getInstance().getCommands().keySet()));
                saveCommands();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        editCommand.setOnAction(ae -> {
            if(commandList.getSelectionModel().getSelectedItem() != null){
                Context.getInstance().setSelectedCommand(commandList.getSelectionModel().getSelectedItem().toString());
                try{
                    Context.getInstance().switchTo("edit",ae);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        addCommand.setOnAction(ae -> {
            try{
                Context.getInstance().switchTo("add",ae);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
        toggleSlow.setOnAction(ae -> {
            if(toggleSlow.isSelected()){
                try {
                    Context.getInstance().sendMessage("/slow");
                    slowStatus.setText(slowStatus.getText().replace("off","on"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(!toggleSlow.isSelected()){
                try {
                    Context.getInstance().sendMessage("/slowoff");
                    slowStatus.setText(slowStatus.getText().replace("on","off"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        toggleSub.setOnAction(ae -> {
            if(toggleSub.isSelected()){
                try {
                    Context.getInstance().sendMessage("/subscribers");
                    subStatus.setText(slowStatus.getText().replace("off","on"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(!toggleSub.isSelected()){
                try {
                    Context.getInstance().sendMessage("/subscribersoff");
                    subStatus.setText(slowStatus.getText().replace("on","off"));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        exportCommands.setOnAction(ae -> {
            try {
                exportCommands();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        importCommands.setOnAction(ae -> {
            try {
                importCommands();
                saveCommands();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        timeout.setOnAction(ae -> {
            if(!timeoutInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/timeout " + timeoutInput.getText());
                    timeoutInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ban.setOnAction(ae -> {
            if(!banInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/ban " + banInput.getText());
                    banInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        unban.setOnAction(ae -> {
            if(!unbanInput.getText().equals("")){
                try {
                    Context.getInstance().sendMessage("/unban " + unbanInput.getText());
                    unbanInput.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //set tooltips
        Tooltip tip1 = new Tooltip();
        tip1.setText("When enabled the commands are saved automatically");
        autoSave.setTooltip(tip1);
        Tooltip tip2 = new Tooltip();
        tip2.setText("When enabled you login automatically with your current login information");
        autoLogin.setTooltip(tip2);
        //update options tab
        root.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            if(root.getSelectionModel().getSelectedIndex() == 3){
                autoLogin.setSelected(Context.getInstance().isAutoLoginEnabled());
                autoSave.setSelected(Context.getInstance().isAutoSaveEnabled());
            }
        });
        //Set list data
        ObservableList<String> items;
        items = FXCollections.observableArrayList(Context.getInstance().getCommands().keySet());
        commandList.setItems(items);
        //start timer for stream information
        Context.getInstance().startTimer(new TimerTask() {
            @Override
            public void run() {
                try {
                    updateStreamerInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //set closing event
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setOnCloseRequest(we -> {
                Context.getInstance().setRunning(false);
                try {
                    if(Context.getInstance().isAutoSaveEnabled()){
                        saveCommands();
                    }
                    //Context.getInstance().sendMessage("/subscribersoff");
                    //Context.getInstance().sendMessage("/slowoff");
                    //get information of yourself to trigger chat listener and stop thread
                    Context.getInstance().getOutputStream().write("WHOIS botfantasia \r\n");
                    Context.getInstance().getOutputStream().flush();
                    Context.getInstance().stopTimer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        //set running true
        Context.getInstance().setRunning(true);
    }

    private String fetchStreamerInfo() throws Exception{
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
        if(obj.isNull("stream")){
            Context.getInstance().setLive(false);
            Platform.runLater(() -> {
                streamStatus.setText("Not live");
                toggleSlow.setDisable(true);
                toggleSub.setDisable(true);
                ban.setDisable(true);
                banInput.setDisable(true);
                spinner.setVisible(false);
                infoLabel.setVisible(false);
                timeout.setDisable(true);
                timeoutInput.setDisable(true);
                unban.setDisable(true);
                unbanInput.setDisable(true);
            });
            return;
        }
        Context.getInstance().setLive(true);
        JSONObject stream = obj.getJSONObject("stream");
        Context.getInstance().setViewers(stream.getInt("viewers"));
        Context.getInstance().setStartedAt(stream.getString("created_at"));
        JSONObject channel = stream.getJSONObject("channel");
        Context.getInstance().setPartner(channel.getBoolean("partner"));
        Context.getInstance().setGame(channel.getString("game"));
        Context.getInstance().setTitle(channel.getString("status"));
        Platform.runLater(() -> {
            toggleSlow.setDisable(false);
            toggleSub.setDisable(!Context.getInstance().isPartner());
            ban.setDisable(false);
            banInput.setDisable(false);
            streamStatus.setText("Live");
            viewers.setText(Context.getInstance().getViewers());
            game.setText(Context.getInstance().getGame());
            title.setText(Context.getInstance().getTitle());
            spinner.setVisible(false);
            infoLabel.setVisible(false);
            timeout.setDisable(false);
            timeoutInput.setDisable(false);
            unban.setDisable(false);
            unbanInput.setDisable(false);
        });
    }

    public void importCommands() throws Exception{
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TwitchBot XML File", "*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.getExtensionFilters().add(extensionFilter);
        File dir = null;
        if(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX){
            dir = new File(System.getProperty("user.home"));
        } else if(SystemUtils.IS_OS_MAC_OSX){
            dir = new File(System.getProperty("user.home")+File.separator+"Documents");
        }
        fileChooser.setInitialDirectory(dir);
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if(file == null)
            return;
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
        ObservableList<String> items = FXCollections.observableArrayList(Context.getInstance().getCommands().keySet());
        commandList.setItems(items);
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

    private void exportCommands() throws Exception{
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("commands");
        doc.appendChild(root);
        for(String c: Context.getInstance().getCommands().keySet()){
            Element command = doc.createElement("command");
            Element cname = doc.createElement("name");
            cname.appendChild(doc.createTextNode(c));
            command.appendChild(cname);
            Element coutput = doc.createElement("output");
            coutput.appendChild(doc.createTextNode(Context.getInstance().getCommands().get(c)));
            command.appendChild(coutput);
            root.appendChild(command);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TwitchBot XML File","*.xml");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File dir = null;
        if(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX){
            dir = new File(System.getProperty("user.home"));
        } else if(SystemUtils.IS_OS_MAC_OSX){
            dir = new File(System.getProperty("user.home")+File.separator+"Documents");
        }
        fileChooser.setInitialDirectory(dir);
        File file = fileChooser.showSaveDialog(this.root.getScene().getWindow());
        if(file == null)
            return;
        StreamResult result = new StreamResult(file);
        transformer.transform(src,result);
        System.out.println("Exported Commands");
    }

    private void saveCommands() throws Exception{
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("commands");
        doc.appendChild(root);
        for(String c: Context.getInstance().getCommands().keySet()){
            Element command = doc.createElement("command");
            Element cname = doc.createElement("name");
            cname.appendChild(doc.createTextNode(c));
            command.appendChild(cname);
            Element coutput = doc.createElement("output");
            coutput.appendChild(doc.createTextNode(Context.getInstance().getCommands().get(c)));
            command.appendChild(coutput);
            root.appendChild(command);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getCommandsFile());
        transformer.transform(src,result);
        System.out.println("Saved Commands");
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
                if(list.item(i).getNodeName().equals("autologin")){
                    NodeList l = list.item(i).getChildNodes();
                    switch(l.item(0).getTextContent()){
                        case "1":
                            Context.getInstance().setAutoLoginEnabled(true);
                            break;
                        case "0":
                            Context.getInstance().setAutoLoginEnabled(false);
                            break;
                    }
                }
            }
        }
    }

    private void saveOptions() throws Exception{
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
                    if(autoSave.isSelected()){
                        l.item(0).setTextContent("1");
                        Context.getInstance().setAutoSaveEnabled(true);
                    } else if(!autoSave.isSelected()){
                        l.item(0).setTextContent("0");
                        Context.getInstance().setAutoSaveEnabled(false);
                    }
                } else if(list.item(i).getNodeName().equals("autologin")){
                    NodeList l = list.item(i).getChildNodes();
                    if(autoLogin.isSelected()){
                        l.item(0).setTextContent("1");
                        Context.getInstance().setAutoLoginEnabled(true);
                        l.item(1).setTextContent(Context.getInstance().getUsername());
                        l.item(2).setTextContent(Base64.getUrlEncoder().encodeToString(Context.getInstance().getPassword().getBytes("utf-8")));
                    } else if(!autoLogin.isSelected()){
                        l.item(0).setTextContent("0");
                        Context.getInstance().setAutoLoginEnabled(false);
                        l.item(1).setTextContent("null");
                        l.item(2).setTextContent("null");
                    }
                }
            }
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getOptionsFile());
        transformer.transform(src,result);
    }
}
