package it.davidestabelli.songrithmapp.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.graphics.Color;
import it.davidestabelli.songrithmapp.Helper.FFT.FFT;
import it.davidestabelli.songrithmapp.Helper.FFT.WaveDecoder;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

@SuppressWarnings("NewApi")
public class MusicConverter {
    private static final float BEAT_TRACE_SAMPLE = 0.11f;
    public static final DateTimeFormatter AUDIO_FORMAT = DateTimeFormatter.ofPattern("mm:ss");

    private File source;		                 
    private File oggTarget;
    private int importingPercentage;

    private Long duration; // millis

    private String stringedDuration;
    private float secondsDuration;

    private String sourceFormat;
    private String fileName;

    private boolean hasBeatTrace;
    private int[] beatTrace;
    private double beatTraceDurationRatio;
    private double durationBeatTraceRatio;
    private int numberOfBeatTraces;
    private Color[] beatTraceColor;
    private String[] beatTraceNames;

    public MusicConverter(String oggPath, int[] beatTrace, String name, int numberOfBeatTraces, Color[] beatTraceColor, String[] beatTraceNames) throws EncoderException {
        this.oggTarget = new File(oggPath);

        //Getting infos
        MultimediaObject sourceObject = new MultimediaObject(oggTarget);
        this.duration = sourceObject.getInfo().getDuration();
        this.secondsDuration = duration / 1000;
        this.stringedDuration = LocalTime.ofSecondOfDay(Math.round(secondsDuration)).format(AUDIO_FORMAT);

        this.sourceFormat = sourceObject.getInfo().getFormat();
        this.fileName = name;

        //Set Beat Trace
        if(beatTrace == null) {
            int beatsIntoTrace = Math.round((duration / 1000) / BEAT_TRACE_SAMPLE);
            this.beatTrace = new int[beatsIntoTrace];
            for (int i = 0; i < beatsIntoTrace; i++)
                this.beatTrace[i] = 0;
            this.hasBeatTrace = false;
        } else {
            this.beatTrace = beatTrace;
            this.hasBeatTrace = true;
        }
        this.numberOfBeatTraces = numberOfBeatTraces;
        this.beatTraceDurationRatio = this.beatTrace.length / duration.doubleValue();
        this.durationBeatTraceRatio = duration.doubleValue() / this.beatTrace.length;

        this.importingPercentage = 100;

        this.beatTraceColor = beatTraceColor;
        this.beatTraceNames = beatTraceNames;
    }

    public MusicConverter(File toConvert) {
        this.source = toConvert;
        oggTarget = null;

        this.importingPercentage = 0;
        this.numberOfBeatTraces = 4;

        this.beatTraceColor = new Color[numberOfBeatTraces];
        this.beatTraceNames = new String[numberOfBeatTraces];
        for (int i = 0; i < numberOfBeatTraces; i++) {
            beatTraceColor[i] = Color.WHITE;
            beatTraceNames[i] = "";
        }

        try {
            //Getting infos
            MultimediaObject sourceObject = new MultimediaObject(source);
            duration = sourceObject.getInfo().getDuration();
            sourceFormat = sourceObject.getInfo().getFormat();
            fileName = source.getName().split("\\.")[0];

            CountDownLatch latch = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            executor.execute(() -> {
                if(!sourceFormat.equals("ogg")){
                    try {
                        //Audio Attributes
                        AudioAttributes audio = new AudioAttributes();
                        audio.setCodec("libvorbis");
                        audio.setChannels(2);
                        float samplingRate = (40960 / (BEAT_TRACE_SAMPLE * 2 * 2)) * 2;
                        audio.setSamplingRate(Math.round(samplingRate));

                        //Encoding attributes
                        EncodingAttributes attrs = new EncodingAttributes();
                        attrs.setOutputFormat("ogg");
                        attrs.setAudioAttributes(audio);

                        //Encoder
                        oggTarget = Files.createTempFile(source.getName().split("\\.")[0], ".ogg").toFile();
                        Encoder encoder = new Encoder();
                        encoder.encode(new MultimediaObject(source), oggTarget, attrs);
                    } catch (Exception e){
                        oggTarget = null;
                    }
                } else {
                    oggTarget = source;
                }
                this.importingPercentage += 33;
                latch.countDown();
            });

            executor.execute(() -> {
                try {
                    latch.await();
                } catch(InterruptedException ex) {
                    System.out.println(ex);
                }

                ImportedFileHandler.importNewFile(this);

                this.importingPercentage += 67;
            });

            executor.shutdown();
        } catch (Exception ex) {                                      
            ex.printStackTrace();                                       
            oggTarget = null;
        }
    }

