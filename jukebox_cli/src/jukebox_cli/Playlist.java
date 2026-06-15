package jukebox_cli;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private ArrayList<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
        System.out.println("➕ Added \"" + song.getTitle() + "\" to playlist [" + name + "]");
    }

    public void removeSong(int index) {
        if (index >= 0 && index < songs.size()) {
            Song removed = songs.remove(index);
            System.out.println("❌ Removed \"" + removed.getTitle() + "\" from playlist [" + name + "]");
        } else {
            System.out.println("⚠️ Invalid song index.");
        }
    }

    public void displayPlaylist() {
        System.out.println("\n--- Playlist: " + name + " ---");
        if (songs.isEmpty()) {
            System.out.println("(No songs in this playlist)");
            return;
        }
        for (int i = 0; i < songs.size(); i++) {
            System.out.println("[" + i + "] " + songs.get(i).getTitle());
        }
    }
}