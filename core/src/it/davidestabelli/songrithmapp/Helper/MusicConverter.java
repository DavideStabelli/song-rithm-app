package it.davidestabelli.songrithmapp.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;

import it.davidestabelli.songrithmapp.Helper.FFT.FFT;
import it.davidestabelli.songrithmapp.Helper.FFT.WaveDecoder;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

public class MusicConverter {
    private static final int NUMBER_OF_SPECTRUMS = 19;

    private File source;		                 
    private File oggTarget;
    private File wavTarget;

    private Long duration;
    private String sourceFormat;
    private String fileName;

    List<Float>[] spectrumList;
    List<Float> spectralFlux;

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

