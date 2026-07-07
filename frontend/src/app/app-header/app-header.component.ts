import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleSummary } from '../core/exercise.models';

/**
 * Persistent top bar (logo + track navigation), shown above every route via AppComponent. Fetches
 * the module list itself rather than reading it from ModuleListComponent, since it also needs to be
 * present - and populated - on the exercise IDE route, which never loads that data on its own.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.scss'
})
export class AppHeaderComponent implements OnInit {
  modules: ModuleSummary[] = [];
  menuOpen = false;

  constructor(private readonly exerciseApi: ExerciseApiService) {}

  ngOnInit(): void {
    this.exerciseApi.getModules().subscribe({
      next: (modules) => { this.modules = modules; },
      error: () => { /* header degrades to just the logo if the API call fails */ }
    });
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.menuOpen = false;
  }
}
