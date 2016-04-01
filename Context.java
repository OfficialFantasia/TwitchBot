package com.fantasia;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
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

    private String access_token,channel,selectedCommand,title,game,giveawayCommand;
    private int viewers;
    private BufferedReader inputStream;
    private BufferedWriter outputStream;
    private HashMap<String, String> commands = new HashMap<>();
    private List<String> giveawayEntries = new ArrayList<>();
    private String[] vars = {"%NICK%", "%HOURS%", "%MINUTES%"};
    private boolean autoSaveEnabled,running,partner,live;
    private DateTime startedAt;
    private Timer timer;
    private final double VERSION = 1.2;
    private ListView giveawayEntriesList;
    protected Stage stage;

    public String getAccess_token() {
        return access_token;
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
        if(startedAt != null)
            return Hours.hoursBetween(startedAt,DateTime.now()).getHours() - TimeUnit.MILLISECONDS.toHours(DateTimeZone.getDefault().getOffset(DateTime.now()));
        return 0;
    }

    public long getUptimeMinutes(){
        if(startedAt != null)
            return Minutes.minutesBetween(startedAt,DateTime.now()).getMinutes() - (60*Hours.hoursBetween(startedAt,DateTime.now()).getHours());
        return 0;
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

    public double getVERSION() {
        return VERSION;
    }

    public String getGiveawayCommand() {
        return giveawayCommand;
    }

    public List<String> getGiveawayEntries() {
        return giveawayEntries;
    }

    public ListView getGiveawayEntriesList() {
        return giveawayEntriesList;
    }

    public Stage getStage() {
        return stage;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
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

    public void setStartedAt(String startedAtNotParsed) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        startedAt = formatter.parseDateTime(startedAtNotParsed);
    }

    public void setGiveawayCommand(String giveawayCommand) {
        this.giveawayCommand = giveawayCommand;
    }

    public void setGiveawayEntriesList(ListView giveawayEntriesList) {
        this.giveawayEntriesList = giveawayEntriesList;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //frequently used
    public void sendMessage(String msg) throws Exception{
        outputStream.write("PRIVMSG #" + Context.getInstance().getChannel() + " :" + msg + " \r\n");
        outputStream.flush();
    }

    public void switchToCommandsTab(){
        try{
            TabPane root = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/main.fxml"));
            root.getSelectionModel().select(2);
            stage.setScene(new Scene(root,354,227));
            stage.show();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void switchTo(String destination) throws IOException {
        Parent channel = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/" + destination + ".fxml"));
        stage.setScene(new Scene(channel,354,227));
        stage.show();
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