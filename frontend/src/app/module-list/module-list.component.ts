
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleSummary } from '../core/exercise.models';

@Component({
    selector: 'app-module-list',
    imports: [RouterLink],
    templateUrl: './module-list.component.html',
    styleUrl: './module-list.component.scss'
})
export class ModuleListComponent implements OnInit {
  modules: ModuleSummary[] = [];
  loading = true;

  constructor(private readonly exerciseApi: ExerciseApiService) {}

  ngOnInit(): void {
    this.exerciseApi.getModules().subscribe({
      next: (modules) => {
        this.modules = modules;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get totalModuleCount(): number {
    return this.modules.length;
  }

  get totalExerciseCount(): number {
    return this.modules.reduce((total, module) => total + module.totalCount, 0);
  }

  get solvedExerciseCount(): number {
    return this.modules.reduce((total, module) => total + module.solvedCount, 0);
  }
}
