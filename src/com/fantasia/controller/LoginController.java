package com.fantasia.controller;

import com.fantasia.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LoginController implements Initializable{

    //FXML
    @FXML
    private Pane root;
    @FXML
    private Button submit;
    @FXML
    private TextField userInput;
    @FXML
    private PasswordField passInput;
    @FXML
    private Label error;
    @FXML
    private CheckBox saveLogin;
    @FXML
    private Hyperlink getPassword;

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
            loadLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //UI-Elements
        submit.setOnAction(ae -> {
            if(!userInput.getText().equals("") && !passInput.getText().equals("")){
                Context.getInstance().setUsername(userInput.getText());
                Context.getInstance().setPassword(passInput.getText());
            }
            try {
                if(!connect()){
                    error.setVisible(true);
                } else {
                    if(saveLogin.isSelected()){
                        saveLogin();
                    }
                    Context.getInstance().switchTo("channel",ae);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        getPassword.setOnAction(ae -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URL("http://twitchapps.com/tmi/").toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //check for login information
        checkLogin();
    }

    private void checkLogin(){
        Platform.runLater(() -> {
            if(Context.getInstance().isAutoLoginEnabled()){
                try {
                    if(!connect()){
                        error.setVisible(true);
                    } else {
                        Context.getInstance().switchTo("channel",root);
                    }
                } catch (Exception ex){
                    error.setVisible(true);
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean connect() throws Exception{
        Socket server = new Socket("irc.twitch.tv", 6667);
        Context.getInstance().setInputStream(new BufferedReader(new InputStreamReader(server.getInputStream())));
        Context.getInstance().setOutputStream(new BufferedWriter(new OutputStreamWriter(server.getOutputStream())));
        Context.getInstance().getOutputStream().write("PASS " + Context.getInstance().getPassword() + " \r\n");
        Context.getInstance().getOutputStream().flush();
        Context.getInstance().getOutputStream().write("NICK " + Context.getInstance().getUsername() + " \r\n");
        Context.getInstance().getOutputStream().flush();
        Context.getInstance().getOutputStream().write("USER " + Context.getInstance().getUsername() + " 8 * : Twitch Bot \r\n");
        Context.getInstance().getOutputStream().flush();

        String line;
        while((line = Context.getInstance().getInputStream().readLine()) != null){
            if(line.contains("004")){
                System.out.println("Connected to: " + Context.getInstance().getUsername());
                return true;
            } else if(line.contains("433") || line.contains("NOTICE * :")) {
                System.out.println("Couldn't connect!");
                return false;
            }
        }
        return false;
    }

    private void loadLogin() throws Exception{
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
                if(list.item(i).getNodeName().equals("autologin")){
                    NodeList l = list.item(i).getChildNodes();
                    switch(l.item(0).getTextContent()){
                        case "0":
                            Context.getInstance().setAutoLoginEnabled(false);
                            break;
                        case "1":
                            Context.getInstance().setAutoLoginEnabled(true);
                            break;
                    }
                    if(Context.getInstance().isAutoLoginEnabled()){
                        Context.getInstance().setUsername(l.item(1).getTextContent());
                        Context.getInstance().setPassword(new String(Base64.getUrlDecoder().decode(l.item(2).getTextContent()), "utf-8"));
                        saveLogin.setSelected(true);
                        userInput.setText(Context.getInstance().getUsername());
                        passInput.setText(Context.getInstance().getPassword());
                    }
                }
            }
        }
    }

    private void saveLogin() throws Exception{
        File file = new File(Context.getInstance().getOptionsFile());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        for(int i = 0;i<list.getLength();i++){
            if(list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                if(list.item(i).getNodeName().equals("autologin")){
                    NodeList l = list.item(i).getChildNodes();
                    l.item(0).setTextContent("1");
                    l.item(1).setTextContent(Context.getInstance().getUsername());
                    l.item(2).setTextContent(Base64.getUrlEncoder().encodeToString(Context.getInstance().getPassword().getBytes("utf-8")));
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
        Element general = doc.createElement("general");
        Element autosave = doc.createElement("autosave");
        autosave.appendChild(doc.createTextNode("0"));
        general.appendChild(autosave);
        Element timezone = doc.createElement("timezone");
        timezone.appendChild(doc.createTextNode("GMT"));
        general.appendChild(timezone);
        root.appendChild(general);
        Element autologin = doc.createElement("autologin");
        Element enabled = doc.createElement("enabled");
        enabled.appendChild(doc.createTextNode("0"));
        autologin.appendChild(enabled);
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode("null"));
        autologin.appendChild(name);
        Element pass = doc.createElement("pass");
        pass.appendChild(doc.createTextNode("null"));
        autologin.appendChild(pass);
        root.appendChild(autologin);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource src = new DOMSource(doc);
        StreamResult result = new StreamResult(Context.getInstance().getOptionsFile());
        transformer.transform(src,result);
    }
}
