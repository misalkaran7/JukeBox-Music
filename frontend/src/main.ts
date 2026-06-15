import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Bootstrapping the root standalone AppComponent with HTTP Client hooks enabled
bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error('Bootstrap failure on web runtime initialization:', err));