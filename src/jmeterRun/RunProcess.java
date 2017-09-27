package jmeterRun;

import utils.APIReportProcessing;
import utils.Helper;
import utils.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RunProcess {

    private String jmxFile;
    private String jMeterPath;
    private String resultFolder;
    private String rawXMLDataFile;
    private String xslFile;
    private String moduleName;
    private String reportDateTime;
    private String envProp;
    private String resourceDir;
    private String baseUrl = "";
    private String email = "";
    private String pwd = "";
    private String clientId = "";

    public RunProcess(String jMeterPth, String moduleName, String jmxFile, String resultFolder, String xslFile,
                      String reportDateTime) {
        try {
            this.jMeterPath = jMeterPth;
            this.jmxFile = jmxFile;
            this.resultFolder = resultFolder;
            this.xslFile = xslFile;
            this.moduleName = moduleName;
            this.rawXMLDataFile = resultFolder + moduleName + ".xml";
            this.reportDateTime = reportDateTime;

            if (System.getProperty("os.name").toLowerCase().contains("window"))
                jMeterPath = "\"" + jMeterPath + "jmeter.bat" + "\"";
            else
                jMeterPath = "sh " + jMeterPath + "jmeter";

            resourceDir = System.getProperty("Resources");
            if (resourceDir == null)
                resourceDir = "";

            String targetEnv = "";
            baseUrl = System.getProperty("baseUrl");
            /*if (baseUrl.contains("staging"))
                targetEnv = "staging";
            else
                targetEnv = "prod";*/
            targetEnv="staging";
            envProp = resourceDir + "JMX_Modules/properties/automation_" + targetEnv + ".properties";
            email = System.getProperty("email");
            pwd = System.getProperty("pwd");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logging.log("error", "Error while setting up JMX file configuration. Exception:" + ex.getMessage());
        }
    }

    public String[] executeJMeterAndWriteResults() {
        String[] moduleWiseReport = {"", "", ""};
        try {
            String command = "";
            APIReportProcessing.apiResponseList.clear();
            moduleWiseReport[0] = moduleName;
            moduleWiseReport[1] = moduleName + "_" + reportDateTime + ".html";
            if (System.getProperty("os.name").toLowerCase().contains("window"))
                command = jMeterPath
                        + " -Jjmeter.save.saveservice.output_format=xml -Jjmeter.save.saveservice.assertion_results=all -Jjmeter.save.saveservice.response_code=true -Jjmeter.save.saveservice.response_data=true -Jjmeter.save.saveservice.response_message=true -Jjmeter.save.saveservice.subresults=true -Jjmeter.save.saveservice.assertions=true -Jjmeter.save.saveservice.samplerData=true -Jjmeter.save.saveservice.responseHeaders=true -Jjmeter.save.saveservice.requestHeaders=true -Jjmeter.save.saveservice.url=true -n -t "
                        + "\"" + jmxFile + "\"" + " -l " + "\"" + resultFolder + moduleName + ".xml" + "\"" + " --addprop " + "\"" + envProp + "\"" + " -JresourceDir=" + "\"" + resourceDir + "\"" + " -JbaseUrl=" + baseUrl + " -Jemail=" + email + " -Jpwd=" + pwd;
            else
                command = jMeterPath
                        + " -Jjmeter.save.saveservice.output_format=xml -Jjmeter.save.saveservice.assertion_results=all -Jjmeter.save.saveservice.response_code=true -Jjmeter.save.saveservice.response_data=true -Jjmeter.save.saveservice.response_message=true -Jjmeter.save.saveservice.subresults=true -Jjmeter.save.saveservice.assertions=true -Jjmeter.save.saveservice.samplerData=true -Jjmeter.save.saveservice.responseHeaders=true -Jjmeter.save.saveservice.requestHeaders=true -Jjmeter.save.saveservice.url=true -n -t "
                        + jmxFile + " -l " + resultFolder + moduleName + ".xml" + " --addprop " + envProp + " -JresourceDir=" + resourceDir + " -JbaseUrl=" + baseUrl + " -Jemail=" + email + " -Jpwd=" + pwd;
            System.out.println(command);
            Process pro = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            byte[] bytes = new byte[4096];
            String line, output = "";
            while ((line = input.readLine()) != null) {
                output = output + line;
                System.out.println(line);
            }
            input.close();
            prepareFinalResults(moduleWiseReport[1]);
            if (APIReportProcessing.apiResponseList.size() > 0)
                moduleWiseReport[2] = "true";
            else
                moduleWiseReport[2] = "false";
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while running JMX file through JMeter Non GUI mode. Error occurred in JMX File:"
                    + jmxFile + ". Exception:" + e.getMessage());
        }
        return moduleWiseReport;
    }

    private void prepareFinalResults(String moduleReport) throws IOException {
        try {
            String currentDateTimeReadable = Helper.getCurrentDateInReadableFormat();
            HashMap<String, String> globalParams = new HashMap<String, String>();
            globalParams.put("titleReport", moduleName);
            globalParams.put("dateReport", currentDateTimeReadable);
            APIReportProcessing.generatModuleWiseReportFile(rawXMLDataFile, xslFile, resultFolder + moduleReport,
                    globalParams);
            APIReportProcessing.fetchAPIReportDetail(rawXMLDataFile);
            APIReportProcessing.generateDetailedAPIReport(resultFolder, moduleName, currentDateTimeReadable);
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error",
                    "Error while setting up parameters for final result preparation. Exception:" + e.getMessage());
        }
    }

}