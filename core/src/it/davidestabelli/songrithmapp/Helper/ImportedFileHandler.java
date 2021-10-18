package it.davidestabelli.songrithmapp.Helper;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if(localFileFolder.exists()){
                throw new Exception("Il file è già stato importato, se lo si vuole reimportare cancellare quello già esistente!");
            } else {
                Files.createDirectory(localFileFolder.toPath());
                
                String oggFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".ogg";
                File oggLocalFile = new File(oggFilePathString);
                Files.copy(musicElaboratedFile.getOggTarget().file().toPath(), oggLocalFile.toPath());

                String wavFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".wav";
                File wavLocalFile = new File(wavFilePathString);
                Files.copy(musicElaboratedFile.getWavTarget().file().toPath(), wavLocalFile.toPath());

                String dataFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".json";
                File dataLocalFile = new File(dataFilePathString);
                FileOutputStream outputStream = new FileOutputStream(dataLocalFile);

                Map<String,Object> fileContentMap = new HashMap<>();
                fileContentMap.put("oggPath", musicElaboratedFile.getOggTarget().path());
                fileContentMap.put("wavPath", musicElaboratedFile.getWavTarget().path());
                Map<String,Object> fileSpectrumListMap = new HashMap<>();
                for (int i = 0; i < musicElaboratedFile.getSpectrumList().length; i++) {
                    List<Float> singleSpectrumList = musicElaboratedFile.getSpectrumList()[i];
                    fileSpectrumListMap.put(String.format("%d", i), singleSpectrumList);
                }
                fileContentMap.put("spectrumList", fileSpectrumListMap);

                JSONObject fileContent = new JSONObject(fileContentMap);
                outputStream.write(fileContent.toString().getBytes());
                outputStream.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
            String wavPath = (String) readedMap.get("wavPath");
            Map spectrumListMap = (Map) readedMap.get("spectrumList");
            return new MusicConverter(oggPath, wavPath, spectrumListMap);
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