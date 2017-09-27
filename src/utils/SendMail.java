package utils;

import jmeterRun.APIResponse;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Rohit Jaryal on 02-08-2016.
 */
public class SendMail {

    private String host = "smtp.gmail.com";
    private final String port = "XYZ";
//    private final String user = "XYZ@XYZ.com";
    private final String user = "dont-reply@XYZ.com";
    private final String password = "XYZ";
    private String from = "donotreply@XYZ.com";
    ArrayList<String> toAddress = new ArrayList<String>();
    ArrayList<String> ccAddress = new ArrayList<String>();
    ArrayList<String> bccAddress = new ArrayList<String>();

    private HashMap<String, String> generalProperty = new HashMap<String, String>();

    public SendMail(String receipientAddress) {
        String[] addresses = receipientAddress.split(",");
        for (String str : addresses) {
            try {
                str = str.trim();
                if (str.contains("cc:"))
                    ccAddress.add(str.split(":")[1]+"@XYZ.com");
                else if (str.contains("bcc:"))
                    bccAddress.add(str.split(":")[1]+"@XYZ.com");
                else if (str.contains("to:"))
                    toAddress.add(str.split(":")[1]+"@XYZ.com");
                else
                    toAddress.add(str+"@XYZ.com");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMailNotification(String testResultLocation, String[] moduleReportData) throws IOException {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.user", user);
        // properties.put("mail.smtp.socketFactory.class",
        // "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.password", password);

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            try {
                for (int i = 0; i < toAddress.size(); i++) {
                    if (!toAddress.get(i).trim().isEmpty())
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress.get(i)));
                }
            } catch (Exception e) {
            }

            try {
                for (int i = 0; i < ccAddress.size(); i++) {
                    if (!ccAddress.get(i).isEmpty())
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccAddress.get(i)));
                }
            } catch (Exception e) {
            }

            try {
                for (int i = 0; i < bccAddress.size(); i++) {
                    if (!bccAddress.get(i).isEmpty())
                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccAddress.get(i)));
                }
            } catch (Exception e) {
            }

            message.setSubject("Functional test of module:" + moduleReportData[0]);

            String htmlContent = "<html><head>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n<title>Summary of Use case result(s)</title>\r\n</head>\r\n<body style=\"font-family:verdana;\">\r\n<h3><b>Summary of Use case result</b></h3>\r\n<table align=\"center\"  border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width='50%'>\r\n<tbody>\r\n<tr valign=\"top\" bgcolor=\"#2674a6\" >\r\n<th><font color=\"#ffffff\">Total UseCases</font></th>\r\n<th><font color=\"#ffffff\">Failures</font></th>\r\n<th><font color=\"#ffffff\">Success Rate</font></th>\r\n</tr>\r\n<tr><td bgcolor=\"#eeeee0\">@#TotalUseCases</td><td bgcolor=\"#eeeee0\">@#Failures</td><td bgcolor=\"#eeeee0\">@#SuccessRate</td></tr>\r\n</table>\r\n<hr>\r\n<h3><b>List of Use Case(s)</b></h3>\r\n<table align=\"center\"  border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width='100%'>\r\n<tbody>\r\n<tr valign=\"top\" bgcolor=\"#2674a6\" >\r\n<th width='5%'><font color=\"#ffffff\">S.no</font></th>\r\n<th><font color=\"#ffffff\">Use case name</font></th>\r\n<th width='10%'><font color=\"#ffffff\">Test Status</font></th>\r\n</tr>";

//            String content = readContentFromModuleReport(testResultLocation + "/" + moduleReportData[1]);
//            String content = "<html><head>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n<title>List of failed use case(s)</title>\r\n</head>\r\n<body>\r\n<h1><b>List of failed use case(s)</b></h1>\r\n<table align=\"center\"  border=\"1\" cellpadding=\"5\" cellspacing=\"2\">\r\n<tbody>\r\n<tr valign=\"top\" bgcolor=\"#2674a6\" >\r\n<th><font color=\"#ffffff\">S.no</font></th>\r\n<th><font color=\"#ffffff\">Use case name</font></th>\r\n</font>\r\n</tr>";
            int i = 1;
            int totalFailures = 0;
            for (APIResponse res : APIReportProcessing.apiResponseList) {
                if (!res.getUseCaseResult()) {
                    totalFailures++;
                    htmlContent += "<tr bgcolor=\"#eeeee0\"><td><b><font color=\"#FF0000\">" + i + "</font></b></td><td><b><font color=\"#FF0000\">" + res.getApiUseCaseName() + "</font></b></td><td><b><font color=\"#FF0000\">" + "Failed" + "</font></b></td></tr>";
                } else
                    htmlContent += "<tr bgcolor=\"#eeeee0\"><td>" + i + "</td><td>" + res.getApiUseCaseName() + "</td><td>" + "Passed" + "</td></tr>";
                i++;
            }
            htmlContent += "</tbody></table><hr><p>To view detailed test report,please login on <a href=\"http://xyz.com/\">http://xyz.com/</a> and click \"Payment Automation Test report\" link given under <b>payments_automation</b> project.</p></body></html>";

            int totalSamples = APIReportProcessing.apiResponseList.size();
            int totalPass = totalSamples - totalFailures;
            double passPercentage = (totalPass * 100) / totalSamples;
            String passPercentageData = String.format("%.2f", passPercentage) + "%";




            htmlContent = htmlContent.replaceAll("@#TotalUseCases", String.valueOf(totalSamples));
            htmlContent = htmlContent.replaceAll("@#Failures", String.valueOf(totalFailures));
            htmlContent = htmlContent.replaceAll("@#SuccessRate", passPercentageData);
            message.setContent(htmlContent, "text/html");
            Transport transport = session.getTransport("smtps");
            transport.connect(host, user, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("Sent EMail successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readContentFromModuleReport(String moduleSummaryFile) throws IOException {
        File moduleFile = new File(moduleSummaryFile);
        String summaryContent = "";
        try {
            FileInputStream summaryfis = new FileInputStream(moduleFile);
            int readCounterForModule = summaryfis.read();
            while (readCounterForModule != -1) {
                summaryContent = summaryContent + (char) readCounterForModule;
                readCounterForModule = summaryfis.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Not able to fetch content of file:" + moduleSummaryFile);
        }
        return summaryContent;
    }
}
