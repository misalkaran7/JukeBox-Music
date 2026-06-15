package jukebox_cli;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {
    private Clip clip;
    private AudioInputStream activeStream; // Maintained reference so it doesn't close mid-track
    private long currentFrame;
    private String status = "stopped";
    private Song currentSong;

    public synchronized boolean play(Song song) {
        stop(); // Ensure any previous streams and lines are fully terminated
        currentFrame = 0;
        currentSong = song;

        try {
            // Open the audio stream reference cleanly without an auto-close block
            activeStream = openPlaybackStream(new File(song.getFilePath()));
            clip = AudioSystem.getClip();
            clip.open(activeStream);
            clip.start();
            status = "play";
            System.out.println("🎶 Streaming: " + song.getTitle());
            return true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("❌ Playback failure: " + e.getMessage());
            e.printStackTrace();
            cleanupResources();
            return false;
        }
    }

    private AudioInputStream openPlaybackStream(File audioFile) throws UnsupportedAudioFileException, IOException {
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioBytes));
        AudioFormat sourceFormat = sourceStream.getFormat();

        if (AudioFormat.Encoding.PCM_SIGNED.equals(sourceFormat.getEncoding())) {
            return sourceStream;
        }

        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);

        return AudioSystem.getAudioInputStream(decodedFormat, sourceStream);
    }

    public synchronized boolean pause() {
        if (status.equals("play") && clip != null) {
            currentFrame = clip.getMicrosecondPosition();
            clip.stop();
            status = "paused";
            System.out.println("⏸️ Paused.");
            return true;
        }
        return false;
    }

    public synchronized boolean resume() {
        if (status.equals("paused") && clip != null) {
            clip.setMicrosecondPosition(currentFrame);
            clip.start();
            status = "play";
            System.out.println("▶️ Resumed.");
            return true;
        }
        return false;
    }

    public synchronized boolean stop() {
        cleanupResources();
        System.out.println("⏹️ Audio engine reset complete.");
        return true;
    }

    // Helper method to dismantle hardware audio references sequentially
    private void cleanupResources() {
        try {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
            if (activeStream != null) {
                activeStream.close(); // Safely closes the data channel now that playback has halted
            }
        } catch (IOException e) {
            System.out.println("⚠️ Warning during audio hardware stream cleanup: " + e.getMessage());
        } finally {
            clip = null;
            activeStream = null;
            currentSong = null;
            currentFrame = 0;
            status = "stopped";
        }
    }

    public String getStatus() {
        return status;
    }
}