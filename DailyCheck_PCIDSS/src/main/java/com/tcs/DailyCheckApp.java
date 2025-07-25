package com.tcs;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.tcs.model.AppConfig;
import com.tcs.model.Machine;
import com.tcs.model.MachineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;

@SpringBootApplication(scanBasePackages = { "com.tcs", "com.tcs.model" })
@Import(AppConfig.class)
public class DailyCheckApp{

    @Qualifier("appConfigBean")
    @Autowired
    public AppConfig APP_PROPERTIES;

    private static ArrayList<String> serverDirList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context  = SpringApplication.run(DailyCheckApp.class, args);

        serverDirList.add("/home/logs/eua-proxy-1401/europa/webservices_payment/");
        serverDirList.add("/home/logs/eua-proxy-2401/europa/webservices_payment/");
        serverDirList.add("/home/logs/eua-proxy-3401/europa/webservices_payment/");
        serverDirList.add("/home/logs/eua-proxy-4401/europa/webservices_payment/");
        serverDirList.add("/home/logs/eua-proxy-1401/europa/amadeus/");
        serverDirList.add("/home/logs/eua-proxy-2401/europa/amadeus/");
        serverDirList.add("/home/logs/eua-proxy-3401/europa/amadeus/");
        serverDirList.add("/home/logs/eua-proxy-4401/europa/amadeus/");
        serverDirList.add("/home/logs/eua-proxy-1401/europa/webservices_cxf/");
        serverDirList.add("/home/logs/eua-proxy-2401/europa/webservices_cxf/");
        serverDirList.add("/home/logs/eua-proxy-3401/europa/webservices_cxf/");
        serverDirList.add("/home/logs/eua-proxy-4401/europa/webservices_cxf/");

        context.getBean(DailyCheckApp.class).start();
        context.close();
        System.out.println("--------------- PCI DSS Checks ---------------");
        System.exit(0);
    }

    public void start() throws Exception {
        Machine machine = MachineFactory.createMachine(AppConfig.MACHINE2USE);
        if(AppConfig.CLEAR_PREV_LOGS){
            clearPrevLogs();
        }
        downloadLogs(machine);

        UnzipGzipFile(machine);
        System.out.println("----All zipped Files were unzipped----");
        searchByKeyword();
        System.out.println("Lines containing the word 'Exception' have been written to the output-all.log file.");
        removeDuplicateLines();
    }

    public void UnzipGzipFile(Machine machine)  {
        Path dir = Paths.get("src\\main\\resources\\downloads");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.gz")) {
            for (Path entry : stream) {
                    unzipGzipFile(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    private static void unzipGzipFile(Path gzipFile) {
        Path outputFile = Paths.get(gzipFile.toString().replaceFirst("[.][^.]+$", ""));

        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzipFile.toFile()));
             FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            System.out.println("Unzipped: " + gzipFile.getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeDuplicateLines() {
        String inputFilePath = "src/main/resources/output-all.log";
        String tempFilePath = "src/main/resources/temp-output.log";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(tempFilePath)) {
            Set<String> uniqueLines = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (uniqueLines.add(line)) {
                    writer.write(line + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while processing the file: " + e.getMessage());
        }
        File inputFile = new File(inputFilePath);
        File tempFile = new File(tempFilePath);
        if (inputFile.delete()) {
            tempFile.renameTo(inputFile);
        } else {
            System.err.println("Could not delete the original file.");
        }
    }
    public void searchByKeyword()  {
        String directoryPath = "src\\main\\resources\\downloads";
        String outputFilePath = "src/main/resources/output-all.log";
        String keyword = "Exception";

        BufferedWriter writer = null;
        String number = "";
        String service = "";
        try {
            writer = new BufferedWriter(new FileWriter(outputFilePath));
            File directory = new File(directoryPath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String regex = "(\\d{4})-(\\w+)";

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.find()) {
                        number = matcher.group(1);
                        service = matcher.group(2);
                    }
                    if (file.isFile()) {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains(keyword)) {
                                    writer.write(number +" "+service+"==>"+line);
                                    writer.newLine();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Search complete. The lines containing the keyword 'Exception' have been written to " + outputFilePath);
    }

    private void downloadLogs(Machine machine) throws Exception {
        for (String serverHostName : machine.getMachineNames()) {
            try (EAServer server = new EAServer(serverHostName, AppConfig.WALLIX_USERNAME, AppConfig.WALLIX_PASSWORD)) {

                for (String remoteDir : serverDirList) {
                    String[] parts = remoteDir.split("-");
                    String number = parts[parts.length - 1].replaceAll("[^0-9]", "");
                    String[] serverName = remoteDir.split("/");
                    String extracted = serverName[serverName.length - 1];

                    boolean flag = server.downloadFile(remoteDir,AppConfig.TARGET_LOCAL_DIR + number +"-"+ extracted +"-");
                    if(flag) {
                        System.out.println("All the files containing given date in "+remoteDir+" has been downloaded");
                    }else {
                        System.out.println("There is no such files in "+remoteDir);
                    }
                }
                }
            }}

    private void clearPrevLogs() {
        System.out.println("--------------- Delete Previous Logs ---------------");
        File[] logFiles = new File(AppConfig.TARGET_LOCAL_DIR).listFiles((dir, name) -> name.toLowerCase().endsWith(".log.gz"));
        for (File logFile : logFiles) {
            logFile.delete();
        }
        File[] logFiless = new File(AppConfig.TARGET_LOCAL_DIR).listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));
        for (File logFile : logFiless) {
            logFile.delete();
        }
        System.out.println("--------------- Delete Previous Logs Completed---------------");
    }
}