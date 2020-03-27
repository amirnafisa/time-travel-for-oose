package com.jhuoose.timetravel.controllers;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.common.collect.ImmutableList;

import com.jhuoose.timetravel.repositories.BookingNotFoundException;
import io.javalin.http.Context;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailController {
    private MetaDataController metaDataController;
    private UsersController usersController;
    private BookingController bookingController;
    private ItineraryController itineraryController;

    private static EmailController GmailControllerInstance = null;

    private GoogleClientSecrets clientSecrets;
    private static volatile String code = "";

    private static final String TRAXO_MAIL_ID = "plans+09321cf52c98472dab0eab4798e10d5b@in.us.traxo.com";
    private static final String APPLICATION_NAME = "OOSE Time Travel";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private String last_synced_time;
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = ImmutableList.of(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_SEND);

    private EmailController(UsersController usersController, MetaDataController metaDataController) {
        this.usersController = usersController;
        this.metaDataController = metaDataController;

        this.clientSecrets = new GoogleClientSecrets();
        var details = new GoogleClientSecrets.Details();
        details.setClientId(System.getenv("GMAIL_CLIENT_ID"));
        details.setClientSecret(System.getenv("GMAIL_CLIENT_SECRET"));
        this.clientSecrets.setWeb(details);


    }

    public static EmailController getInstance(UsersController usersController, MetaDataController metaDataController) throws GeneralSecurityException, IOException {
        if (GmailControllerInstance == null) {
            GmailControllerInstance = new EmailController(usersController, metaDataController);
        }
        return GmailControllerInstance;
    }

    private String convert_time_to_epoch (String time) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return String.valueOf(df.parse(time).getTime()/1000);
    }

    private void print_gmail(Gmail service, Message message) throws IOException {
        message = service.users().messages().get("me", message.getId()).execute();
        List<MessagePartHeader> all_headers = message.getPayload().getHeaders();
        for (MessagePartHeader h: all_headers) {

            if (h.getName().compareTo("Subject") == 0) {
                System.out.println("Subject: ");
                System.out.println(h.getValue());
                System.out.println("\n");
            }
        }
        String mimeType = message.getPayload().getMimeType();
        List<MessagePart> parts = message.getPayload().getParts();
        if (mimeType.contains("alternative")) {
            String mailBody = "";
            for (MessagePart part : parts) {
                mailBody = new String(Base64.decodeBase64(part.getBody()
                        .getData().getBytes()));

            }
            System.out.println(mailBody);
        }
    }

    private void syncToTTDatabase (NetHttpTransport HTTP_TRANSPORT, Credential credential, String curUser) throws IOException, ParseException, MessagingException, SQLException, InterruptedException {
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        this.last_synced_time = this.metaDataController.getLastEmailSyncTime(curUser);
        List<Message> messages = retrieveEmail(service);
        this.metaDataController.setLastEmailSyncTime(curUser);

        for (Message message: messages) {
            //print_gmail(service, message);
            forward2Traxo(service, message, TRAXO_MAIL_ID);
        }

    }

    public void Callback(Context ctx) throws GeneralSecurityException, IOException, SQLException, ParseException, MessagingException, InterruptedException {
        code = ctx.queryParam("code");

        String curUser = this.usersController.currentUser(ctx).getLogin();
        String tokens_directory_path = TOKENS_DIRECTORY_PATH.concat("/"+curUser);

        if (code == null) {
            File tokens_file = new File (tokens_directory_path.concat("/StoredCredential"));

            if (tokens_file.delete()) {
                System.out.print("[INFO] Tokens file for user " + curUser + " is deleted!");
            } else {
                System.out.print("[INFO] Tokens file for user " + curUser + " is not deleted. Simply try again!");
            }
            ctx.html("<p>Close Window. Error Occured. Try again!</p>");
        } else {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, this.clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokens_directory_path)))
                    .setAccessType("offline")
                    .build();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(System.getenv("GMAIL_REDIRECT_URI")).execute();

            Credential credential = flow.createAndStoreCredential(response, curUser);

            syncToTTDatabase(HTTP_TRANSPORT, credential, curUser);

            ctx.html("<p>Successfully Received Verification Code. Close Window!</p>");

            ctx.status(201);
        }

    }

    public void loadCredentials(Context ctx) {
        try {
            String curUser = this.usersController.currentUser(ctx).getLogin();

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            //Get tokens directory path for the user
            String tokens_directory_path = TOKENS_DIRECTORY_PATH.concat("/" + curUser);

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, this.clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokens_directory_path)))
                    .setAccessType("offline")
                    .build();
            //load existing credential
            Credential credential = flow.loadCredential(curUser);
            if (credential != null
                    && (credential.getRefreshToken() != null ||
                    credential.getExpiresInSeconds() == null ||
                    credential.getExpiresInSeconds() > 60)) {
                syncToTTDatabase(HTTP_TRANSPORT, credential, curUser);
                ctx.status(201);
                return;
            }
            //Browse url to get auth access and receive token
            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(System.getenv("GMAIL_REDIRECT_URI"));
            ctx.json(authorizationUrl.build());
            ctx.status(200);
        } catch (Exception e){
            ctx.status(204);
        }
    }

    private List<Message> retrieveEmail(Gmail service) throws IOException, ParseException {
        //sample hardcoded query = trip
        String query = "after:"+convert_time_to_epoch(this.last_synced_time)+" label:oosetravel";
        ListMessagesResponse response = service.users().messages().list("me").setQ(query).execute();
        List<Message> messages = new ArrayList<>();
        while (response.getMessages() != null) {
            System.out.println("Debug: Atleast one message found");
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list("me")
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        return messages;
    }

    private void forward2Traxo(Gmail service, Message message, String forward_mail_address) throws IOException, MessagingException, InterruptedException {

        message = service.users().messages().get("me", message.getId()).setFormat("raw").execute();

        byte[] emailBytes = Base64.decodeBase64(message.getRaw());

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
        email.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(forward_mail_address));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);

        Message forward_message = new Message();
        forward_message.setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
        service.users().messages().send("me", forward_message).execute();
        TimeUnit.SECONDS.sleep(20);

    }
}
