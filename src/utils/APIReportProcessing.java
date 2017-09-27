package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jmeterRun.APIResponse;

public class APIReportProcessing {

    public static ArrayList<APIResponse> apiResponseList = new ArrayList<APIResponse>();
    public static int totalSamples = 0;
    public static int totalFailures = 0;
    static int passCount = 0;
    static int totalTestCount = 0;

    public static ArrayList<String> individualModuleCount = new ArrayList<String>();

    public static void generatModuleWiseReportFile(String baseXMLRawXMLFile, String baseXSLfilePath,
                                                   String baseSummaryHTMLFile, HashMap<String, String> xslGlobalParams) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Source xslDoc = new StreamSource(baseXSLfilePath);
            Source xmlDoc = new StreamSource(baseXMLRawXMLFile);
            OutputStream htmlFile = new FileOutputStream(baseSummaryHTMLFile);
            Transformer transformer = tFactory.newTransformer(xslDoc);
            setGlobalParamsInXSL(transformer, xslGlobalParams);
            transformer.transform(xmlDoc, new StreamResult(htmlFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logging.log("error", "Error while reading raw data XML file. Exception:" + e.getMessage());
        } catch (TransformerConfigurationException e) {
            Logging.log("error", "Error while reading raw data XML file. Exception:" + e.getMessage());
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            Logging.log("error", "Error while reading raw data XML file. Exception:" + e.getMessage());
        } catch (TransformerException e) {
            e.printStackTrace();
            Logging.log("error", "Error while generating summary report file. Exception:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while generating summary report file. Exception:" + e.getMessage());
        }
    }

