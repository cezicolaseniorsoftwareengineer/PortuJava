import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ExerciseDetail, HintView, ModuleSummary, SolutionView } from './exercise.models';

@Injectable({ providedIn: 'root' })
export class ExerciseApiService {
  private readonly base = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getModules(): Observable<ModuleSummary[]> {
    return this.http.get<ModuleSummary[]>(`${this.base}/api/modules`);
  }

  getExercise(exerciseId: string): Observable<ExerciseDetail> {
    return this.http.get<ExerciseDetail>(`${this.base}/api/exercises/${exerciseId}`);
  }

  getHint(exerciseId: string, index: number): Observable<HintView> {
    return this.http.get<HintView>(`${this.base}/api/exercises/${exerciseId}/hints/${index}`);
  }

  getSolution(exerciseId: string): Observable<SolutionView> {
    return this.http.get<SolutionView>(`${this.base}/api/exercises/${exerciseId}/solution`);
  }
}
