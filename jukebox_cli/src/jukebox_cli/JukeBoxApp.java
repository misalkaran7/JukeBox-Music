package jukebox_cli;

import java.util.*;

public class JukeBoxApp {
    private static ArrayList<Song> library = new ArrayList<>();
    private static LinkedList<Song> playQueue = new LinkedList<>();
    private static AudioPlayer player = new AudioPlayer();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        library = FolderReader.loadSongsFromFolder("music");

        String mainChoice = "";
        while (!mainChoice.equals("7")) {
            System.out.println("\n============================= CYBER-JUKEBOX INTERFACE =============================");
            System.out.println("1. View Track DNA Library    2. Add Track to Queue     3. Shuffle & Play Queue");
            System.out.println("4. Live Audio Player Menu    5. Sonic Smart DNA Match  6. Steganography Secure Vault");
            System.out.println("7. Exit Workstation");
            System.out.print("System Command: ");
            mainChoice = scanner.nextLine().trim();

            switch (mainChoice) {
                case "1": displayLibrary(); break;
                case "2": queueMenu(scanner); break;
                case "3": shuffleAndPlay(); break;
                case "4": playbackMenu(scanner); break;
                case "5": runSonicMatch(scanner); break;
                case "6": runSteganoVault(scanner); break;
                case "7": player.stop(); System.out.println("👋 Shutdown sequence finalized. Goodbye!"); break;
                default: System.out.println("❌ Invalid syntax command.");
            }
        }
        scanner.close();
    }

    private static void displayLibrary() {
        System.out.println("\n--- Decoded System Track Directory ---");
        for (int i = 0; i < library.size(); i++) {
            System.out.println("[" + i + "] " + library.get(i).toString());
        }
    }

    private static void queueMenu(Scanner scanner) {
        displayLibrary();
        System.out.print("\nSelect index target to buffer in pipeline queue: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx >= 0 && idx < library.size()) {
                playQueue.add(library.get(idx));
                System.out.println("➕ Buffered: " + library.get(idx).getTitle());
            }
        } catch (Exception e) { System.out.println("❌ Parsing Exception."); }
    }

    private static void shuffleAndPlay() {
        if (playQueue.isEmpty()) { System.out.println("⚠️ Queue buffer empty."); return; }
        Collections.shuffle(playQueue);
        System.out.println("🔀 Structural variations executed across queue pipelines.");
        player.play(playQueue.peek());
    }

    private static void runSonicMatch(Scanner scanner) {
        displayLibrary();
        System.out.print("\nSelect reference index trace for similarity processing: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx >= 0 && idx < library.size()) {
                Song target = library.get(idx);
                Song closestMatch = null;
                double minVariance = Double.MAX_VALUE;

                for (Song s : library) {
                    if (s == target) continue;
                    double var = Math.abs(s.getIntensity() - target.getIntensity());
                    if (var < minVariance) { minVariance = var; closestMatch = s; }
                }
                if (closestMatch != null) {
                    System.out.println("🎯 Match Sequence Locked: " + closestMatch.getTitle() + " [DNA Match]");
                } else { System.out.println("⚠️ Insufficient comparative tracks."); }
            }
        } catch (Exception e) { System.out.println("❌ Processing fault."); }
    }

    private static void runSteganoVault(Scanner scanner) {
        System.out.println("\n--- Steganographic Vault Hub ---");
        System.out.println("1. Inject Secret Payload into Song\n2. Extract/Decrypt Message from Song");
        System.out.print("Action Choice: ");
        String selection = scanner.nextLine().trim();

        displayLibrary();
        System.out.print("Target Song Index: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx < 0 || idx >= library.size()) return;
            Song selected = library.get(idx);

            if (selection.equals("1")) {
                System.out.print("Enter your secret password/message string: ");
                String secret = scanner.nextLine();
                AudioAnalyzer.hideSecretMessage(selected, secret);
            } else if (selection.equals("2")) {
                System.out.println("🔓 Processing extraction matrix payload...");
                String message = AudioAnalyzer.extractSecretMessage(selected);
                System.out.println("\n🔑 Revealed Secret Plaintext Payload: \n>>> " + message);
            }
        } catch (Exception e) { System.out.println("❌ Processing breakdown constraint."); }
    }

    private static void playbackMenu(Scanner scanner) {
        String action = "";
        while (!action.equals("back")) {
            System.out.println("\nLive Controls: [play] | [pause] | [resume] | [stop] | [back]");
            System.out.print("Control Console: ");
            action = scanner.nextLine().toLowerCase().trim();
            switch (action) {
                case "play": if (!playQueue.isEmpty()) player.play(playQueue.peek()); break;
                case "pause": player.pause(); break;
                case "resume": player.resume(); break;
                case "stop": player.stop(); break;
            }
        }
    }
}