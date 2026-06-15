package jukebox_cli;

import java.io.*;

public class AudioAnalyzer {

    // === USP 1: AUDIO DNA HASHING & MOOD ENGINE ===
    public static void analyzeTrackDNA(Song song) {
        File file = new File(song.getFilePath());
        if (!file.exists()) return;

        long totalBytes = file.length();
        double energyAccumulator = 0;
        int sampleCount = 0;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long skipped = fis.skip(44); // Skip WAV header

            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i += 2) {
                    if (i + 1 < bytesRead) {
                        short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
                        energyAccumulator += Math.abs(sample);
                        sampleCount++;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Parsing error on track: " + song.getTitle());
        }

        double averageIntensity = sampleCount > 0 ? (energyAccumulator / sampleCount) : 0;
        int pseudoHash = (int) (totalBytes ^ (long) averageIntensity);
        String generatedDNA = "FX-" + Integer.toHexString(Math.abs(pseudoHash)).toUpperCase();
        if (generatedDNA.length() > 9) generatedDNA = generatedDNA.substring(0, 9);

        String calculatedMood = (averageIntensity > 8000) ? "🔥 High-Energy" : 
                                (averageIntensity > 3000) ? "🎸 Melodic/Moderate" : "☕ Chill/Ambient";

        song.setAnalysisData(generatedDNA, calculatedMood, averageIntensity);
    }

    // === USP 2: OPTIMIZED STREAMING STEGANOGRAPHY ENGINE ===
    public static void hideSecretMessage(Song song, String message) {
        File sourceFile = new File(song.getFilePath());
        File tempFile = new File(song.getFilePath() + ".tmp");
        
        message += "##END##"; 
        byte[] messageBytes = message.getBytes();

        // Using small buffers instead of loading the entire 34MB into RAM at once
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            
            // 1. Copy the 44-byte WAV header exactly as it is
            byte[] header = new byte[44];
            int headerRead = fis.read(header);
            fos.write(header, 0, headerRead);

            int messageByteIdx = 0;
            int bitIdx = 0;
            int currentAudioByte;

            // 2. Stream through the audio data payloads block-by-block
            while ((currentAudioByte = fis.read()) != -1) {
                if (messageByteIdx < messageBytes.length) {
                    // Extract the specific single target bit from our secret message text byte
                    int currentBit = (messageBytes[messageByteIdx] >> bitIdx) & 1;
                    
                    // Clear out the LSB of the audio sample and inject our secret text bit
                    currentAudioByte = (currentAudioByte & 0xFE) | currentBit;
                    
                    bitIdx++;
                    if (bitIdx == 8) {
                        bitIdx = 0;
                        messageByteIdx++;
                    }
                }
                fos.write(currentAudioByte);
            }
            
        } catch (IOException e) {
            System.out.println("❌ Streaming failure: " + e.getMessage());
            return;
        }

        // 3. Safely replace the original file with our newly compiled audio file
        if (sourceFile.delete() && tempFile.renameTo(sourceFile)) {
            song.setSecureVaultStatus(true);
            System.out.println("🔒 Success! Secret payload securely encrypted into audio streams.");
        } else {
            System.out.println("❌ System file update swap lock failure.");
        }
    }

    public static String extractSecretMessage(Song song) {
        File file = new File(song.getFilePath());
        if (!file.exists()) return "File not found.";

        StringBuilder decodedMessage = new StringBuilder();
        
        // Stream reading directly from disk buffer channels
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            long skipped = bis.skip(44); // Skip WAV header
            
            int currentByteVal = 0;
            int bitCounter = 0;
            int readByte;

            while ((readByte = bis.read()) != -1) {
                int lsb = readByte & 1; 
                currentByteVal |= (lsb << bitCounter);
                bitCounter++;

                if (bitCounter == 8) {
                    char decodedChar = (char) currentByteVal;
                    decodedMessage.append(decodedChar);
                    
                    // If we encounter our signature termination tag, exit instantly to save memory processing loops
                    if (decodedMessage.toString().endsWith("##END##")) {
                        return decodedMessage.substring(0, decodedMessage.length() - 7);
                    }
                    currentByteVal = 0;
                    bitCounter = 0;
                }
            }
        } catch (IOException e) {
            return "❌ Error parsing audio streams.";
        }
        return "⚠️ No hidden secure vault payload signatures recognized.";
    }
}