    public long getBeatTraceIndexFromMillis(float millisPosition){
        long index = 0;
        double doubleIndex = millisPosition * beatTraceDurationRatio;
        doubleIndex = Math.floor(doubleIndex);
        index = Math.round(doubleIndex);
        if(index >= beatTrace.length)
            index = beatTrace.length - 1;
        return index;
    }

    public long getMillisFromBeatTraceIndex(int index){
        double rawMillis = (index * durationBeatTraceRatio) + BEAT_TRACE_SAMPLE*500;

        return Math.round(rawMillis);
    }

    public void setBeatTrace(float millisPosition, int value, boolean add){
        Long index = getBeatTraceIndexFromMillis(millisPosition);
        if(!hasBeatTrace)
            hasBeatTrace = true;
        if(value == 0)
            beatTrace[index.intValue()] = 0;
        else
            beatTrace[index.intValue()] = add ? (value | beatTrace[index.intValue()]) : value;
    }

    public int getBeatTrace(float millisPosition){
        Long index = getBeatTraceIndexFromMillis(millisPosition);
        return beatTrace[index.intValue()];
    }

    public void clearBeatTrace(){
        for (int i = 0; i < beatTrace.length; i++) {
            beatTrace[i] = 0;
        }
        hasBeatTrace = false;
    }

    public void clearBeatTrace(int index){
        for (int i = 0; i < beatTrace.length; i++) {
            int beatTraceInstant = beatTrace[i];
            int value = 3;
            value = value << (2*index);
            value = value ^ 255;
            value = beatTraceInstant & value;
            beatTrace[i] = value;
        }
    }

    public void deleteBeatTrace(int index){
        if(index < numberOfBeatTraces && index > 0){
            this.numberOfBeatTraces = numberOfBeatTraces - 1;
            for (int i = 0; i < beatTrace.length; i++) {
                int suffixPosition = 255 >> 8 - (2 * index);
                int suffix = beatTrace[i] & suffixPosition;
                int prefixPosition = 255 << 2 * (index + 1);
                int prefix = (beatTrace[i] & prefixPosition) >> 2;
                beatTrace[i] = prefix | suffix;
            }
            List<Color> newBeatTraceColor = new ArrayList<>();
            List<String> newBeatTraceNames = new ArrayList<>();
            for (int i = 0; i < beatTraceColor.length; i++) {
                if(i != index) {
                    newBeatTraceColor.add(beatTraceColor[i]);
                    newBeatTraceNames.add(beatTraceNames[i]);
                }
            }
            this.beatTraceColor = newBeatTraceColor.toArray(new Color[0]);
            this.beatTraceNames = newBeatTraceNames.toArray(new String[0]);
        }
    }

    public String getStringedDuration() {
        return stringedDuration;
    }

    public float getSecondsDuration() {
        return secondsDuration;
    }

    public boolean hasBeatTrace(){
        return hasBeatTrace;
    }

    public int[] getBeatTrace() {
        return beatTrace;
    }

    public FileHandle getOggTarget() {
        return new FileHandle(oggTarget);
    }

    public Long getDuration() {
        return duration;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return source.getPath();
    }

    public int getImportingPercentage() {
        return importingPercentage;
    }

    public File getSource() {
        return source;
    }

    public int getNumberOfBeatTraces() {
        return numberOfBeatTraces;
    }

    public Color[] getBeatTraceColor() {
        return beatTraceColor;
    }

    public String[] getBeatTraceNames() {
        return beatTraceNames;
    }

    public void setBeatTraceColor(Color beatTraceColor, int i) {
        this.beatTraceColor[i] = beatTraceColor;
    }

    public void setBeatTraceNames(String beatTraceNames, int i) {
        this.beatTraceNames[i] = beatTraceNames;
    }
}

