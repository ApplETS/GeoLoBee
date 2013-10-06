import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;


public class IRCClient {
    private final String SERVER = "localhost";
    private final int PORT = 6667;
    private String my_username = "myself";

    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;

    private IRCEvent ie;

    private List<Channel> myChannels = new ArrayList<Channel>();
    
    public static void main(String[] args) {
	//	new IRCClient();
    }

    public IRCClient(IRCEvent event) {
	ie = event;
	connect();
    }

    private void connect () {
	try {
	    socket = new Socket (SERVER, PORT);
	    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	    writer.write("NICK " + my_username + "\r\n");
	    writer.write("USER " + my_username + " 0 * :PIC Client\r\n");

	    writer.flush();
			
	    new Thread (new Reader ()).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void send(String data) {
	try {
	    writer.write(data + " \r\n");
	    writer.flush();
	    //System.out.println(">" + data);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void join(String channel) {
	send("JOIN " + channel);
    }

    public User parseEventSource(String source) {
	User sourceUser;
	String tempUserData[] = source.split(":")[1].split("!");
	sourceUser = new User(tempUserData[0]);
	return sourceUser;
    }

    public User updateUserList(Channel channel, String nickList) {
	String userArray[] = nickList.split(" ");
	//System.out.println(nickList);
	channel.clearUserList();
	for (int i = 0;i <= userArray.length -1; i++) {
	    channel.addUser(new User(userArray[i]));
	}
	return new User("");
    }
    public Channel findChannelByName(String channel) {
	Channel tempChannel;
	for (int i = 0; i <= myChannels.size(); i++) {
	    tempChannel = myChannels.get(i);
	    if (tempChannel.getName().equals(channel))
		return tempChannel;
	}
	return new Channel(""); //should not ever happen
    }

    
    private class Reader implements Runnable {
	public void run() {
	    String input = "";
	    String command = "";
	    try {
		while ((input = reader.readLine()) != null) {
		    //System.out.println("<"+input);
		    String arr[] = input.split(" ");
		    if (arr[0].equals("PING")) {
			send("PONG " + input.substring(5));
		    } else {
			command = arr[1];
			User source = parseEventSource(arr[0]);
			String channelSource;
			Channel channel;
			switch (command) {
			case "PRIVMSG": // message (channel&priv)
			    channelSource = arr[2];
			    channel = findChannelByName(channelSource);
			    //System.out.println("users in channel:" + channel.getUsers().size());
			    //for (int i = 0; i <= channel.getUsers().size() -1; i++)
				//System.out.println(channel.getUsers().get(i).getName());
			    break;
			case "NOTICE": // 
			    //System.out.println("notice found");
			    break;
			case "JOIN": // User or Me joins channel
			    channelSource = arr[2].split(":")[1];
			    if (source.getName().equals(my_username)) { //I just joined
				myChannels.add(new Channel(channelSource));
				ie.channelJoined(channelSource);
			    } else { //someone just joined
				channel = findChannelByName(channelSource);
				channel.addUser(source);
				//System.out.println("users in channel:" + channel.getUsers().size());
				//for (int i = 0; i <= channel.getUsers().size() -1; i++)
				    //System.out.println(channel.getUsers().get(i).getName());
				ie.userJoin(source.getName(), channelSource);
			    }
			    break;
			case "PART": // User or Me joins channel

			    if (source.getName().equals(my_username)) { //I just parted
				channelSource = arr[2].split(":")[1];
				myChannels.remove(new Channel(channelSource));
				ie.channelParted(channelSource);
			    } else { //someone just parted
				channelSource = arr[2];
				channel = findChannelByName(channelSource);
				channel.delUser(source);
				ie.userPart(source.getName(), channelSource);
				//System.out.println("Just removed " + source.getName() + " from channel " +channel.getName());
				//System.out.println("users in channel:" + channel.getUsers().size());
				//for (int i = 0; i <= channel.getUsers().size() -1; i++)
				    //System.out.println(channel.getUsers().get(i).getName());
			    }
			    break;
			case "QUIT": // User or Me joins channel
			    Channel tempChannel;
			    for (int i = 0; i <= myChannels.size() -1; i++) {
				tempChannel = myChannels.get(i);
				tempChannel.delUser(source);
			    }
			    ie.userQuit(source.getName());
			    break;
			case "001": // Registration complete
			    ie.registrationComplete();
			    break;
			case "353": // User nicks recieved upon joining
			    channelSource = arr[4];
			    channel = findChannelByName(channelSource);
			    updateUserList(channel, input.split(":")[2]);
			    //System.out.println("users in channel:" + channel.getUsers().size());
			    //for (int i = 0; i <= channel.getUsers().size() -1; i++)
				//System.out.println(channel.getUsers().get(i).getName());
			    break;
			default:
			    ////System.out.println("unimplemented:" + command);
			    break;
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public class Channel {
	private String channel;
	private List<User> userList = new ArrayList<User>();
	
	public Channel (String channelName) {
	    channel = channelName;
	}
	
	public void addUser(User user) { 
	    userList.add(user);
	}
	public void delUser(User user) {
	    User tempUser = new User("");
	    for (int i = 0; i <= userList.size() -1; i++) {
		tempUser = userList.get(i);
		if (userList.get(i).getName().equals(user.getName()))
		    userList.remove(userList.get(i));
	    }
	}
	public String getName() {
	    return channel;
	}
	public List<User> getUsers() {
	    return userList;
	}
	public void clearUserList() {
	    userList.clear();
	}
    }
    public class User {
	private String name;
	// private String host;
	// private String realname;
	public User (String userName) {
	    name = userName;
	}
	public String getName() {
	    return name;
	}
    }
}
