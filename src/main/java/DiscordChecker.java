import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DiscordChecker {
    private static final String USER_TOKEN = "NDgwNzQ0ODU2NDMyNDEwNjI0.GmEPoo.gc6R3iD7xPto0Z1dSnaK15R5up4DM999vRdi9A";
	private static boolean hasNewMessages = false;

    public static void main(String[] args) {
        try {
        	System.out.println("﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎");
			System.out.println("daaapaul (Discord):");
            checkChannelsMessages();
			if(!hasNewMessages) {
				System.out.println("No new messages!");
			}
			System.out.println("﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkChannelsMessages() throws Exception {

        JSONArray channels = new JSONArray(readHttpConnection(getHttpConnection("https://discordapp.com/api/v9/users/@me/channels")));

		for(int i = 0; i < channels.length(); i++) {
			JSONObject channel = channels.getJSONObject(i);
			String channelId = channel.getString("id");
			checkMessagesFor(channelId);
		}
    }

    private static void checkMessagesFor(String channelId) throws Exception {
        JSONArray messages = new JSONArray(readHttpConnection(getHttpConnection("https://discordapp.com/api/v9/channels/" + channelId + "/messages?limit=10")));

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

		for (int i = messages.length() - 1; i >= 0; i--) {
			JSONObject message = messages.getJSONObject(i);
			Instant messageTime = Instant.parse(message.getString("timestamp").substring(0, message.getString("timestamp").indexOf(".") + 4) + "Z");
			
			if (messageTime.isAfter(oneDayAgo)) {
				printMessageInfo(message);
			}
		}
    }

    private static void printMessageInfo(JSONObject message) {
		hasNewMessages = true;

        System.out.println("	﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎");

        System.out.println("	From " + message.getJSONObject("author").getString("username") +  ": ");
        System.out.println("	" + message.getString("content").replace("<@480744856432410624>", "@Paul"));
        
		System.out.print("	﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊\n");
    }

	private static HttpURLConnection getHttpConnection(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection apiChannelConnection = (HttpURLConnection) url.openConnection();
        apiChannelConnection.setRequestMethod("GET");
        apiChannelConnection.setRequestProperty("Authorization", USER_TOKEN);

		return apiChannelConnection;
	}

	private static String readHttpConnection(HttpURLConnection connection) throws Exception {
        BufferedReader apiChannelConnectionReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine = null;
        while ((inputLine = apiChannelConnectionReader.readLine()) != null) {
            response.append(inputLine);
        }
        apiChannelConnectionReader.close();

		return response.toString();
	}
}
