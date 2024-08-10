import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.security.GeneralSecurityException;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Task;

public class GoogleChecker {

	private static final String APPLICATION_NAME = "GoogleChecker";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_READONLY,
		GmailScopes.GMAIL_MODIFY, 
		CalendarScopes.CALENDAR_READONLY,
		TasksScopes.TASKS_READONLY);

	private static Credential getCredentials(String email, final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		InputStream in = GoogleChecker.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(email);

		return credential;
	}

	private static Gmail getGmailService(String email) throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(email, HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}

	private static Calendar getCalendarService(String email) throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(email, HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}

	private static Tasks getTasksService(String email) throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(email, HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}

	public static void main(String[] args) {
		try {

			Gmail personalService = getGmailService("paulpeng2008@gmail.com");
			System.out.println("﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎");
			System.out.println("paulpeng2008@gmail.com: ");
			listUnreadEmails("paulpeng2008@gmail.com", personalService);
			askToReadEmails("paulpeng2008@gmail.com", personalService);
			System.out.println("﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊\n");

			System.out.println("Enter any key to continue to today's events\n");
			Scanner input = new Scanner(System.in);
			var in = input.next();

			System.out.println("﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎﹎");
			listCalendarEvents(getCalendarService("paulpeng2008@gmail.com"));
			System.out.println("﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊﹊\n");

			System.out.println("Enter any key to continue to paul.peng@student.tdsb.on.ca...\n");
			Scanner input2 = new Scanner(System.in);
			var in2 = input.next();

			goToUrl("https://mail.google.com/mail/u/paul.peng@student.tdsb.on.ca/?tab=rm&ogbl#inbox");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void listUnreadEmails(String email, Gmail service) throws IOException {

		List<Message> messageList = service.users().messages().list(email).setQ("is:unread").execute().getMessages();

		String subject = null;
		String from = null;

		if (messageList == null) {
			System.out.println("No new emails!");
			return;
		} else {
			for (Message message : messageList) {
				Message fullMessage = service.users().messages().get(email, message.getId()).execute();
				List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
				for (MessagePartHeader messageHeader : headers) {
					switch (messageHeader.getName()) {
						case "Subject":
							subject = messageHeader.getValue();
							break;
						case "From":
							from = messageHeader.getValue();
							break;
						default:
							break;
					}
				}
				System.out.println("SUBJECT: " + subject + " | FROM: " + from);
			}
		}
	}

	private static void askToReadEmails(String email, Gmail service)
			throws IOException, InterruptedException, URISyntaxException {

		List<Message> messageList = service.users().messages().list(email).setQ("is:unread").execute().getMessages();

		if (messageList == null) {
			return;
		}

		System.out.println("Mark all as read? (Y/N)");
		Scanner s = new Scanner(System.in);
		String userInput = s.nextLine();
		if (userInput.equalsIgnoreCase("y")) {
			for (Message message : messageList) {
				markAsRead(service, email, message.getId());
			}
		} else if (userInput.equalsIgnoreCase("n")) {
			goToUrl("https://mail.google.com/mail/u/" + email + "/?tab=rm&ogbl#inbox");
		} else {
			System.out.println("Invalid command.");
		}
	}

	private static void markAsRead(Gmail service, String userId, String messageId) throws IOException {
		ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList("UNREAD"));
		service.users().messages().modify(userId, messageId, mods).execute();
	}

	private static void goToUrl(String url) throws IOException, InterruptedException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				bringChromeToFront();
				Thread.sleep(500);
				desktop.browse(new URI(url));
			} else {
				System.err.println("Browsing is not supported on this desktop.");
			}
		} else {
			System.err.println("The Desktop class is not supported on this desktop.");
		}
	}

	private static void bringChromeToFront() throws IOException {
		String[] instructions = {
				"osascript",
				"-e", "tell application \"Google Chrome\" to run"
		};
		Runtime.getRuntime().exec(instructions);
	}

	private static void listCalendarEvents(Calendar service) throws IOException, GeneralSecurityException {
		LocalDate today = LocalDate.now();
		DateTime startOfDay = new DateTime(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());

		LocalDate tomorrow = today.plusDays(1);
		DateTime startOfTomorrow = new DateTime(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());

		Events todaysEvents = service.events().list("primary")
				.setTimeMin(startOfDay)
				.setTimeMax(startOfTomorrow)
				.setOrderBy("startTime")
				.setSingleEvents(true)
				.execute();

		List<Event> todaysEventsList = todaysEvents.getItems();
		if (todaysEventsList.isEmpty()) {
			System.out.println("No events today.");
		} else {
			System.out.println("Today's events:");
			for (Event event : todaysEventsList) {
				DateTime startTime = event.getStart().getDateTime();
				if (startTime == null) {
					if (event.getStart().getDate().toString().equals(today.toString())) {
						System.out.println("    - " + event.getSummary() + " (All day)");
					}
				} else {
					System.out.println("    - " + event.getSummary() + " at " + readableRfcTime(startTime));
				}
			}
		}
	}

	private static String readableRfcTime(DateTime rfcTime) {
		String readableStartTime = rfcTime.toString().substring(11, 13) + rfcTime.toString().substring(14, 16);
		int twentyFourHourTime = Integer.parseInt(readableStartTime);
		int twelveHourTime = twentyFourHourTime;

		String finalTime = null;
		String twelveHourTimeString = twelveHourTime + "";

		if (twelveHourTime >= 1300) {
			twelveHourTime -= 1200;
			twelveHourTimeString = twelveHourTime + "";
			if (twelveHourTime < 1000) {
				finalTime = twelveHourTimeString.substring(0, 1) + ":" + twelveHourTimeString.substring(1, 3) + "PM";
			} else {
				finalTime = twelveHourTimeString.substring(0, 2) + ":" + twelveHourTimeString.substring(2, 4) + "PM";
			}
		} else if (twelveHourTime < 1200) {
			if (twelveHourTime < 1000) {
				finalTime = twelveHourTimeString.substring(0, 1) + ":" + twelveHourTimeString.substring(1, 3) + "AM";
			} else {
				finalTime = twelveHourTimeString.substring(0, 2) + ":" + twelveHourTimeString.substring(2, 4) + "AM";
			}
		} else {
			finalTime = twelveHourTimeString.substring(0, 2) + ":" + twelveHourTimeString.substring(2, 4) + "PM";
		}
		return finalTime;
	}

	private static void listCalendarTasks(Tasks service) throws IOException, GeneralSecurityException {
		TaskLists result = service.tasklists().list().execute();
		List<TaskList> taskLists = result.getItems();

		LocalDate today = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String todayString = today.format(formatter);

		for (TaskList taskList : taskLists) {
			com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(taskList.getId())
					.setDueMax(todayString + "T23:59:59-04:00")
					.setShowCompleted(false)
					.execute();

			List<Task> items = tasks.getItems();

			if (items != null && !items.isEmpty()) {
				System.out.println("Today's tasks:");
				for (Task task : items) {
					System.out.println("	- " + task.getTitle());
				}
			} else {
				System.out.println("No tasks today.");
			}
		}
	}
}
