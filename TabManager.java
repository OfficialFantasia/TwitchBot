package com.fantasia;

import com.fantasia.controller.InformationController;
import com.fantasia.controller.OptionsController;
import javafx.scene.control.Tab;

public class TabManager {

    protected final static TabManager instance = new TabManager();
    public static TabManager getInstance() {
        return instance;
    }

    private Tab information,controls,commands,giveaway,options;

    public Tab getCommands() {
        return commands;
    }

    public Tab getControls() {
        return controls;
    }

    public Tab getGiveaway() {
        return giveaway;
    }

    public Tab getInformation() {
        return information;
    }

    public Tab getOptions() {
        return options;
    }

    public void setCommands(Tab commands) {
        this.commands = commands;
    }

    public void setControls(Tab controls) {
        this.controls = controls;
    }

    public void setGiveaway(Tab giveaway) {
        this.giveaway = giveaway;
    }

    public void setInformation(Tab information) {
        this.information = information;
    }

    public void setOptions(Tab options) {
        this.options = options;
    }
}
