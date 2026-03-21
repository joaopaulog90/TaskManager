import { bootstrapApplication } from '@angular/platform-browser';
import { configuracaoApp } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, configuracaoApp)
  .catch((err) => console.error(err));
