package com.fantasia;

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
            String arg = "";
            if(getCommand(msg).contains("%ARG%")){
                String[] temp = msg.split(" ");
                arg = temp[1];
            }
            String response = Context.getInstance().getCommands().get(getCommand(msg));
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
                        case "%ARG%":
                            response = response.replace(c,arg);
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
            String[] temp = msg.split(" ");
            if(temp.length>1){
                if(Context.getInstance().getCommands().containsKey(temp[0]+" %ARG%")){
                    return true;
                } else {
                    return false;
                }
            } else {
                if(Context.getInstance().getCommands().containsKey(temp[0])){
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private String getCommand(String msg){
        if(!Context.getInstance().getCommands().isEmpty()){
            for(String c: Context.getInstance().getCommands().keySet()){
                if(msg.contains(c.replace("%ARG%",""))){
                    return c;
                }
            }
        }
        return "";
    }

    public void sendMessage(String msg) throws Exception{
        Context.getInstance().getOutputStream().write("PRIVMSG #" + Context.getInstance().getChannel() + " :" + msg + " \r\n");
        Context.getInstance().getOutputStream().flush();
    }
}
