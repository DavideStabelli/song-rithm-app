package it.davidestabelli.songrithmapp.Helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.badlogic.gdx.graphics.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

public class ImportedFileHandler {
    public static final String FOLDER_PATH = (new JFileChooser().getFileSystemView().getDefaultDirectory().toString()) + "/RythmSong/";

    public static void importNewFile(MusicConverter musicElaboratedFile) {
        try {
            File localFolder = new File(FOLDER_PATH);
            if(!localFolder.exists())
                Files.createDirectory(localFolder.toPath());

            String localPathString = FOLDER_PATH + musicElaboratedFile.getFileName();
            File localFileFolder = new File(localPathString); 
            if(!localFileFolder.exists()){
                Files.createDirectory(localFileFolder.toPath());
            }

            String oggFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".ogg";
            File oggLocalFile = new File(oggFilePathString);
            if(!musicElaboratedFile.getOggTarget().path().equals(oggLocalFile.getPath()))
                Files.copy(musicElaboratedFile.getOggTarget().file().toPath(), oggLocalFile.toPath());

            String dataFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".json";
            File dataLocalFile = new File(dataFilePathString);
            FileOutputStream outputStream = new FileOutputStream(dataLocalFile);

            Map<String,Object> fileContentMap = new HashMap<>();
            fileContentMap.put("oggPath", musicElaboratedFile.getOggTarget().path());

            fileContentMap.put("numberOfTraces", musicElaboratedFile.getNumberOfBeatTraces());

            fileContentMap.put("hasBeatTrace", musicElaboratedFile.hasBeatTrace());

            if(musicElaboratedFile.hasBeatTrace())
                fileContentMap.put("beatTrace", musicElaboratedFile.getBeatTrace());

            String[] colorCodes = new String[musicElaboratedFile.getNumberOfBeatTraces()];
            for (int i = 0; i < musicElaboratedFile.getNumberOfBeatTraces(); i++) {
                colorCodes[i] = musicElaboratedFile.getBeatTraceColor()[i].toString();
            }
            fileContentMap.put("beatTraceColor", colorCodes);

            fileContentMap.put("beatTraceNames", musicElaboratedFile.getBeatTraceNames());

            JSONObject fileContent = new JSONObject(fileContentMap);
            outputStream.write(fileContent.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void updateBeatTrace(MusicConverter music){
        String fileName = music.getFileName();
        String filePath = FOLDER_PATH + fileName + "/" + fileName + ".json";
        try
        {
            File dataLocalFile = new File(filePath);
            String content = new String ( Files.readAllBytes( dataLocalFile.toPath() ) );
            Map<String,Object> readedMap = JSONObject.parseObject(content).getInnerMap();
            readedMap.put("hasBeatTrace", music.hasBeatTrace());
            if(music.hasBeatTrace())
                readedMap.put("beatTrace", music.getBeatTrace());

            String[] colorCodes = new String[music.getNumberOfBeatTraces()];
            for (int i = 0; i < music.getNumberOfBeatTraces(); i++) {
                colorCodes[i] = music.getBeatTraceColor()[i].toString();
            }
            readedMap.put("beatTraceColor", colorCodes);
            readedMap.put("beatTraceNames", music.getBeatTraceNames());

            FileOutputStream outputStream = new FileOutputStream(dataLocalFile);
            JSONObject fileContent = new JSONObject(readedMap);
            outputStream.write(fileContent.toString().getBytes());
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static List<String> getFileList(){
        List<String> listOfImportedFiles = new ArrayList<String>();
        File folder = new File(FOLDER_PATH);
        if(folder.exists()){
            File[] files = folder.listFiles();
            for (File file : files) {
                listOfImportedFiles.add(file.getName().split("\\.")[0]);
            }
        }
        return listOfImportedFiles;
    }

    public static MusicConverter getMusicFileFromImport(String fileName){
        String filePath = FOLDER_PATH + fileName + "/" + fileName + ".json";
        try
        {
            String content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
            Map<String,Object> readedMap = JSONObject.parseObject(content).getInnerMap();
            String oggPath = (String) readedMap.get("oggPath");
            int[] beatTrace = null;
            if((boolean) readedMap.get("hasBeatTrace")) {
                JSONArray listArray = (JSONArray)readedMap.get("beatTrace");
                beatTrace = listArray.stream().filter(i -> i instanceof Integer).mapToInt(e -> (int) e).toArray();
            }
            Integer numberOfTraces = (Integer) readedMap.get("numberOfTraces");

            JSONArray listArray = (JSONArray)readedMap.get("beatTraceColor");
            Color[] beatTraceColor = listArray.stream().filter(i -> i instanceof String).map(e -> Color.valueOf((String) e)).collect(Collectors.toList()).toArray(new Color[0]);

            listArray = (JSONArray)readedMap.get("beatTraceNames");
            String[] beatTraceNames = listArray.stream().filter(i -> i instanceof String).map(e -> (String) e).collect(Collectors.toList()).toArray(new String[0]);

            return new MusicConverter(oggPath, beatTrace, fileName, numberOfTraces, beatTraceColor, beatTraceNames);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Map getConfigurations(){
        String filePath = FOLDER_PATH + "config.ini";
        try
        {
            String content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
            Map<String,Object> readedMap = JSONObject.parseObject(content).getInnerMap();

            return readedMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteImport(String fileName){
        String filePath = FOLDER_PATH + fileName;
        try {
            File directory = new File(filePath);
            if(directory.exists()){
                File[] files = directory.listFiles();
                for (File file : files) {
                    file.delete();
                }
            }
            directory.delete();
            return true;
        } catch (Exception e){
            return false;
        }
    }
}