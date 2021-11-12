package it.davidestabelli.songrithmapp.Helper;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SimpleAudioPlayer {
    private static final int STATUS_STOP = 0;
    private static final int STATUS_PLAY = 1;
    private static final int STATUS_PAUSE = 2;

    private File audioFile;
    private int status;
    private int pausePosition;
    private AudioInputStream audioInputStream;
    private Clip clip;

    public SimpleAudioPlayer(File file) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.audioFile = file;
        this.status = STATUS_STOP;
        this.pausePosition = 0;
        this.audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        this.clip = AudioSystem.getClip();
        clip.open(audioInputStream);
    }

    // Method to play the audio
    public void play()
    {
        //start the clip
        clip.start();

        status = STATUS_PLAY;
    }

    // Method to pause the audio
    public void pause()
    {
        this.pausePosition = clip.getFramePosition();
        clip.stop();
        status = STATUS_PAUSE;
    }

    // Method to resume the audio
    public void resumeAudio() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        clip.close();
        resetAudioStream();
        clip.setFramePosition(pausePosition);
        this.play();
    }

    // Method to restart the audio
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException
    {
        clip.stop();
        clip.close();
        resetAudioStream();
        pausePosition = 0;
        clip.setFramePosition(0);
        this.play();
    }

    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        status = STATUS_STOP;
        pausePosition = 0;
        clip.stop();
        clip.close();
    }

    // Method to jump over a specific part
    public void setPosition(float frame) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException
    {
        int framePosition = Math.round(frame);
        if (frame > 0 && frame < clip.getFrameLength())
        {
            clip.stop();
            clip.close();
            resetAudioStream();
            pausePosition = framePosition;
            clip.setFramePosition(framePosition);
            //this.play();
        }
    }

    public float getSecondsFromFrames(float frames){
        return (clip.getLongFramePosition() / clip.getFormat().getFrameRate());
    }

    public float getPosition(){
        return clip.getLongFramePosition();
    }

    public long getFrameLenght(){
        return clip.getFrameLength();
    }

    public long getMillisPosition(){
        return Math.round(getSecondsFromFrames(getFrameLenght()) * 1000f);
    }

    public long getMillisLenght(){
        return Math.round(clip.getMicrosecondLength() / 1000f);
    }

    // Method to reset audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException
    {
        audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public boolean isPlaying(){
        return status == STATUS_PLAY;
    }
}
