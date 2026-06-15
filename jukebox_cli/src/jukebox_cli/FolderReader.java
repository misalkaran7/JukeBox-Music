package jukebox_cli;

import java.io.File;
import java.util.ArrayList;

public class FolderReader {
    public static ArrayList<Song> loadSongsFromFolder(String folderName) {
        ArrayList<Song> localSongs = new ArrayList<>();
        String projectPath = System.getProperty("user.dir");
        
        // Strategy A: Primary directory search check (e.g., JukeBox_CLI/music)
        File musicFolder = new File(projectPath + File.separator + folderName);

        // Strategy B: Nested directory fallback check if Strategy A misses (e.g., JukeBox_CLI/jukebox_cli/music)
        if (!musicFolder.exists() || !musicFolder.isDirectory()) {
            musicFolder = new File(projectPath + File.separator + "jukebox_cli" + File.separator + folderName);
        }

        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File[] files = musicFolder.listFiles();
            if (files != null) {
                System.out.println("🧬 Processing Audio DNA Profiling sequences from: " + musicFolder.getAbsolutePath());
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".wav")) {
                        String title = file.getName().replace(".wav", ""); 
                        Song newSong = new Song(title, file.getAbsolutePath());
                        
                        // Automatically run DNA Profiler
                        AudioAnalyzer.analyzeTrackDNA(newSong);
                        localSongs.add(newSong);
                    }
                }
            }
        } else {
            System.out.println("❌ Directory configuration fault: Unable to locate 'music' folder anywhere at: " + projectPath);
        }
        return localSongs;
    }
}