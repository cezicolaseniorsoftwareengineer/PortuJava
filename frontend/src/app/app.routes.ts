import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./module-list/module-list.component').then((m) => m.ModuleListComponent)
  },
  {
    path: 'exercicios/:exerciseId',
    loadComponent: () =>
      import('./exercise-ide/exercise-ide.component').then((m) => m.ExerciseIdeComponent)
  },
  {
    path: 'laboratorio-repositorio',
    loadComponent: () =>
      import('./bank-project-lab/bank-project-lab.component').then((m) => m.BankProjectLabComponent)
  },
  {
    path: 'mapa-do-tesouro',
    loadComponent: () =>
      import('./treasure-map/treasure-map.component').then((m) => m.TreasureMapComponent)
  }
];
