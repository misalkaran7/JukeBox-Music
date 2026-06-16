# Cyber-Jukebox Web Workstation & Steganographic Vault

A high-performance, full-stack audio streaming and security workstation. This application couples a native Java REST API backend with a modern, glassmorphic Angular frontend, allowing users to stream local uncompressed WAV audio tracks while using Least Significant Bit (LSB) steganography to securely hide and extract encrypted plaintext fragments within the audio frames.

---

## 🚀 Features

* **Audio DNA Profiling & Streaming:** Scans a local directory, decodes audio files dynamically via a custom Java audio engine, and exposes control handles (Play, Pause, Resume, Stop) over local REST endpoints.
* **Dynamic Title Filtering:** Real-time search processing on the frontend to parse and narrow down track profiles instantly.
* **Steganographic Secure Vault:** Implements LSB injection algorithms to embed secret plaintext data directly into raw WAV frame channels and decrypt them cleanly on demand.
* **Modern Immersive UI:** A smooth glassmorphic interface with rounded components, glow accents, and backdrop blurs to break away from blocky, retro console styles.

---

## 🛠️ Tech Stack

* **Backend:** Java SE, `com.sun.net.httpserver.HttpServer`, `javax.sound.sampled`
* **Frontend:** Angular (v17+ Standalone), TypeScript, HTML5, CSS3 (Glassmorphism)
* **Communication:** REST HTTP Pipeline (CORS-enabled)

---

## 📂 Project Structure

```text
JukeBox_CLI/
├── jukebox_cli/              # Java Backend Repository
│   ├── src/
│   │   └── jukebox_cli/
│   │       ├── JukeboxHttpServer.java   # REST API Entry Point (Port 8091)
│   │       ├── AudioPlayer.java         # Native Clip Thread Controls
│   │       ├── FolderReader.java        # Path Resolution & Tracking File Scan
│   │       ├── Song.java                # Audio Model Entity
│   │       └── AudioAnalyzer.java       # DNA Profiling & Steganography Engine
│   └── music/                # Local Directory for .wav Audio Tracks
└── frontend/                 # Angular Client Web Application
    └── src/
        └── app/
            ├── app.component.ts         # Filtering, Logic Streams & API Bindings
            ├── app.component.html       # Cyber Deck Interface Layout
            └── app.component.css        # Neon-Aura Stylesheet

```

---

## 🏃‍♂️ Getting Started

### 1. Prerequisites

* Java Development Kit (JDK 17 or higher)
* Node.js & npm (Latest LTS)
* Angular CLI

### 2. Setup & Run Backend (Java)

1. Drop your standard 16-bit PCM `.wav` audio tracks into the `JukeBox_CLI/jukebox_cli/music/` directory.
2. Open the project root in your terminal and compile/run the server instance:
```bash
java -cp "jukebox_cli/bin;jukebox_cli/src" jukebox_cli.JukeboxHttpServer

```


3. Verify the console outputs:
`🚀 Cyber-Jukebox REST API Server successfully initiated on http://localhost:8091`

### 3. Setup & Run Frontend (Angular)

1. Open a separate terminal split window and navigate into the frontend application directory:
```bash
cd frontend

```


2. Install the required Node packages:
```bash
npm install

```


3. Start the local development web runner:
```bash
npm start

```


4. Open your web browser and go to: **`http://localhost:4200`**

---

## 🔒 Security Workflow (Steganography)

1. Select a track from the **Decoded System Track Directory**.
2. Input your secret text payload inside the text area in the **Secure Vault** panel.
3. Click **🔒 Inject Message Payload** to lock the plaintext inside the audio frame bits.
4. Click **🔓 Decrypt & Extract Message** to cleanly pull the hidden data back onto the UI dashboard output block.
