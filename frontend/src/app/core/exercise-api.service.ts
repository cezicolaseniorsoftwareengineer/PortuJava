import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ExerciseDetail, HintView, ModuleSummary, SolutionView } from './exercise.models';

@Injectable({ providedIn: 'root' })
export class ExerciseApiService {
  constructor(private readonly http: HttpClient) {}

  getModules(): Observable<ModuleSummary[]> {
    return this.http.get<ModuleSummary[]>('/api/modules');
  }

  getExercise(exerciseId: string): Observable<ExerciseDetail> {
    return this.http.get<ExerciseDetail>(`/api/exercises/${exerciseId}`);
  }

  getHint(exerciseId: string, index: number): Observable<HintView> {
    return this.http.get<HintView>(`/api/exercises/${exerciseId}/hints/${index}`);
  }

  getSolution(exerciseId: string): Observable<SolutionView> {
    return this.http.get<SolutionView>(`/api/exercises/${exerciseId}/solution`);
  }
}
