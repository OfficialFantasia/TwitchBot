package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class OptionsController implements Initializable{

    @FXML
    private Button saveOptions,logout;
    @FXML
    public CheckBox autoSave,modwin,subandmod;
    @FXML
    private Label warning,warningmsg;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveOptions.setOnAction(ae -> {
            try {
                saveOptions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        logout.setOnAction(ae -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("Are you sure?");
            alert.setContentText("By logging out you delete your current access token and you have to login at the next start.");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                try{
                    deleteToken();
                    System.exit(0);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            } else {
                alert.close();
            }
        });

        Tooltip tip1 = new Tooltip();
        tip1.setText("When enabled the commands are saved automatically");
        autoSave.setTooltip(tip1);
        autoSave.setSelected(Context.getInstance().isAutoSaveEnabled());

        if(Context.getInstance().isOnlySubsAndMods())
            modwin.setDisable(true);
        modwin.setSelected(Context.getInstance().isModsCanWin());
        if(Context.getInstance().isOnlySubsAndMods() && Context.getInstance().getModerators().isEmpty() && Context.getInstance().getSubs().isEmpty()){
            warning.setVisible(true);
            warningmsg.setText("There are no mods or subs in the chat");
        }
        subandmod.setSelected(Context.getInstance().isOnlySubsAndMods());
        subandmod.selectedProperty().addListener(cl -> {
            if(subandmod.isSelected() && Context.getInstance().getModerators().isEmpty() && Context.getInstance().getSubs().isEmpty()){
                warning.setVisible(true);
                warningmsg.setText("There are no mods or subs in the chat");
            } else {
                warning.setVisible(false);
                warningmsg.setText("");
            }
        });
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
                }
                if(list.item(i).getNodeName().equals("giveaway")){
                    NodeList l = list.item(i).getChildNodes();
                    if(modwin.isSelected()){
                        l.item(0).setTextContent("1");
                        Context.getInstance().setModsCanWin(true);
                    } else if(!modwin.isSelected()){
                        l.item(0).setTextContent("0");
                        Context.getInstance().setModsCanWin(false);
                    }
                    if(subandmod.isSelected()){
                        l.item(1).setTextContent("1");
                        Context.getInstance().setOnlySubsAndMods(true);
                        l.item(0).setTextContent("1");
                        Context.getInstance().setModsCanWin(true);
                        modwin.setDisable(true);
                        modwin.setSelected(true);
                    } else if(!subandmod.isSelected()){
                        l.item(1).setTextContent("0");
                        Context.getInstance().setOnlySubsAndMods(false);
                        modwin.setDisable(false);
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

    private void deleteToken() throws Exception{
        File file = new File(Context.getInstance().getOptionsFile());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        org.w3c.dom.Node root = doc.getDocumentElement();
        NodeList list = root.getChildNodes();
        for(int i = 0;i<list.getLength();i++) {
            if (list.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                if (list.item(i).getNodeName().equals("access_token")) {
                    list.item(i).setTextContent("null");
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
