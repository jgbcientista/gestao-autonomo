import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { SessionMonitorComponent } from './session-monitor.component';

@NgModule({
  declarations: [
    SessionMonitorComponent
  ],
  imports: [
    CommonModule,
    NgChartsModule
  ],
  exports: [
    SessionMonitorComponent
  ]
})
export class SessionMonitorModule { } 