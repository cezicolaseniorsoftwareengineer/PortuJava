import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ScratchRunResult, SubmissionResult } from './exercise.models';

@Injectable({ providedIn: 'root' })
export class SubmissionApiService {
  private readonly base = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  submit(exerciseId: string, code: string): Observable<SubmissionResult> {
    return this.http.post<SubmissionResult>(`${this.base}/api/submissions`, { exerciseId, code });
  }

  scratchRun(code: string): Observable<ScratchRunResult> {
    return this.http.post<ScratchRunResult>(`${this.base}/api/submissions/scratch-run`, { code });
  }
}
