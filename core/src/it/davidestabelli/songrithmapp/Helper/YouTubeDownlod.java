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
import java.util.concurrent.Executors;

public class YouTubeDownlod {
    private String url;
    private String videoId;

    public String videoInfoString;

    private YoutubeDownloader downloader;

    public int downloadProgress;
    public boolean isDownloading;

    public YouTubeDownlod (String url){
        this.url = url;

        String[] splittetUrl = url.split("=");
        this.videoId = splittetUrl[splittetUrl.length - 1];

        // init downloader with default config
        this.downloader = new YoutubeDownloader();
    }

    public VideoInfo requestVideoInfo(){
        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        return response.data();
    }

    public void downloadAudio(){
        isDownloading = true;
        VideoInfo info = requestVideoInfo();
        File outputDir = new File(ImportedFileHandler.FOLDER_PATH);
        Format format = info.bestAudioFormat();
        downloadProgress = 0;
        RequestVideoFileDownload request = new RequestVideoFileDownload(format)
                .saveTo(outputDir)
                .renameTo(info.details().title())
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
                        isDownloading = false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error: " + throwable.getLocalizedMessage());
                        isDownloading = false;
                    }
                })
                .async();
        downloader.downloadVideoFile(request);
    }
}
