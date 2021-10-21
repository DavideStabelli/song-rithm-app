package it.davidestabelli.songrithmapp.Helper;

import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.proxy.ProxyCredentialsImpl;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;

import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Locale;
import java.util.concurrent.Executors;

public class YouTubeDownlod {
    public static final String YOUTUBE_URL_WATCH = "watch?v";
    public static final int DOWNLOAD_WAITING = 0;
    public static final int DOWNLOAD_IN_PROGRESS = 1;
    public static final int DOWNLOAD_FINISHED = 2;
    public static final int DOWNLOAD_FINISHED_WITH_ERROR = 3;

    private String url;
    private String videoId;

    public String videoInfoString;
    private String videoTitle;
    private Format format;

    private YoutubeDownloader downloader;

    public int downloadProgress;
    public int downloadingState;

    public YouTubeDownlod (String url){
        this.url = url;

        String[] splittetUrl = url.split("=");
        this.videoId = splittetUrl[splittetUrl.length - 1];

        downloadingState = DOWNLOAD_WAITING;

        // init downloader with default config
        this.downloader = new YoutubeDownloader();

        VideoInfo info = requestVideoInfo();
        this.videoTitle = info.details().title();
        String duration = LocalTime.ofSecondOfDay(info.details().lengthSeconds()).format(MusicConverter.AUDIO_FORMAT);
        String viewCount = NumberFormat.getIntegerInstance(Locale.ITALY).format(info.details().viewCount());
        this.videoInfoString = String.format("%s \n %s | %s views", videoTitle, duration, viewCount);

        format = info.bestAudioFormat();
    }

    public VideoInfo requestVideoInfo(){
        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        return response.data();
    }

    public void downloadAudio(){
        this.downloadingState = DOWNLOAD_IN_PROGRESS;
        File outputDir = new File(ImportedFileHandler.FOLDER_PATH);
        this.downloadProgress = 0;
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .saveTo(outputDir)
                .renameTo(this.videoTitle)
                .overwriteIfExists(true)
                .callback(new YoutubeProgressCallback<File>() {
                    @Override
                    public void onDownloading(int progress) {
                        downloadProgress = progress;
                    }

                    @Override
                    public void onFinished(File videoInfo) {
                        System.out.println("Finished file: " + videoInfo);
                        MusicConverter musicConverter = new MusicConverter(videoInfo);
                        ImportedFileHandler.importNewFile(musicConverter);
                        videoInfo.delete();
                        downloadingState = DOWNLOAD_FINISHED;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error: " + throwable.getLocalizedMessage());
                        downloadingState = DOWNLOAD_FINISHED;
                    }
                })
                .async();
        downloader.downloadVideoFile(request);
    }
}
