package utils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.slf4j.Logger;

public class Helper {
    public static Logger logger = null;

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("d_MMM_y_H_m_s");
        Date date = new Date();
        TimeZone timeZone = TimeZone.getTimeZone("IST");
        dateFormat.setTimeZone(timeZone);
        String currentTime = dateFormat.format(date);
        return currentTime;
    }

    public static String getCurrentDateInReadableFormat() {
        DateFormat dateFormat = new SimpleDateFormat("d-MMM-y H:m:s");
        Date date = new Date();
        TimeZone timeZone = TimeZone.getTimeZone("IST");
        dateFormat.setTimeZone(timeZone);
        String currentTime = dateFormat.format(date);
        return currentTime;
    }

    public static void createDir(String dirLocation) {
        try {
            File file = new File(dirLocation);
            if (!file.exists())
                file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while creating result directory. Exception:" + e.getMessage());
        }
    }

    public static void loadResources(String resultFolder) throws IOException {
        try {
            if (!new File(resultFolder + "/collapse.png").exists())
                FileUtils.copyFile(new File(System.getProperty("user.dir") + "/Properties/collapse.png"),
                        new File(resultFolder + "/collapse.png"));

            if (!new File(resultFolder + "/expand.png").exists())
                FileUtils.copyFile(new File(System.getProperty("user.dir") + "/Properties/expand.png"),
                        new File(resultFolder + "/expand.png"));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while copying appropriate resources. Exception:" + e.getMessage());
        }
    }

    public static void loadResources(String sourceFolder, String resultFolder) throws IOException {
        try {
            if (!new File(resultFolder + "/collapse.png").exists())
                FileUtils.copyFile(new File(sourceFolder + "/collapse.png"), new File(resultFolder + "/collapse.png"));

            if (!new File(resultFolder + "/expand.png").exists())
                FileUtils.copyFile(new File(sourceFolder + "/expand.png"), new File(resultFolder + "/expand.png"));

            if (!new File(resultFolder + "/images/").exists())
                FileUtils.copyDirectory(new File(sourceFolder + "/Configuration/images"), new File(resultFolder + "/images"));

            if (!new File(resultFolder + "/font/").exists())
                FileUtils.copyDirectory(new File(sourceFolder + "/Configuration/font"), new File(resultFolder + "/font"));

            if (!new File(resultFolder + "/css/").exists())
                FileUtils.copyDirectory(new File(sourceFolder + "/Configuration/css"), new File(resultFolder + "/css"));

            if (!new File(resultFolder + "/assets/").exists())
                FileUtils.copyDirectory(new File(sourceFolder + "/Configuration/assets"), new File(resultFolder + "/assets"));


        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while copying appropriate resources. Exception:" + e.getMessage());
        }
    }


    public static String checkForValidDirectory(String dirPath) {
        if (dirPath == null)
            dirPath = "";
        if ((!dirPath.endsWith("/") && !dirPath.endsWith("\\"))) {
            dirPath += "/";
        }
        return dirPath;
    }

    public static void generateJmeterProperyFile(String resourceFolderPath) {
        try {
            HashMap<String, String> jmxExec = new HashMap<String, String>();
            HashMap<String, String> jmxModuleMapping = new ConfigReader(resourceFolderPath + "Configuration/JMX_Mapping.properties").getConfig();
            String[] modules = System.getProperty("Modules").split(",");
            if (modules.length > 0) {
                for (String mod : modules) {
                    String[] modNameValue = mod.split("=");
                    if (Boolean.valueOf(modNameValue[1])) {
                        if (jmxModuleMapping.containsKey(modNameValue[0]))
                            jmxExec.put(modNameValue[0], jmxModuleMapping.get(modNameValue[0]));
                    }
                }
                File jmxModuleConfigFile = new File(resourceFolderPath + "Configuration/JmeterTestExecution.properties");
                if (jmxModuleConfigFile.exists())
                    jmxModuleConfigFile.delete();
                jmxModuleConfigFile.createNewFile();
                writeToFile(jmxExec, jmxModuleConfigFile.getAbsolutePath());
            } else {
                System.out.println("No modules present to run. Please check the run time parameters.");
                Logging.log("error", "No modules present to run. Please check the run time parameters.");
            }
        } catch (Exception e) {
            Logging.log("error", "Error while setting up JmeterTestExecution.properties file. Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addJmxFileModules(HashMap<String, String> jmxExec, HashMap<String, String> jmxModuleMapping, String moduleName) {
        if (Boolean.valueOf(System.getProperty(moduleName))) {
            if (jmxModuleMapping.containsKey(moduleName))
                jmxExec.put(moduleName, jmxModuleMapping.get(moduleName));
        }
    }


    public static void writeToFile(HashMap<String, String> jmxExec, String fileName) throws Exception {
        PrintWriter output = null;
        Properties properties = new Properties();
        Set set = jmxExec.keySet();
        try {
            Iterator itr = set.iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                String value = jmxExec.get(key);
                properties.setProperty(key, value);
            }
            properties.store(new FileOutputStream(fileName), "JmeterTestExecution.properties");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logging.log("error", "Error while generating JmeterTestExecution.properties file. Exception:" + ex.getMessage());
        }
    }


    public static void archiveReports(String targetDirPath, String zipFileName) {
        try {
            int days = 7;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date oldestAllowedFileDate = calendar.getTime();
            System.out.println("Target Date:" + oldestAllowedFileDate);
            File targetDir = new File(targetDirPath);
            Iterator<File> filesToDelete = FileUtils.iterateFiles(targetDir, new AgeFileFilter(oldestAllowedFileDate),
                    null);

            ArrayList<File> listOfFilesToZip = new ArrayList<File>();
            while (filesToDelete.hasNext()) {
                File procFile = filesToDelete.next();
                if (procFile.getName().equals("collapse.png") || procFile.getName().equals("expand.png") || procFile.getName().contains(".zip") || procFile.getName().equals("reportData.txt"))
                    continue;
                listOfFilesToZip.add(procFile);
            }
            if (listOfFilesToZip.isEmpty())
                return;
            zipFiles(zipFileName, listOfFilesToZip);
            for (File delFile : listOfFilesToZip) {
                FileUtils.deleteQuietly(delFile);
            }
            Logging.log("info", "Archived report(s) earlier then " + days + " days.");
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while archiving report(s). Exception:" + e.getMessage());
        }
    }

    public static void zipFiles(String zipperFileName, ArrayList<File> reportList) {
        try {
            FileOutputStream fout = new FileOutputStream(zipperFileName);
            ZipOutputStream zout = new ZipOutputStream(fout);
            for (File file : reportList) {
                zout.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                int len;
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    zout.write(buffer, 0, len);
                }
                in.close();
                zout.closeEntry();
            }
            zout.close();
            fout.close();
            Logging.log("info", "Archived report(s) with zipper name:" + zipperFileName);
        } catch (Exception e) {
            e.printStackTrace();
            Logging.log("error", "Error while archiving report(s). Exception:" + e.getMessage());
        }
    }


}
