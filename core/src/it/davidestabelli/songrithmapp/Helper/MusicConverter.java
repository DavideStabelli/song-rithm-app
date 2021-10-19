package it.davidestabelli.songrithmapp.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
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
    private static final float BEAT_TRACE_SAMPLE = 0.2f;

    private File source;		                 
    private File oggTarget;
    private File wavTarget;

    private Long duration; // millis
    private String sourceFormat;
    private String fileName;

    private List<Float>[] spectrumList;

    private boolean hasBeatTrace;
    private int[] beatTrace;

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
                audio.setBitRate(128000);                                   
                audio.setChannels(2);                                       
                audio.setSamplingRate(44100);                         
                                                                            
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
            //float[] lastSpectrum = new float[1024 / 2 + 1];
            //spectralFlux = new ArrayList<Float>();
            spectrumList = new List[NUMBER_OF_SPECTRUMS];

            while (decoder.readSamples(samples) > 0) {
               /* fft.forward(samples);
                System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
                System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);

                float flux = 0;
                for (int i = 0; i < spectrum.length; i++)
                    flux += (spectrum[i] - lastSpectrum[i]);
                spectralFlux.add(flux);*/

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

            //System.out.println("there are " + spectralFlux.size() + " samples");
        } catch (Exception ex) {                                      
            ex.printStackTrace();                                       
            oggTarget = null;   
            wavTarget = null;                                     
        }
    }

    public int getBeatTraceIndexFromMillis(Long millisPosition){
        int index = 0;
        index = Math.round((millisPosition * beatTrace.length) / duration);
        return index;
    }

    public int getMillisFromBeatTraceIndex(int index){
        return Math.round(index * BEAT_TRACE_SAMPLE * 1000);
    }

    public void setBeatTrace(Long millisPosition, int value){
        int index = getBeatTraceIndexFromMillis(millisPosition);
        if(!hasBeatTrace)
            hasBeatTrace = true;
        if(value == 0)
            beatTrace[index] = 0;
        else
            beatTrace[index] = (value | beatTrace[index]);
    }

    public int getBeatTrace(Long millisPosition){
        int index = getBeatTraceIndexFromMillis(millisPosition);
        return beatTrace[index];
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

