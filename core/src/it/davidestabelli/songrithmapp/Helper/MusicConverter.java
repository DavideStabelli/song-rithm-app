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

import it.davidestabelli.songrithmapp.Helper.FFT.FFT;
import it.davidestabelli.songrithmapp.Helper.FFT.WaveDecoder;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

@SuppressWarnings("NewApi")
public class MusicConverter {
    private static final int NUMBER_OF_SPECTRUMS = 19;
    private static final float BEAT_TRACE_SAMPLE = 0.11f;
    public static final DateTimeFormatter AUDIO_FORMAT = DateTimeFormatter.ofPattern("mm:ss");

    public static final int FINISH_STATUS = 1;
    public static final int STARTING_STATUS = 0;
    public static final int ERROR_STATUS = 2;

    private File source;		                 
    private File oggTarget;
    private File wavTarget;
    private int oggTargetStatus;
    private int wavTargetStatus;
    private List<Float>[] spectrumList;
    private int spectrumListStatus;
    private int importStatus;
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

    public MusicConverter(String oggPath, int[] beatTrace, String name, int numberOfBeatTraces) throws EncoderException {
        this.oggTarget = new File(oggPath);
        //this.wavTarget = new File(wavPath);
        /*this.spectrumList = new List[NUMBER_OF_SPECTRUMS];
        for (int i = 0; i < this.spectrumList.length; i++) {
            JSONArray mapArray = (JSONArray)spectrumListMap.get(String.format("%d",i));
            List<Float> spectrum = mapArray.parallelStream().filter(entry -> entry instanceof BigDecimal).map(e -> ((BigDecimal) e).floatValue()).collect(Collectors.toList());
            //List<Float> spectrum = new ArrayList<Float>();
            //for (Object value : mapArray) {
            //    BigDecimal decimalValue = (BigDecimal) value;
            //    spectrum.add(decimalValue.floatValue());
            //}
            this.spectrumList[i] = spectrum;
        }*/

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

        this.oggTargetStatus = FINISH_STATUS;
        this.wavTargetStatus = FINISH_STATUS;
        this.spectrumListStatus = FINISH_STATUS;
        this.importStatus = FINISH_STATUS;
        this.importingPercentage = 100;
    }

    public MusicConverter(File toConvert) {
        this.source = toConvert;
        oggTarget = null;
        wavTarget = null;

        this.oggTargetStatus = STARTING_STATUS;
        this.wavTargetStatus = STARTING_STATUS;
        this.spectrumListStatus = STARTING_STATUS;
        this.importStatus = STARTING_STATUS;
        this.importingPercentage = 0;

        this.numberOfBeatTraces = 4;

        try {
            //Getting infos
            MultimediaObject sourceObject = new MultimediaObject(source);
            duration = sourceObject.getInfo().getDuration();
            sourceFormat = sourceObject.getInfo().getFormat();
            fileName = source.getName().split("\\.")[0];

            CountDownLatch latch = new CountDownLatch(2);
            ExecutorService executor = Executors.newFixedThreadPool(3);

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
                        this.oggTargetStatus = FINISH_STATUS;
                    } catch (Exception e){
                        oggTarget = null;
                        this.oggTargetStatus = ERROR_STATUS;
                    }
                } else {
                    oggTarget = source;
                    this.oggTargetStatus = FINISH_STATUS;
                }
                this.importingPercentage += 33;
                latch.countDown();
            });

            executor.execute(() -> {
                try {
                    if(!sourceFormat.equals("wav")){
                        //Audio Attributes
                        AudioAttributes audio = new AudioAttributes();
                        audio.setCodec("pcm_s16le");
                        audio.setChannels(2);
                        audio.setSamplingRate(44100);

                        //Encoding attributes
                        EncodingAttributes attrs = new EncodingAttributes();
                        attrs.setOutputFormat("wav");
                        attrs.setAudioAttributes(audio);
                        attrs.setMapMetaData(true);

                        //Encoder
                        wavTarget = Files.createTempFile(source.getName().split("\\.")[0], ".wav").toFile();
                        Encoder encoder = new Encoder();
                        encoder.encode(new MultimediaObject(source), wavTarget, attrs);
                    } else {
                        wavTarget = source;
                    }
                    this.wavTargetStatus = FINISH_STATUS;
                } catch (Exception e){
                    wavTarget = null;
                    this.wavTargetStatus = ERROR_STATUS;
                }
                this.importingPercentage += 33;
                /*
                try {
                    WaveDecoder decoder = new WaveDecoder(new FileInputStream(wavTarget));
                    FFT fft = new FFT(1024, 44100);

                    float[] samples = new float[1024];
                    float[] spectrum = new float[1024 / 2 + 1];
                    spectrumList = new List[NUMBER_OF_SPECTRUMS];
                    while (decoder.readSamples(samples) > 0) {
                        fft.forward(samples);
                        spectrum = fft.getSpectrum();

                        int samplesPerSpectrum = spectrum.length / NUMBER_OF_SPECTRUMS;

                        for (int i = 0; i < NUMBER_OF_SPECTRUMS; i++) {
                            float spectrumValueSum = 0;
                            for (int j = 0; j < samplesPerSpectrum; j++)
                                spectrumValueSum += spectrum[j + (i * samplesPerSpectrum)];
                            if(spectrumList[i] == null)
                                spectrumList[i] = new ArrayList<Float>();
                            spectrumList[i].add(spectrumValueSum);
                        }
                    }
                    wavTarget.delete();
                    //toConvert.delete();
                    this.spectrumListStatus = FINISH_STATUS;
                } catch (Exception e){
                    spectrumList = null;
                    this.spectrumListStatus = ERROR_STATUS;
                }

                this.importingPercentage += 25;
                */
                latch.countDown();
            });

            executor.execute(() -> {
                try {
                    latch.await();
                } catch(InterruptedException ex) {
                    System.out.println(ex);
                }

                ImportedFileHandler.importNewFile(this);

                this.importStatus = FINISH_STATUS;
                this.importingPercentage += 34;
            });

            executor.shutdown();
        } catch (Exception ex) {                                      
            ex.printStackTrace();                                       
            oggTarget = null;   
            wavTarget = null;                                     
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

    public void setBeatTrace(float millisPosition, int value){
        Long index = getBeatTraceIndexFromMillis(millisPosition);
        if(!hasBeatTrace)
            hasBeatTrace = true;
        if(value == 0)
            beatTrace[index.intValue()] = 0;
        else
            beatTrace[index.intValue()] = (value | beatTrace[index.intValue()]);
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

    public FileHandle getWavTarget() {
        return new FileHandle(wavTarget);
    }

    public Long getDuration() {
        return duration;
    }

    public List[] getSpectrumList() {
        return spectrumList;
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
}

