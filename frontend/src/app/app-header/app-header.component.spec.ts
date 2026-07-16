import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ExerciseApiService } from '../core/exercise-api.service';
import { AppHeaderComponent } from './app-header.component';

describe('AppHeaderComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppHeaderComponent],
      providers: [
        provideRouter([]),
        { provide: ExerciseApiService, useValue: { getModules: () => of([]), resetAllProgress: () => of(null) } }
      ]
    }).compileComponents();
  });

  it('exposes compact mobile navigation without losing accessible labels', () => {
    const fixture = TestBed.createComponent(AppHeaderComponent);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const projectLink = element.querySelector('.project-lab-link') as HTMLAnchorElement;
    const mapLink = element.querySelector('.treasure-map-link') as HTMLAnchorElement;
    const menuButton = element.querySelector('.menu-toggle') as HTMLButtonElement;

    expect(element.querySelector('.project-label-short')?.textContent?.trim()).toBe('Lab');
    expect(projectLink.getAttribute('aria-label')).toBe('Abrir Laboratório de Repositório');
    expect(mapLink.getAttribute('aria-label')).toBe('Abrir mapa do tesouro do PortuJava');
    expect(element.querySelector('.map-label-short')?.textContent?.trim()).toBe('Mapa');
    expect(menuButton.getAttribute('aria-controls')).toBe('tracks-menu-panel');
  });
});
