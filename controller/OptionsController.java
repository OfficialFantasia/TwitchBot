package com.fantasia.controller;

import com.fantasia.Context;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
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
import java.util.ResourceBundle;

public class OptionsController implements Initializable{

    @FXML
    private Button saveOptions,logout;
    @FXML
    private CheckBox autoSave;

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
            try{
                deleteToken();
                System.exit(0);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

        Tooltip tip1 = new Tooltip();
        tip1.setText("When enabled the commands are saved automatically");
        autoSave.setTooltip(tip1);
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
