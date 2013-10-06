public class foo implements IRCEvent {
    private IRCClient client;

    public static void main (String[] args) {
	new foo();
    }
    
    public foo () {
	System.out.println("foo starts");
	client = new IRCClient(this);
    }
    
    public void registrationComplete (){
	client.join("#foobar");
	client.join("#barbaz");
    }
    public void userQuit (String user) {
	System.out.println("User "+user+" has quit the server");
    }
    public void userJoin (String user, String channel) {
	System.out.println("User "+user+" joined "+channel);
    }
    public void userPart (String user, String channel) {
	System.out.println("User "+user+" parted "+channel);
    }

    public void channelJoined (String channel) {
	System.out.println("I joined channel: "+channel);
    }
    public void channelParted (String channel) {
	System.out.println("I parted channel: "+channel);
    }
    
}