    private static void setGlobalParamsInXSL(Transformer trasform, HashMap<String, String> xslGlobalParams) {
        Set<String> paramsKeys = xslGlobalParams.keySet();
        try {
            for (String key : paramsKeys) {
                trasform.setParameter(key, xslGlobalParams.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while setting global parameters for use with XSL. Exception:" + e.getMessage());
        }
    }

    public static void fetchAPIReportDetail(String rawXMLReportFile) {

        File rawXMLReport = null;
        try {
            rawXMLReport = new File(rawXMLReportFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(rawXMLReport);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            fetchFailedCasesResultSetOfNode(doc, "httpSample");
            fetchFailedCasesResultSetOfNode(doc, "sample");
            individualModuleCount.add(String.valueOf(passCount) + "," + String.valueOf(totalTestCount));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error in fetching up data from XML file. Exception:" + e.getMessage());
        } finally {
            try {
                rawXMLReport.delete();
            } catch (Exception e) {
                e.printStackTrace();
                Logging.log("error", "Error in deleting XML data file. Exception:" + e.getMessage());
            }
        }
    }


    public static void fetchFailedCasesResultSetOfNode(Document doc, String nodeName) {
        NodeList nList = doc.getElementsByTagName(nodeName);
        try {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                APIResponse apiResponse = new APIResponse();
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute("s").equals("false")) {
                        apiResponse.setUseCaseResult(false);
                        APIReportProcessing.totalFailures++;
                        passCount++;

                    } else
                        apiResponse.setUseCaseResult(true);

                    try {
                        apiResponse.setApiUseCaseName(eElement.getAttribute("lb"));
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch lb");
                        apiResponse.setApiUseCaseName("NA");
                    }

                    try {
                        apiResponse.setUseCaseID(eElement.getAttribute("ts"));
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch ts");
                        apiResponse.setUseCaseID("NA");
                    }


                    try {
                        apiResponse
                                .setRequestUrl(eElement.getElementsByTagName("java.net.URL").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch URL");
                        apiResponse
                                .setRequestUrl("NA");
                    }

                    try {
                        apiResponse.setRequestType(eElement.getElementsByTagName("method").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch request type.");
                        apiResponse.setRequestType("NA");
                    }

                    try {
                        apiResponse.setRequestHeader(
                                eElement.getElementsByTagName("requestHeader").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch Request Header.");
                        apiResponse.setRequestHeader(
                                "NA");
                    }

                    try {
                        apiResponse.setResponseHeader(
                                eElement.getElementsByTagName("responseHeader").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch Response Header.");
                        apiResponse.setResponseHeader(
                                "NA");
                    }

                    try {
                        apiResponse
                                .setRequestData(eElement.getElementsByTagName("queryString").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch Request data.");
                        apiResponse.setRequestData(
                                "NA");
                    }

                    try {
                        apiResponse.setResponseData(
                                eElement.getElementsByTagName("responseData").item(0).getTextContent());
                    } catch (Exception e) {
                        Logging.log("error", "Failed to fetch Response data.");
                        apiResponse.setResponseData(
                                "NA");
                    }


                    NodeList assertionList = eElement.getElementsByTagName("assertionResult");
                    ArrayList<String[]> assertion = new ArrayList<String[]>();
                    for (int assertCount = 0; assertCount < assertionList.getLength(); assertCount++) {
                        String[] assertResult = new String[4];
                        Node assertNode = assertionList.item(assertCount);
                        if (assertNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element assertNd = (Element) assertNode;
                            String assertName = "";
                            try {
                                assertName = assertNd.getElementsByTagName("name").item(0).getTextContent();
                            } catch (Exception e) {
                                e.printStackTrace();
                                assertName = "";
                            }

                            String assertFailure = "";
                            try {
                                assertFailure = assertNd.getElementsByTagName("failure").item(0).getTextContent();
                            } catch (Exception e) {
                                e.printStackTrace();
                                assertFailure = "";
                            }

                            String assertError = "";
                            try {
                                assertError = assertNd.getElementsByTagName("error").item(0).getTextContent();
                            } catch (Exception e) {
                                e.printStackTrace();
                                assertError = "";
                            }

                            String assertFailureMsg = "";
                            try {
                                assertFailureMsg = assertNd.getElementsByTagName("failureMessage").item(0)
                                        .getTextContent();
                            } catch (Exception e) {
                                e.printStackTrace();
                                assertFailureMsg = "";
                            }
                            assertResult[0] = assertName;
                            assertResult[1] = assertFailure;
                            assertResult[2] = assertError;
                            assertResult[3] = assertFailureMsg;
                            assertion.add(assertResult);
                        }
                    }
                    apiResponse.setAssertions(assertion);
                    apiResponseList.add(apiResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while fetching failure data. Exception:" + e.getMessage());
        }
        APIReportProcessing.totalSamples += nList.getLength();

        totalTestCount += nList.getLength();


        System.out.println("Total Samples:" + APIReportProcessing.totalSamples);
        System.out.println("Total Failures:" + APIReportProcessing.totalFailures);
    }

    public static void generateDetailedAPIReport(String resultFolder, String moduleName, String reportDateTime)
            throws IOException {
        String htmlTemplate = "<html><head>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n<title>Failed UseCase report</title>\r\n<style type=\"text/css\">\r\n\r\n\r\n\t\t\t\tbody {\r\n\t\t\t\t\tfont:normal 68% verdana,arial,helvetica;\r\n\t\t\t\t\tcolor:#000000;\r\n\t\t\t\t}\r\n\t\t\t\ttable tr td, table tr th {\r\n\t\t\t\t\tfont-size: 68%;\r\n\t\t\t\t}\r\n\t\t\t\ttable.details tr th{\r\n\t\t\t\t    color: #ffffff;\r\n\t\t\t\t\tfont-weight: bold;\r\n\t\t\t\t\ttext-align:center;\r\n\t\t\t\t\tbackground:#2674a6;\r\n\t\t\t\t\twhite-space: nowrap;\r\n\t\t\t\t}\r\n\t\t\t\ttable.details tr td{\r\n\t\t\t\t\tbackground:#eeeee0;\r\n\t\t\t\t\t/*white-space: nowrap;*/\r\n\t\t\t\t}\r\n\t\t\t\th1 {\r\n\t\t\t\t\tmargin: 0px 0px 5px; font: 165% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\th2 {\r\n\t\t\t\t\tmargin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\th3 {\r\n\t\t\t\t\tmargin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\t.Failure {\r\n\t\t\t\t\tfont-weight:bold; color:red;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\r\n\t\t\t\timg\r\n\t\t\t\t{\r\n\t\t\t\t  border-width: 0px;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\t\t\t.expand_link\r\n\t\t\t\t{\r\n\t\t\t\t   position=absolute;\r\n\t\t\t\t   right: 0px;\r\n\t\t\t\t   width: 27px;\r\n\t\t\t\t   top: 1px;\r\n\t\t\t\t   height: 27px;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\t\t\t.page_details\r\n\t\t\t\t{\r\n\t\t\t\t   display: none;\r\n\t\t\t\t}\r\n                                \r\n                                .page_details_expanded\r\n                                {\r\n                                    display: block;\r\n                                    display/* hide this definition from  IE5/6 */: table-row;\r\n                                }\r\n\r\n\r\n\t\t\t</style>\r\n<script language=\"JavaScript\">\r\n                           function expand(details_id)\r\n\t\t\t   {\r\n\t\t\t      \r\n\t\t\t      document.getElementById(details_id).className = \"page_details_expanded\";\r\n\t\t\t   }\r\n\t\t\t   \r\n\t\t\t   function collapse(details_id)\r\n\t\t\t   {\r\n\t\t\t      \r\n\t\t\t      document.getElementById(details_id).className = \"page_details\";\r\n\t\t\t   }\r\n\t\t\t   \r\n\t\t\t   function change(details_id)\r\n\t\t\t   {\r\n\t\t\t      if(document.getElementById(details_id+\"_image\").src.match(\"expand\"))\r\n\t\t\t      {\r\n\t\t\t         document.getElementById(details_id+\"_image\").src = \"collapse.png\";\r\n\t\t\t         expand(details_id);\r\n\t\t\t      }\r\n\t\t\t      else\r\n\t\t\t      {\r\n\t\t\t         document.getElementById(details_id+\"_image\").src = \"expand.png\";\r\n\t\t\t         collapse(details_id);\r\n\t\t\t      } \r\n                           }\r\n\t\t\t</script>\r\n</head>\r\n<body>\r\n<h1><b>Use Case report</b></h1>\r\n<br/>\r\n<table width=\"100%\">\r\n<tbody><tr>\r\n<td align=\"left\">Date report:@#Date_Report </td></tr>\r\n</tbody></table>\r\n<hr size=\"1\">\r\n<h3><b>Use case:</b>@#Use_Case_Name</h3>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<h3>Detailed Information:</h3>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Request URL</th>\r\n<th>Request Type</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n<td>@#Request_URL</td>\r\n<td>@#Request_Type</td>\r\n</tr>\r\n</table>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<br/>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Request Header</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n<td>@#Request_Header</td>\r\n</tr>\r\n</table>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<br/>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Request Data</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n<td>@#Request_Data</td>\r\n</tr>\r\n</table>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<br/>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Response Header</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n<td>@#Response_Header</td>\r\n</tr>\r\n</table>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<br/>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Response Data</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n<td>@#Response_Data\r\n</td>\r\n</tr>\r\n</table>\r\n<hr size=\"1\" width=\"95%\" align=\"center\">\r\n<br/>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th>Assertions: Name</th>\r\n<th>Failure</th>\r\n<th>Error</th>\r\n<th>Failure Message</th>\r\n</tr>\r\n<tr valign=\"top\" class=\"\">\r\n@#Assertions</tr>\r\n</table>\r\n</body></html>";
        try {
            FileOutputStream htmlfile;
            PrintStream printhtml;
            for (APIResponse apiResponse : apiResponseList) {
                String htmlContent = htmlTemplate;
/*                if (apiResponse.getUseCaseResult())
                    continue;*/

                htmlContent = htmlContent.replaceAll("@#Date_Report", reportDateTime);
                try {
                    htmlContent = htmlContent.replaceAll("@#Use_Case_Name", Matcher.quoteReplacement(apiResponse.getApiUseCaseName()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Use_Case_Name", "NA");
                }
                try {
                    htmlContent = htmlContent.replaceAll("@#Request_URL", Matcher.quoteReplacement(apiResponse.getRequestUrl()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Request_URL", "NA");
                }

                try {
                    htmlContent = htmlContent.replaceAll("@#Request_Type", Matcher.quoteReplacement(apiResponse.getRequestType()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Request_Type", "NA");
                }

                try {
                    htmlContent = htmlContent.replaceAll("@#Request_Header",
                            Matcher.quoteReplacement(apiResponse.getRequestHeader()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Request_Header",
                            "NA");
                }

                try {
                    htmlContent = htmlContent.replaceAll("@#Request_Data",
                            Matcher.quoteReplacement(apiResponse.getRequestData()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Request_Data",
                            "NA");
                }

                try {
                    htmlContent = htmlContent.replaceAll("@#Response_Header",
                            Matcher.quoteReplacement(apiResponse.getResponseHeader()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Response_Header",
                            "NA");
                }

                try {
                    htmlContent = htmlContent.replaceAll("@#Response_Data",
                            Matcher.quoteReplacement(apiResponse.getResponseData()));
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Response_Data",
                            "NA");
                }

                String assertInfo = "";
                try {
                    for (String[] assertData : apiResponse.getAssertions()) {
                        assertInfo += "<tr><td>" + assertData[0] + "</td><td >" + assertData[1] + "</td><td>"
                                + assertData[2] + "</td><td>" + assertData[3] + "</td></tr>";
                    }
                    if (assertInfo.equals("")) {
                        assertInfo = "<tr><td >NA</td><td>NA</td><td>NA</td><td>NA</td></tr>";
                    }
                    assertInfo = Matcher.quoteReplacement(assertInfo);
                    htmlContent = htmlContent.replaceAll("@#Assertions", assertInfo);
                } catch (Exception e) {
                    htmlContent = htmlContent.replaceAll("@#Assertions", "NA");
                }
                File file = new File(resultFolder + apiResponse.getUseCaseID() + ".html");
                if (!file.exists())
                    file.createNewFile();
                htmlfile = new FileOutputStream(file, false);
                printhtml = new PrintStream(htmlfile);
                printhtml.println(htmlContent);
                printhtml.close();
                htmlfile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while creating failure report. Exception:" + e.getMessage());
        }
    }

    public static String updateJMXFile(String moduleName, String resulFolder) throws Exception {
        Element root = null;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        File file = new File(resulFolder + moduleName + ".jmx");
        String downloadLocation = "";
        try {
            if (file.exists()) {
                downloadLocation = resulFolder + moduleName + ".xml";
                doc = docBuilder.parse(file);
                Element root1 = doc.getDocumentElement();
                NodeList ndList = root1.getChildNodes();
                System.out.println(ndList.getLength());
                for (int i = 0; i < ndList.getLength(); i++) {
                    System.out.println(ndList.item(i).getNodeName());
                    if (ndList.item(i).getNodeName().equals("hashTree"))
                        root = (Element) ndList.item(i);
                }
            } else {
                System.out.println("JMX file does not exist at location:" + file.getAbsolutePath());
                return "";
            }
            Element child = doc.createElement("ResultCollector");
            child.setAttribute("guiclass", "SimpleDataWriter");
            child.setAttribute("testclass", "ResultCollector");
            child.setAttribute("testname", "Simple_Data_Writer");
            child.setAttribute("enabled", "true");
            root.appendChild(child);

            Element boolPropChild = doc.createElement("boolProp");
            boolPropChild.setAttribute("name", "ResultCollector.error_logging");
            boolPropChild.setTextContent("false");
            child.appendChild(boolPropChild);

            Element objPropChild = doc.createElement("objProp");
            child.appendChild(objPropChild);
            addTextNodeToParent("name", "saveConfig", objPropChild, doc);
            Element valueChild = doc.createElement("value");
            valueChild.setAttribute("class", "SampleSaveConfiguration");
            objPropChild.appendChild(valueChild);
            addTextNodeToParent("time", "true", valueChild, doc);
            addTextNodeToParent("latency", "true", valueChild, doc);
            addTextNodeToParent("timestamp", "true", valueChild, doc);
            addTextNodeToParent("success", "true", valueChild, doc);
            addTextNodeToParent("label", "true", valueChild, doc);
            addTextNodeToParent("code", "true", valueChild, doc);
            addTextNodeToParent("message", "true", valueChild, doc);
            addTextNodeToParent("threadName", "true", valueChild, doc);
            addTextNodeToParent("dataType", "true", valueChild, doc);
            addTextNodeToParent("encoding", "true", valueChild, doc);
            addTextNodeToParent("assertions", "true", valueChild, doc);
            addTextNodeToParent("subresults", "true", valueChild, doc);
            addTextNodeToParent("responseData", "true", valueChild, doc);
            addTextNodeToParent("samplerData", "true", valueChild, doc);
            addTextNodeToParent("xml", "true", valueChild, doc);
            addTextNodeToParent("fieldNames", "true", valueChild, doc);
            addTextNodeToParent("responseHeaders", "true", valueChild, doc);
            addTextNodeToParent("requestHeaders", "true", valueChild, doc);
            addTextNodeToParent("responseDataOnError", "true", valueChild, doc);
            addTextNodeToParent("saveAssertionResultsFailureMessage", "true", valueChild, doc);
            addTextNodeToParent("assertionsResultsToSave", "0", valueChild, doc);
            addTextNodeToParent("bytes", "true", valueChild, doc);
            addTextNodeToParent("url", "true", valueChild, doc);
            // addTextNodeToParent("filename", "true", valueChild, doc);
            addTextNodeToParent("hostname", "true", valueChild, doc);
            addTextNodeToParent("threadCounts", "true", valueChild, doc);
            addTextNodeToParent("sampleCount", "true", valueChild, doc);
            addTextNodeToParent("idleTime", "true", valueChild, doc);
            Element fileLocation = doc.createElement("stringProp");
            fileLocation.setAttribute("name", "filename");
            fileLocation.setTextContent(downloadLocation);
            child.appendChild(fileLocation);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = sw.toString();
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(xmlString);
            bw.flush();
            bw.close();
            fw.close();
            sw.close();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadLocation;
    }

    private static void addTextNodeToParent(String nodeName, String nodeValue, Element parentNode, Document doc) {
        Element textNode = doc.createElement(nodeName);
        textNode.setTextContent(nodeValue);
        parentNode.appendChild(textNode);
    }

    public static void generateSummaryReportFile(String summaryReportFilePath, ArrayList<String[]> moduleReportData, String successRate)
            throws IOException {
        PrintWriter pwSummary = null;
        FileOutputStream fosSummary = null;
        try {
            String summaryHeader = "<html><head>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n<title>Jmeter Summary Report</title>\r\n<style type=\"text/css\">\r\n\t\t\t\tbody {\r\n\t\t\t\t\tfont:normal 68% verdana,arial,helvetica;\r\n\t\t\t\t\tcolor:#000000;\r\n\t\t\t\t}\r\n\t\t\t\ttable tr td, table tr th {\r\n\t\t\t\t\tfont-size: 68%;\r\n\t\t\t\t}\r\n\t\t\t\ttable.details tr th{\r\n\t\t\t\t    color: #ffffff;\r\n\t\t\t\t\tfont-weight: bold;\r\n\t\t\t\t\ttext-align:center;\r\n\t\t\t\t\tbackground:#2674a6;\r\n\t\t\t\t\twhite-space: nowrap;\r\n\t\t\t\t}\r\n\t\t\t\ttable.details tr td{\r\n\t\t\t\t\tbackground:#eeeee0;\r\n\t\t\t\t\t/*white-space: nowrap;*/\r\n\t\t\t\t}\r\n\t\t\t\th1 {\r\n\t\t\t\t\tmargin: 0px 0px 5px; font: 165% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\th2 {\r\n\t\t\t\t\tmargin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\th3 {\r\n\t\t\t\t\tmargin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica\r\n\t\t\t\t}\r\n\t\t\t\t.Failure {\r\n\t\t\t\t\tfont-weight:bold; color:red;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\r\n\t\t\t\timg\r\n\t\t\t\t{\r\n\t\t\t\t  border-width: 0px;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\t\t\t.expand_link\r\n\t\t\t\t{\r\n\t\t\t\t   position=absolute;\r\n\t\t\t\t   right: 0px;\r\n\t\t\t\t   width: 27px;\r\n\t\t\t\t   top: 1px;\r\n\t\t\t\t   height: 27px;\r\n\t\t\t\t}\r\n\t\t\t\t\r\n\t\t\t\t.page_details\r\n\t\t\t\t{\r\n\t\t\t\t   display: none;\r\n\t\t\t\t}\r\n                                \r\n                                .page_details_expanded\r\n                                {\r\n                                    display: block;\r\n                                    display/* hide this definition from  IE5/6 */: table-row;\r\n                                }\r\n\r\n\r\n\t\t\t</style>\r\n</head>\r\n<body>\r\n<h1><b>Jmeter Summary Report</b></h1>\r\n<br>\r\n<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">\r\n<tbody><tr valign=\"top\">\r\n<th width=\"11%\">DateTime</th>\r\n<th>Modules</th>\r\n<th width=\"8%\">Pass (%)</th>\r\n</tr>\r\n</tbody>";
            String totalLinks = "";
            int totalPass = totalSamples - APIReportProcessing.totalFailures;
            double passPercentage = (totalPass * 100) / totalSamples;


            for (String[] module : moduleReportData) {
                if (module[2].equalsIgnoreCase("true"))

                    if (passPercentage > Integer.valueOf(successRate))
                        totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"#228B22\" face=\"Tahoma\">" + module[0]
                                + "</font></b></a>&nbsp;&nbsp;&nbsp;";
                    else
                        totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"Red\" face=\"Tahoma\">" + module[0]
                                + "</font></b></a>&nbsp;&nbsp;&nbsp;";
                else
                    totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"#228B22\" face=\"Tahoma\">" + module[0]
                            + "</font></b></a>&nbsp;&nbsp;&nbsp;";
            }

            totalLinks += "&nbsp;&nbsp;&nbsp;&nbsp;<b>URL:" + System.getProperty("baseUrl") + "</b>";

            String passPercentageData = totalPass + "/" + totalSamples + " (" + String.format("%.2f", passPercentage) + "%)";
            System.out.println("Pass percentage data:" + passPercentageData);
            String newDataEntry = "<tr><td>" + Helper.getCurrentDateInReadableFormat() + "</td><td>" + totalLinks
                    + "</td><td>" + passPercentageData + "</td></tr>";
            File summaryfile = new File(summaryReportFilePath + "/" + "jmetersummaryreport.html");
            if (!summaryfile.exists()) {
                summaryfile.createNewFile();
                fosSummary = new FileOutputStream(summaryfile, true);
                pwSummary = new PrintWriter(fosSummary);
                pwSummary.println(summaryHeader);
                pwSummary.flush();
                pwSummary.close();
                fosSummary.close();
                System.out.println("Adding content to HTML file. This will occur when a blank file is present. ");
                Logging.log("info", "Creating new summary file.");
            }
            FileInputStream summaryfis = new FileInputStream(summaryfile);
            String summaryContent = "";
            int readCounterForModule = summaryfis.read();
            while (readCounterForModule != -1) {
                summaryContent = summaryContent + (char) readCounterForModule;
                readCounterForModule = summaryfis.read();
            }
            summaryContent = summaryContent.replaceAll(Pattern.quote(summaryHeader), summaryHeader + newDataEntry);
            summaryfis.close();
            summaryfile.delete();
            fosSummary = new FileOutputStream(new File(summaryReportFilePath + "/" + "jmetersummaryreport.html"), true);
            pwSummary = new PrintWriter(fosSummary);
            summaryContent = Matcher.quoteReplacement(summaryContent);
            pwSummary.println(summaryContent);
            System.out.println(
                    "Summary report generated at :::::::" + summaryReportFilePath + "/" + "jmetersummaryreport.html");
            Logging.log("info",
                    "Summary report generated at :::::::" + summaryReportFilePath + "/" + "jmetersummaryreport.html");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pwSummary != null)
                    pwSummary.close();
                if (fosSummary != null)
                    fosSummary.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Logging.log("error", "Error in generating Summary File. Exception:" + ex.getMessage());
            }
        }
    }


    public static void generateSummaryReportFile_New(String summaryReportFilePath, ArrayList<String[]> moduleReportData, String successRate)
            throws IOException {
        // Reading Dashboard file content
        String dashboardContent = "";
        FileInputStream dashboardFis = null;
        try {
            dashboardFis = new FileInputStream(System.getProperty("Resources") + "/Configuration/" + "dashboard.html");
            int readCounterForModule = dashboardFis.read();
            while (readCounterForModule != -1) {
                dashboardContent = dashboardContent + (char) readCounterForModule;
                readCounterForModule = dashboardFis.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error in reading Dashboard HTML file. Exception:" + e.getMessage());
        } finally {
            if (dashboardFis != null)
                dashboardFis.close();
        }

        ArrayList<String> reportData = new ArrayList<String>();
        String totalLinks = "";
        int totalPass = totalSamples - APIReportProcessing.totalFailures;
        double passPercentage = (totalPass * 100) / totalSamples;
        int cnt = 0;
        for (String[] module : moduleReportData) {
            if (module[2].equalsIgnoreCase("true")) {
                String[] moduleStatistic = APIReportProcessing.individualModuleCount.get(cnt).split(",");
                int individualModulePassCount = ((Integer.valueOf(moduleStatistic[1]) - Integer.valueOf(moduleStatistic[0])) * 100) / Integer.valueOf(moduleStatistic[1]);
                if (individualModulePassCount >= Integer.valueOf(successRate))
                    totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"#228B22\" face=\"Tahoma\">" + module[0]
                            + "</font></b></a>&nbsp;&nbsp;&nbsp;";
                else
                    totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"Red\" face=\"Tahoma\">" + module[0]
                            + "</font></b></a>&nbsp;&nbsp;&nbsp;";
            } else
                totalLinks += "<a href=\"" + module[1] + "\"><b><font color=\"#228B22\" face=\"Tahoma\">" + module[0]
                        + "</font></b></a>&nbsp;&nbsp;&nbsp;";
            cnt++;
        }

        // Adding last execution report record
        String passPercentageData = totalPass + "/" + totalSamples + " (" + String.format("%.2f", passPercentage) + "%)";
        reportData.add("<tr><td>" + Helper.getCurrentDateInReadableFormat() + "</td><td>" + totalLinks + "</td><td>" + System.getProperty("baseUrl") + "</td><td>" + passPercentageData + "</td></tr>");


        File reportDataFile = new File(summaryReportFilePath + "/" + "reportData.txt");
        if (reportDataFile.exists())
            try (BufferedReader br = new BufferedReader(new FileReader(reportDataFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    reportData.add(line);
                }
            }

        String tableContainerContent = "";
        if (reportData != null && reportData.size() > 0) {
            try {
                reportDataFile.delete();
            } catch (Exception e) {
                Logging.log("error", "Error in deleting report data file. Exception:" + e.getMessage());
            }


            FileWriter fw = null;
            try {
                fw = new FileWriter(summaryReportFilePath + "/" + "reportData.txt");
                for (String str : reportData) {
                    fw.write(str + "\r\n");
                    tableContainerContent += str;
                }
                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Logging.log("error", "Error in writing data to report data file. Exception:" + ex.getMessage());
            } finally {
                if (fw != null)
                    fw.close();
            }
        }


        dashboardContent = dashboardContent.replaceAll("@previousRunList", Matcher.quoteReplacement(tableContainerContent));
        dashboardContent = dashboardContent.replaceAll("@executionDate", Matcher.quoteReplacement(Helper.getCurrentDateInReadableFormat()));
        dashboardContent = dashboardContent.replaceAll("@totalTestCases", String.valueOf(totalSamples));
        dashboardContent = dashboardContent.replaceAll("@totalPassed", String.valueOf(totalPass));
        dashboardContent = dashboardContent.replaceAll("@totalFailed", String.valueOf(APIReportProcessing.totalFailures));
        dashboardContent = dashboardContent.replaceAll("@totalSkipped", "");
        dashboardContent = dashboardContent.replaceAll("@lastRunNumber", Matcher.quoteReplacement(String.valueOf(reportData.size())));
        dashboardContent = dashboardContent.replaceAll("@passPercentage", String.valueOf(passPercentage));
        dashboardContent = dashboardContent.replaceAll("@failPercentage", String.valueOf(100 - passPercentage));


        if (new File(summaryReportFilePath + "/" + "dashboard.html").exists())
            new File(summaryReportFilePath + "/" + "dashboard.html").delete();

        FileOutputStream fosSummary = null;
        PrintWriter pwSummary = null;
        try {
            fosSummary = new FileOutputStream(new File(summaryReportFilePath + "/" + "dashboard.html"), true);
            pwSummary = new PrintWriter(fosSummary);
            pwSummary.println(dashboardContent);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logging.log("error", "Error in generating Dashboard. Exception:" + ex.getMessage());
        } finally {
            try {
                if (pwSummary != null)
                    pwSummary.close();
                if (fosSummary != null)
                    fosSummary.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Logging.log("error", "Error in generating Dashboard. Exception:" + ex.getMessage());
            }
        }
    }
}
