import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';

import { ExerciseApiService } from '../core/exercise-api.service';
import { SubmissionApiService } from '../core/submission-api.service';
import { ExerciseIdeComponent } from './exercise-ide.component';

describe('ExerciseIdeComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExerciseIdeComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
        { provide: ExerciseApiService, useValue: {} },
        { provide: SubmissionApiService, useValue: {} }
      ]
    }).compileComponents();
  });

  it('wraps long Java lines instead of requiring horizontal scrolling', () => {
    const fixture = TestBed.createComponent(ExerciseIdeComponent);
    const options = fixture.componentInstance.editorOptions;

    expect(options.wordWrap).toBe('on');
    expect(options.minimap.enabled).toBeFalse();
    expect(options.scrollBeyondLastColumn).toBe(0);
  });
});
