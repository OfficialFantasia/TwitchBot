package com.fantasia;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Context {
    private final static Context instance = new Context();

    public static Context getInstance() {
        return instance;
    }

    private String username,password,channel,selectedCommand,title,game,timezone;
    private int viewers;
    private BufferedReader inputStream;
    private BufferedWriter outputStream;
    private HashMap<String, String> commands = new HashMap<>();
    private String[] vars = {"%NICK%", "%HOURS%", "%MINUTES%"};
    private boolean autoSaveEnabled,autoLoginEnabled,running,partner,live;
    private DateTime date;
    private Timer timer;

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getChannel(){
        return channel;
    }

    public BufferedReader getInputStream() {
        return inputStream;
    }

    public BufferedWriter getOutputStream() {
        return outputStream;
    }

    public HashMap<String, String> getCommands() {
        return commands;
    }

    public String[] getVars() {
        return vars;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public boolean isAutoLoginEnabled() {
        return autoLoginEnabled;
    }

    public String getCommandsFile() throws Exception {
        return System.getProperty("user.home") + "/TwitchBotDocuments/commands.xml";
    }

    public String getOptionsFile() throws Exception {
        return System.getProperty("user.home") + "/TwitchBotDocuments/options.xml";
    }

    public String getFilePath() throws Exception {
        return System.getProperty("user.home") + "/TwitchBotDocuments/";
    }

    public long getUptimeHours(){
        return Hours.hoursBetween(date,DateTime.now()).getHours() - TimeUnit.MILLISECONDS.toHours(DateTimeZone.getDefault().getOffset(DateTime.now()));
    }

    public long getUptimeMinutes(){
        return Minutes.minutesBetween(date,DateTime.now()).getMinutes() - (60*Hours.hoursBetween(date,DateTime.now()).getHours());
    }

    public boolean isRunning() {
        return running;
    }

    public String getSelectedCommand() {
        return selectedCommand;
    }

    public boolean isLive() {
        return live;
    }

    public boolean isPartner() {
        return partner;
    }

    public String getViewers() {
        return "" + viewers;
    }

    public String getTitle() {
        return title;
    }

    public String getGame() {
        return game;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setChannel(String channel){
        this.channel = channel;
    }

    public void setInputStream(BufferedReader inputStream) {
        this.inputStream = inputStream;
    }

    public void setOutputStream(BufferedWriter outputStream) {
        this.outputStream = outputStream;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public void setAutoLoginEnabled(boolean autoLoginEnabled) {
        this.autoLoginEnabled = autoLoginEnabled;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setSelectedCommand(String selectedCommand) {
        this.selectedCommand = selectedCommand;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public void setPartner(boolean partner) {
        this.partner = partner;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setViewers(int viewers) {
        this.viewers = viewers;
    }

    public void setStartedAt(String startedAt) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        date = formatter.parseDateTime(startedAt);
    }

    //frequently used
    public void sendMessage(String msg) throws Exception{
        Context.getInstance().getOutputStream().write("PRIVMSG #" + Context.getInstance().getChannel() + " :" + msg + " \r\n");
        Context.getInstance().getOutputStream().flush();
    }

    public void switchToCommandsTab(ActionEvent ae){
        try{
            TabPane root = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/controls.fxml"));
            root.getSelectionModel().select(2);
            Stage newStage = (Stage) ((Node) ae.getSource()).getScene().getWindow();
            newStage.setScene(new Scene(root,354,227));
            newStage.show();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void switchTo(String destination,Pane root) throws IOException {
        Parent channel = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/" + destination + ".fxml"));
        Stage newStage = (Stage) root.getScene().getWindow();
        newStage.setScene(new Scene(channel,354,227));
        newStage.show();
    }

    public void switchTo(String destination,ActionEvent ae) throws IOException {
        Parent channel = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/" + destination + ".fxml"));
        Stage newStage = (Stage) ((Node) ae.getSource()).getScene().getWindow();
        newStage.setScene(new Scene(channel,354,227));
        newStage.show();
    }

    public void startTimer(TimerTask task){
        if(running){
            timer.cancel();
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(20));
        } else {
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(20));
        }
    }

    public void stopTimer(){
        timer.cancel();
    }
}