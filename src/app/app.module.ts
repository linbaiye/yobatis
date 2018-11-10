import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './material.module';
import { NavComponent } from './nav/nav.component';
import {RouterModule, Routes} from '@angular/router';
import { ConfigComponent } from './config/config.component';
import { InstallComponent } from './install/install.component';
import { CodeIntroComponent } from './code-intro/code-intro.component';
import { UsageComponent } from './usage/usage.component';

const appRoutes: Routes = [
  {path: 'config', component: ConfigComponent},
  {path: 'install', component: InstallComponent},
  {path: 'code-intro', component: CodeIntroComponent},
  {path: 'usage', component: UsageComponent},
  {path: '**', component: InstallComponent}
];



@NgModule({
  declarations: [
    AppComponent,
    NavComponent,
    ConfigComponent,
    InstallComponent,
    CodeIntroComponent,
    UsageComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule.forRoot(appRoutes),
    MaterialModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
