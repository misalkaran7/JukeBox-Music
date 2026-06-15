import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';

interface Song {
  id: number;
  title: string;
  dna: string;
  mood: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  trackLibrary: Song[] = [];
  selectedSong: Song | null = null;
  secretMessage: string = '';
  extractedVaultText: string = '';
  searchQuery: string = ''; 

  // Fixed and explicitly targeted to your active Java Server port discovered in the terminal
  private backendUrl = 'http://localhost:8091/api';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadTracks();
  }

  // Getter method that filters tracks dynamically based on title matches
  get filteredTracks(): Song[] {
    if (!this.searchQuery.trim()) {
      return this.trackLibrary;
    }
    return this.trackLibrary.filter(song =>
      song.title.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  loadTracks() {
    this.http.get<Song[]>(`${this.backendUrl}/songs`).subscribe({
      next: (data) => this.trackLibrary = data,
      error: (err) => console.error('Error connecting to Java backend API:', err)
    });
  }

  // Tells the Java backend engine to play the target audio track
  playSong(song: Song) {
    this.selectedSong = song;
    this.http.get(`${this.backendUrl}/playback?action=play&id=${song.id}`).subscribe({
      error: (err) => console.error('Playback trigger failure:', err)
    });
  }

  // Tells the Java backend engine to pause execution
  pausePlayback() {
    this.http.get(`${this.backendUrl}/playback?action=pause`).subscribe({
      error: (err) => console.error('Pause trigger failure:', err)
    });
  }

  // Tells the Java backend engine to resume execution
  resumePlayback() {
    this.http.get(`${this.backendUrl}/playback?action=resume`).subscribe({
      error: (err) => console.error('Resume trigger failure:', err)
    });
  }

  // Tells the Java backend engine to halt current track playback references completely
  stopPlayback() {
    this.http.get(`${this.backendUrl}/playback?action=stop`).subscribe({
      error: (err) => console.error('Stop trigger failure:', err)
    });
  }

  // Steganography: Hides message using the Java POST handler
  injectMessage() {
    if (!this.selectedSong) {
      alert('⚠️ Please select a song from the directory first!');
      return;
    }
    if (!this.secretMessage.trim()) {
      alert('⚠️ Please type a message payload to hide!');
      return;
    }

    this.http.post(`${this.backendUrl}/stegano/hide?id=${this.selectedSong.id}`, this.secretMessage, { responseType: 'text' })
      .subscribe({
        next: () => {
          alert(`🔒 Secret payload safely injected into: ${this.selectedSong?.title}`);
          this.secretMessage = ''; 
        },
        error: (err) => console.error('Injection link bottleneck:', err)
      });
  }

  // Steganography: Extracts and displays hidden text strings
  extractMessage() {
    if (!this.selectedSong) {
      alert('⚠️ Please select a song from the directory first!');
      return;
    }

    this.http.get(`${this.backendUrl}/stegano/extract?id=${this.selectedSong.id}`, { responseType: 'text' })
      .subscribe({
        next: (data) => {
          this.extractedVaultText = data; 
        },
        error: (err) => console.error('Extraction link bottleneck:', err)
      });
  }
}