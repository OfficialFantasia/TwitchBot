package com.fantasia;

import java.util.concurrent.TimeUnit;

public class ChatListener implements Runnable {
    @Override
    public void run() {
        try {
            String line;
            while((line = Context.getInstance().getInputStream().readLine()) != null && Context.getInstance().isRunning()){
                if(line.startsWith("PING")){
                    Context.getInstance().getOutputStream().write("PONG " + line.substring(4));
                    Context.getInstance().getOutputStream().flush();
                    //get information of yourself to trigger chat listener and continue loop, otherwise next command won't be recognized
                    Context.getInstance().getOutputStream().write("WHOIS botfantasia \r\n");
                    Context.getInstance().getOutputStream().flush();
                    continue;
                }
                String nick = line.substring(line.indexOf(":") + 1,line.indexOf("!"));
                String msg = line.substring(line.indexOf("#"+Context.getInstance().getChannel()) + Context.getInstance().getChannel().length() + 3, line.length());
                onMessage(msg,nick);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void onMessage(String msg, String nick) throws Exception{
        if(checkForCommand(msg)){
            if(msg.equals("!commands")){
                String response = String.join(", ", Context.getInstance().getCommands().keySet());
                sendMessage("Available Commands: " + response);
                return;
            }
            String response = Context.getInstance().getCommands().get(msg);
            for(String c: Context.getInstance().getVars()){
                if(response.contains(c)){
                    switch(c){
                        case "%NICK%":
                            response = response.replaceAll(c,nick);
                            break;
                        case "%HOURS%":
                            response = response.replaceAll(c,""+Context.getInstance().getUptimeHours());
                            break;
                        case "%MINUTES%":
                            response = response.replaceAll(c,""+Context.getInstance().getUptimeMinutes());
                            break;
                        default:
                            break;
                    }
                }
            }
            sendMessage(response);
        }
    }

    private boolean checkForCommand(String msg){
        if(!Context.getInstance().getCommands().isEmpty()){
            if(Context.getInstance().getCommands().containsKey(msg)){
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /*public String getUptimeHours(){
        return "" + TimeUnit.MILLISECONDS.toHours(Context.getInstance().getStarttime());
    }

    public String getUptimeMinutes(){
        return "" + (TimeUnit.MILLISECONDS.toMinutes(Context.getInstance().getStarttime()) - 60 * TimeUnit.MILLISECONDS.toHours(Context.getInstance().getStarttime()));
    }*/

    public void sendMessage(String msg) throws Exception{
        Context.getInstance().getOutputStream().write("PRIVMSG #" + Context.getInstance().getChannel() + " :" + msg + " \r\n");
        Context.getInstance().getOutputStream().flush();
    }
}
