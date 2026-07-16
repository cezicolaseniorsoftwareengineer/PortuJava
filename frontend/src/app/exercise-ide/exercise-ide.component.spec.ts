import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideMonacoEditor } from 'ngx-monaco-editor-v2';

import { ExerciseApiService } from '../core/exercise-api.service';
import { SubmissionApiService } from '../core/submission-api.service';
import { ExerciseIdeComponent } from './exercise-ide.component';

describe('ExerciseIdeComponent', () => {
  beforeEach(async () => {
    const monacoRequire = Object.assign(() => undefined, { config: () => undefined });

    await TestBed.configureTestingModule({
      imports: [ExerciseIdeComponent],
      providers: [
        provideRouter([]),
        provideMonacoEditor({ monacoRequire }),
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

  it('preserves paragraphs and hanging indentation in revealed solution code', () => {
    const fixture = TestBed.createComponent(ExerciseIdeComponent);
    const component = fixture.componentInstance;
    const source = 'public final class Example {\n' +
      '    private static final java.util.List<String> VALUES = java.util.List.of("one", "two");\n' +
      '\n' +
      '    public boolean valid() { return true; }\n' +
      '}';
    const display = component as unknown as {
      toDisplayCodeLines(code: string): Array<{ text: string; indent: number }>;
    };

    component.loading = false;
    component.exercise = { exerciseId: 'example' } as never;
    component.solution = { solutionCode: source, steps: [], annotations: [] };
    component.solutionCodeLines = display.toDisplayCodeLines(source);
    fixture.detectChanges();

    const lines = fixture.nativeElement.querySelectorAll('.solution-code .code-line') as NodeListOf<HTMLElement>;
    const solutionCode = fixture.nativeElement.querySelector('.solution-code') as HTMLElement;
    solutionCode.style.width = '220px';
    solutionCode.style.maxWidth = '220px';

    expect(lines.length).toBe(5);
    expect(lines[1].textContent).toBe('    private static final java.util.List<String> VALUES = java.util.List.of("one", "two");');
    expect(lines[1].style.paddingLeft).toBe('4ch');
    expect(lines[1].style.textIndent).toBe('-4ch');
    expect(lines[2].textContent).toBe('');
    expect(getComputedStyle(lines[1]).wordBreak).toBe('normal');
    expect(solutionCode.scrollWidth).toBeLessThanOrEqual(solutionCode.clientWidth);
  });
});
