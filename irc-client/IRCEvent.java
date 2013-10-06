
public interface IRCEvent {
    public void registrationComplete();
    public void userQuit(String user);
    public void userJoin(String user, String channel);
    public void userPart(String user, String channel);
    public void channelJoined(String channel);
    public void channelParted(String channel);
}
