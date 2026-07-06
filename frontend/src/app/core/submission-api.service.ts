import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ScratchRunResult, SubmissionResult } from './exercise.models';

@Injectable({ providedIn: 'root' })
export class SubmissionApiService {
  constructor(private readonly http: HttpClient) {}

  submit(exerciseId: string, code: string): Observable<SubmissionResult> {
    return this.http.post<SubmissionResult>('/api/submissions', { exerciseId, code });
  }

  scratchRun(code: string): Observable<ScratchRunResult> {
    return this.http.post<ScratchRunResult>('/api/submissions/scratch-run', { code });
  }
}
