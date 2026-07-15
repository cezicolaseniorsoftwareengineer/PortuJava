import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleSummary } from '../core/exercise.models';
import { ModuleListComponent } from './module-list.component';

describe('ModuleListComponent', () => {
  const modules: ModuleSummary[] = [
    {
      moduleCode: 'fundamentals',
      title: 'Fundamentals',
      paradigm: 'JAVA',
      description: 'Foundation track',
      sortOrder: 1,
      exercises: [],
      solvedCount: 2,
      totalCount: 3
    },
    {
      moduleCode: 'real-bank-from-scratch',
      title: 'Construindo um banco inteiro sozinho Real',
      paradigm: 'BANKING',
      description: 'Real banking track',
      sortOrder: 2,
      exercises: [],
      solvedCount: 1,
      totalCount: 2
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleListComponent],
      providers: [
        provideRouter([]),
        {
          provide: ExerciseApiService,
          useValue: { getModules: () => of(modules) }
        }
      ]
    }).compileComponents();
  });

  it('derives curriculum totals from the API instead of hardcoding them', () => {
    const fixture = TestBed.createComponent(ModuleListComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(component.totalModuleCount).toBe(2);
    expect(component.totalExerciseCount).toBe(5);
    expect(component.solvedExerciseCount).toBe(3);
    expect(text).toContain('2 trilhas, 5 exercícios');
    expect(text).toContain('Construindo um banco inteiro sozinho Real');
  });
});
