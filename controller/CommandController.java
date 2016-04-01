package com.fantasia.controller;

import com.fantasia.Context;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.SystemUtils;
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
import java.util.ResourceBundle;

public class CommandController implements Initializable {

    @FXML
    private Button saveCommands,deleteCommand,editCommand,addCommand,importCommands,exportCommands;
    @FXML
    private ListView commandList;

    final static protected CommandController instance = new CommandController();
    public static CommandController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                    Context.getInstance().switchTo("edit");
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        addCommand.setOnAction(ae -> {
            try{
                Context.getInstance().switchTo("add");
            } catch (Exception ex){
                ex.printStackTrace();
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

        ObservableList<String> items = FXCollections.observableArrayList(Context.getInstance().getCommands().keySet());
        commandList.setItems(items);
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
        File file = fileChooser.showOpenDialog(commandList.getScene().getWindow());
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
        File file = fileChooser.showSaveDialog(commandList.getScene().getWindow());
        if(file == null)
            return;
        StreamResult result = new StreamResult(file);
        transformer.transform(src,result);
        System.out.println("Exported Commands");
    }

    public void saveCommands() throws Exception{
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
}
