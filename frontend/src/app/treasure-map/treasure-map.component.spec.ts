import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleExerciseSummary, ModuleSummary } from '../core/exercise.models';
import { BRAZIL_MAP_SOURCE, BRAZIL_MAP_STATES } from './brazil-map.data';
import { TreasureMapComponent } from './treasure-map.component';

describe('TreasureMapComponent', () => {
  let apiModules: ModuleSummary[];

  const exercise = (id: string, status: ModuleExerciseSummary['status']): ModuleExerciseSummary => ({
    exerciseId: id,
    title: id,
    difficulty: 'BÁSICO',
    estimatedMinutes: 20,
    sortOrder: 0,
    status
  });

  const module = (code: string, statuses: ModuleExerciseSummary['status'][]): ModuleSummary => ({
    moduleCode: code,
    title: code,
    paradigm: 'TEST',
    description: 'Test module',
    sortOrder: 0,
    exercises: statuses.map((status, index) => exercise(`${code}-${index}`, status)),
    solvedCount: statuses.filter((status) => status === 'SOLVED').length,
    totalCount: statuses.length
  });

  beforeEach(async () => {
    apiModules = [
      module('logic-shapes', ['NOT_STARTED', 'NOT_STARTED']),
      module('real-bank-from-scratch', ['NOT_STARTED', 'NOT_STARTED']),
      module('technical-interview-preparation', ['NOT_STARTED', 'NOT_STARTED'])
    ];

    await TestBed.configureTestingModule({
      imports: [TreasureMapComponent],
      providers: [
        provideRouter([]),
        { provide: ExerciseApiService, useValue: { getModules: () => of(apiModules) } }
      ]
    }).compileComponents();
  });

  it('starts in Acre and renders all 26 states plus the Federal District on official geometry', () => {
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    const stateButtons = fixture.nativeElement.querySelectorAll('.state-selector button');
    const statePaths = fixture.nativeElement.querySelectorAll('.state-shape');
    const stateLabels = fixture.nativeElement.querySelectorAll('.state-name');
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(component.stops.length).toBe(27);
    expect(new Set(component.stops.map((stop) => stop.code))).toEqual(new Set([
      'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG',
      'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
    ]));
    expect(component.currentStop.code).toBe('AC');
    expect(stateButtons.length).toBe(27);
    expect(statePaths.length).toBe(27);
    expect(stateLabels.length).toBe(27);
    expect(BRAZIL_MAP_STATES.length).toBe(27);
    expect(new Set(BRAZIL_MAP_STATES.map((state) => state.path)).size).toBe(27);
    expect(BRAZIL_MAP_STATES.every((state) => state.path.startsWith('M'))).toBeTrue();
    expect(BRAZIL_MAP_SOURCE).toContain('servicodados.ibge.gov.br');
    expect(text).toContain('não garante emprego');
  });

  it('matches the regional puzzle-map composition with wood, labels and the Brazilian flag', () => {
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    const root = fixture.nativeElement as HTMLElement;
    expect(root.querySelectorAll('[data-region="north"]').length).toBe(7);
    expect(root.querySelectorAll('[data-region="northeast"]').length).toBe(9);
    expect(root.querySelectorAll('[data-region="central-west"]').length).toBe(4);
    expect(root.querySelectorAll('[data-region="southeast"]').length).toBe(4);
    expect(root.querySelectorAll('[data-region="south"]').length).toBe(3);
    expect(root.querySelectorAll('.flag-inset').length).toBe(1);
    expect(root.querySelector('[data-state-label="AM"]')?.textContent).toContain('AMAZONAS - AM');
    expect(root.querySelector('[data-state-label="RS"]')?.textContent).toContain('DO SUL - RS');
    expect(root.querySelectorAll('.cloud, .destination, .compass, .state-stop').length).toBe(0);
  });

  it('does not use bank exercises to bypass the foundation journey', () => {
    apiModules[1] = module('real-bank-from-scratch', ['SOLVED', 'SOLVED']);
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.foundationSolved).toBe(0);
    expect(fixture.componentInstance.currentStop.code).toBe('AC');
    expect(fixture.componentInstance.reachedSaoPaulo).toBeFalse();
  });

  it('uses each official state geometry as an interactive map target', () => {
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    const saoPauloPath = fixture.nativeElement.querySelector('[data-state="SP"]') as SVGPathElement;
    saoPauloPath.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    fixture.detectChanges();

    expect(fixture.componentInstance.selectedStopCode).toBe('SP');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('São Paulo');
  });

  it('keeps the complete journey inside a mobile-width viewport', () => {
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();
    const page = fixture.nativeElement.querySelector('.treasure-page') as HTMLElement;
    const previousWidth = page.style.width;

    page.style.width = '390px';
    fixture.detectChanges();

    expect(page.scrollWidth).toBeLessThanOrEqual(page.clientWidth);
    expect(fixture.nativeElement.querySelectorAll('.state-selector button').length).toBe(27);
    page.style.width = previousWidth;
  });

  it('opens the Sao Paulo hub only after every foundation exercise is solved', () => {
    apiModules[0] = module('logic-shapes', ['SOLVED', 'SOLVED']);
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.reachedSaoPaulo).toBeTrue();
    expect(fixture.componentInstance.currentStop.code).toBe('SP');
    expect(fixture.componentInstance.finalReady).toBeFalse();
  });

  it('releases the fictional pitch only when the complete curriculum is solved', () => {
    apiModules = [
      module('logic-shapes', ['SOLVED']),
      module('real-bank-from-scratch', ['SOLVED']),
      module('technical-interview-preparation', ['SOLVED'])
    ];
    const fixture = TestBed.createComponent(TreasureMapComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.finalReady).toBeTrue();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('TESOURO LIBERADO');
  });
});
