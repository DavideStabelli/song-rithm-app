package it.davidestabelli.songrithmapp.Helper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

                String dataFilePathString = localPathString + "/" + musicElaboratedFile.getFileName() + ".rs";
                File dataLocalFile = new File(dataFilePathString);
                FileOutputStream outputStream = new FileOutputStream(dataLocalFile);
                StringBuilder fileContent = new StringBuilder();
                fileContent.append("sourcepath:");
                fileContent.append(musicElaboratedFile.getPath());
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
        String filePath = FOLDER_PATH + fileName + "/" + fileName + ".ogg";
        File dataLocalFile = new File(filePath);

        // DA SISTEMARE!
        MusicConverter musicFile = new MusicConverter(dataLocalFile);
        
        return musicFile;
    }
}