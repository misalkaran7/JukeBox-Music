package jukebox_cli;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class JukeboxHttpServer {
    private static final int PORT = 8091;
    private static ArrayList<Song> library = new ArrayList<>();
    private static AudioPlayer player = new AudioPlayer();

    public static void main(String[] args) throws Exception {
        // Load the local .wav tracks dynamically from your music folder
        library = FolderReader.loadSongsFromFolder("music");
        
        // Initialize HTTP Server on port 8091
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // 1. API Route: Fetch all songs with their DNA and Mood
        server.createContext("/api/songs", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                setCorsHeaders(exchange);
                
                // Build a clean JSON string manually from our library array
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < library.size(); i++) {
                    Song s = library.get(i);
                    json.append(String.format(
                        "{\"id\":%d,\"title\":\"%s\",\"dna\":\"%s\",\"mood\":\"%s\",\"streamUrl\":\"/api/stream?id=%d\"}",
                        i, 
                        s.getTitle().replace("\"", "\\\""), 
                        s.getAudioDNA(), 
                        s.getMoodTag(),
                        i
                    ));
                    if (i < library.size() - 1) json.append(",");
                }
                json.append("]");

                byte[] response = json.toString().getBytes("UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });

        // 2. API Route: Play, Pause, Resume, and Stop controls
        server.createContext("/api/playback", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                setCorsHeaders(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    byte[] response = "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(405, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                    return;
                }

                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                String action = params.getOrDefault("action", "");
                String message;
                int statusCode = 200;

                switch (action) {
                    case "play": {
                        String idValue = params.get("id");
                        if (idValue == null) {
                            message = "missing id";
                            statusCode = 400;
                            break;
                        }

                        try {
                            int id = Integer.parseInt(idValue);
                            if (id < 0 || id >= library.size()) {
                                message = "invalid id";
                                statusCode = 400;
                                break;
                            }

                            if (player.play(library.get(id))) {
                                message = "playing";
                            } else {
                                message = "playback failed";
                                statusCode = 500;
                            }
                        } catch (NumberFormatException ex) {
                            message = "invalid id";
                            statusCode = 400;
                        }
                        break;
                    }
                    case "pause":
                        if (player.pause()) {
                            message = "paused";
                        } else {
                            message = "nothing to pause";
                            statusCode = 409;
                        }
                        break;
                    case "resume":
                        if (player.resume()) {
                            message = "resumed";
                        } else {
                            message = "nothing to resume";
                            statusCode = 409;
                        }
                        break;
                    case "stop":
                        player.stop();
                        message = "stopped";
                        break;
                    default:
                        message = "unknown action";
                        statusCode = 400;
                }

                byte[] response = ("{\"status\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(statusCode, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });

        server.createContext("/api/stream", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                setCorsHeaders(exchange);

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJsonResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
                    return;
                }

                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                String idValue = params.get("id");
                if (idValue == null) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"missing id\"}");
                    return;
                }

                try {
                    int id = Integer.parseInt(idValue);
                    if (id < 0 || id >= library.size()) {
                        sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"invalid id\"}");
                        return;
                    }

                    File audioFile = new File(library.get(id).getFilePath());
                    if (!audioFile.exists()) {
                        sendJsonResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"file not found\"}");
                        return;
                    }

                    byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
                    String rangeHeader = exchange.getRequestHeaders().getFirst("Range");
                    exchange.getResponseHeaders().set("Content-Type", "audio/wav");
                    exchange.getResponseHeaders().set("Accept-Ranges", "bytes");

                    if (rangeHeader == null || rangeHeader.isBlank()) {
                        exchange.sendResponseHeaders(200, audioBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(audioBytes);
                        }
                        return;
                    }

                    long totalLength = audioBytes.length;
                    long start = 0;
                    long end = totalLength - 1;

                    if (!rangeHeader.startsWith("bytes=")) {
                        exchange.sendResponseHeaders(416, -1);
                        return;
                    }

                    String[] rangeParts = rangeHeader.substring(6).split("-", 2);
                    if (!rangeParts[0].isBlank()) {
                        start = Long.parseLong(rangeParts[0].trim());
                    }
                    if (rangeParts.length > 1 && !rangeParts[1].isBlank()) {
                        end = Long.parseLong(rangeParts[1].trim());
                    }

                    if (start < 0 || end < start || start >= totalLength) {
                        exchange.sendResponseHeaders(416, -1);
                        return;
                    }

                    if (end >= totalLength) {
                        end = totalLength - 1;
                    }

                    int startIndex = (int) start;
                    int endIndex = (int) end;
                    int contentLength = endIndex - startIndex + 1;

                    exchange.getResponseHeaders().set("Content-Range", "bytes " + start + "-" + end + "/" + totalLength);
                    exchange.sendResponseHeaders(206, contentLength);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(audioBytes, startIndex, contentLength);
                    }
                } catch (NumberFormatException ex) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"invalid id\"}");
                }
            }
        });

        // 3. API Route: Inject/Hide messages (LSB Steganography)
        server.createContext("/api/stegano/hide", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                setCorsHeaders(exchange);
                
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    String query = exchange.getRequestURI().getQuery();
                    int id = Integer.parseInt(query.split("id=")[1].trim());
                    
                    String secretMessage = sb.toString();
                    AudioAnalyzer.hideSecretMessage(library.get(id), secretMessage);
                    
                    byte[] response = "{\"status\":\"encrypted\"}".getBytes();
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
                exchange.getResponseBody().close();
            }
        });

        // 4. API Route: Extract/Decrypt hidden messages
        server.createContext("/api/stegano/extract", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                setCorsHeaders(exchange);
                String query = exchange.getRequestURI().getQuery();
                
                String extractedText = "⚠️ No message found.";
                if (query != null && query.contains("id=")) {
                    int id = Integer.parseInt(query.split("id=")[1].trim());
                    extractedText = AudioAnalyzer.extractSecretMessage(library.get(id));
                }
                
                byte[] response = extractedText.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });

        System.out.println("🚀 Cyber-Jukebox REST API Server successfully initiated on http://localhost:" + PORT);
        server.start();
    }

    // Handles CORS configurations so the Angular frontend can make secure HTTP requests
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0]);
            String value = parts.length > 1 ? urlDecode(parts[1]) : "";
            params.put(key, value);
        }

        return params;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String payload) throws IOException {
        byte[] response = payload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}