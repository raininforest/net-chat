package client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryLog {
    private static BufferedWriter logWriter;

    private HistoryLog() {}

    public static List<String> getLastMessages(String login, int countOfMessages){
        File file = new File("history_" + login + ".txt");
        List<String> fullStringList = new ArrayList<>();
        List<String> resultStringList = new ArrayList<>();
        if (file.exists()){
            try (BufferedReader logReader = new BufferedReader(new FileReader(file))){
                String line;
                while ((line = logReader.readLine()) != null){
                    fullStringList.add(line);
                }
                int startIndexToCopy;
                if (countOfMessages <= fullStringList.size()) {
                    startIndexToCopy = fullStringList.size() - countOfMessages;
                } else {
                    startIndexToCopy = 0;
                }
                for (int i = startIndexToCopy; i < fullStringList.size(); i++) {
                    resultStringList.add(fullStringList.get(i));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultStringList;
    }

    public static void startWritingHistory(String login){
        File file = new File("history_" + login + ".txt");
        try {
            logWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessage(String line){
        if (logWriter != null) {
            try {
                logWriter.append(line + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeFile(){
        try {
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
