package jukebox_cli;

public class Song {
    private String title;
    private String filePath;
    private String audioDNA;   
    private String moodTag;   
    private double intensity; 
    private boolean hasHiddenMessage; 

    public Song(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
        this.audioDNA = "PENDING";
        this.moodTag = "Unknown";
        this.intensity = 0.0;
        this.hasHiddenMessage = false;
    }

    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getAudioDNA() { return audioDNA; }
    public String getMoodTag() { return moodTag; }
    public double getIntensity() { return intensity; }
    public boolean isSecureVault() { return hasHiddenMessage; }

    public void setAnalysisData(String audioDNA, String moodTag, double intensity) {
        this.audioDNA = audioDNA;
        this.moodTag = moodTag;
        this.intensity = intensity;
    }

    public void setSecureVaultStatus(boolean status) {
        this.hasHiddenMessage = status;
    }

    @Override
    public String toString() {
        return String.format("%s [DNA: %s | Mood: %s]%s", 
                title, audioDNA, moodTag, (hasHiddenMessage ? " 🔒 [Vault Active]" : ""));
    }
}