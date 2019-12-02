package com.davidepetti.geoclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailAlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "EMAIL ALARM RECEIVED!");

        new EmailSend().execute(context);
    }

    class EmailSend extends AsyncTask<Context, Void, Void> {

        @Override
        protected Void doInBackground(Context... context) {
            sendEmail(context[0]);
            return null;
        }

    }

    private void sendEmail(Context context) {
        exportDatabase(context);

        // Recipient's email ID needs to be mentioned.
        String to = "petti.davide@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "erlichbachman61@gmail.com";

        final String username = "erlichbachman61@gmail.com";//change accordingly
        final String password = "zd3djZsM7SPge2ZA";//change accordingly

        SharedPreferences sharedPref = context.
                getSharedPreferences("com.davidepetti.geoclient.USERNAME_PREFERENCE_FILE", Context.MODE_PRIVATE);
        String name = sharedPref.getString("username", "noname");

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject(name);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("This is message body");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();

            String packageName = context.getApplicationInfo().packageName;
            String backupDBPath = String.format("debug_%s.sqlite", packageName);
            File sd = Environment.getExternalStorageDirectory();
            File backupDB = new File(sd.getAbsolutePath(), backupDBPath);

            Uri filename = Uri.fromFile(backupDB);
            FileDataSource source = new FileDataSource(filename.getPath());
            Log.v(LOG_TAG, source.getName());
            Log.v(LOG_TAG, filename.getPath());
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(source.getName());

            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully....");
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
            String dateString = format.format(currentTime);
//            try {
//                currentTime = format.parse(currentTime.toString());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            sharedPref = context.
                    getSharedPreferences("com.davidepetti.geoclient.SENDING_TIME_PREFERENCE_FILE", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("lastSending", dateString);
            editor.commit();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportDatabase(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            String packageName = context.getApplicationInfo().packageName;
            String currentDBPath = String.format("//data//%s//databases//%s",
                    packageName, "app_database");
            String backupDBPath = String.format("debug_%s.sqlite", packageName);
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
