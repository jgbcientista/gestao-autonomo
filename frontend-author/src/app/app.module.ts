import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { SessionMonitorModule } from './components/session-monitor/session-monitor.module';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { routes } from './app.routes';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    CommonModule,
    SessionMonitorModule,
    RouterModule.forRoot(routes),
    NgChartsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { } 