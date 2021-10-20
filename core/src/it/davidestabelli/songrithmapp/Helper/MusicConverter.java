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

import com.alibaba.fastjson.JSONArray;
import com.badlogic.gdx.files.FileHandle;

import it.davidestabelli.songrithmapp.Helper.FFT.FFT;
import it.davidestabelli.songrithmapp.Helper.FFT.WaveDecoder;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

public class MusicConverter {
    private static final int NUMBER_OF_SPECTRUMS = 19;
    private static final float BEAT_TRACE_SAMPLE = 0.11f;
    public static final DateTimeFormatter AUDIO_FORMAT = DateTimeFormatter.ofPattern("mm:ss");

    private File source;		                 
    private File oggTarget;
    private File wavTarget;

    private Long duration; // millis

    private String stringedDuration;
    private float secondsDuration;

    private String sourceFormat;
    private String fileName;

    private List<Float>[] spectrumList;

    private boolean hasBeatTrace;
    private int[] beatTrace;
    private double beatTraceDurationRatio;
    private double durationBeatTraceRatio;

    public MusicConverter(String oggPath, String wavPath, Map<String,Object> spectrumListMap, int[] beatTrace, String name) throws EncoderException {
        this.oggTarget = new File(oggPath);
        this.wavTarget = new File(wavPath);
        spectrumList = new List[NUMBER_OF_SPECTRUMS];
        for (int i = 0; i < spectrumList.length; i++) {
            JSONArray mapArray = (JSONArray)spectrumListMap.get(String.format("%d",i));
            List<Float> spectrum = new ArrayList<Float>();
            for (Object value : mapArray) {
                BigDecimal decimalValue = (BigDecimal) value;
                spectrum.add(decimalValue.floatValue());
            }
            spectrumList[i] = spectrum;
        }

        //Getting infos
        MultimediaObject sourceObject = new MultimediaObject(oggTarget);
        duration = sourceObject.getInfo().getDuration();
        secondsDuration = duration / 1000;
        stringedDuration = LocalTime.ofSecondOfDay(Math.round(secondsDuration)).format(AUDIO_FORMAT);

        sourceFormat = sourceObject.getInfo().getFormat();
        fileName = name;

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

        beatTraceDurationRatio = this.beatTrace.length / duration.doubleValue();
        durationBeatTraceRatio = duration.doubleValue() / this.beatTrace.length;
    }

    public MusicConverter(File toConvert) {
        this.source = toConvert;
        try {
            //Getting infos
            MultimediaObject sourceObject = new MultimediaObject(source);
            duration = sourceObject.getInfo().getDuration();
            sourceFormat = sourceObject.getInfo().getFormat();
            fileName = source.getName().split("\\.")[0];
            
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

            if(!sourceFormat.equals("ogg")){            
                //Audio Attributes                                       
                AudioAttributes audio = new AudioAttributes();              
                audio.setCodec("libvorbis");
                audio.setChannels(2);
                float samplingRate = (40960/ (BEAT_TRACE_SAMPLE * 2 * 2)) * 2;
                audio.setSamplingRate(Math.round(samplingRate));
                                                                            
                //Encoding attributes                                       
                EncodingAttributes attrs = new EncodingAttributes();        
                attrs.setOutputFormat("ogg");                                     
                attrs.setAudioAttributes(audio);
                                                                            
                //Encoder
                oggTarget = Files.createTempFile(source.getName().split("\\.")[0], ".ogg").toFile();                 
                Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(source), oggTarget, attrs);
            } else {
                oggTarget = source;
            }

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
}

