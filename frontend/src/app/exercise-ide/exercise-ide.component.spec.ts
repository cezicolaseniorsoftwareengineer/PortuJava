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

  it('renders the full solution and explanation excerpts with read-only Java Monaco editors', () => {
    const fixture = TestBed.createComponent(ExerciseIdeComponent);
    const component = fixture.componentInstance;
    const source = 'public final class Example {\n' +
      '    private static final java.util.List<String> VALUES = java.util.List.of("one", "two");\n' +
      '\n' +
      '    public boolean valid() { return true; }\n' +
      '}';

    component.loading = false;
    component.exercise = { exerciseId: 'example' } as never;
    component.solution = {
      solutionCode: source,
      steps: [],
      annotations: [{ codeExcerpt: 'return true;', explanation: 'Returns the decision.' }]
    };
    component.solutionAnnotations = [{
      codeExcerpt: 'return true;',
      explanation: 'Returns the decision.',
      editorHeight: 64
    }];
    fixture.detectChanges();

    const solutionEditor = fixture.nativeElement.querySelector('.solution-code-editor') as HTMLElement;
    const annotationEditor = fixture.nativeElement.querySelector('.annotation-code-editor') as HTMLElement;

    expect(solutionEditor).not.toBeNull();
    expect(annotationEditor).not.toBeNull();
    expect(component.solutionEditorOptions.theme).toBe(component.editorOptions.theme);
    expect(component.solutionEditorOptions.language).toBe('java');
    expect(component.solutionEditorOptions.readOnly).toBeTrue();
    expect(component.solutionEditorOptions.domReadOnly).toBeTrue();
    expect(component.annotationEditorOptions.theme).toBe(component.editorOptions.theme);
    expect(component.annotationEditorOptions.language).toBe('java');
    expect(component.annotationEditorOptions.readOnly).toBeTrue();
  });

  it('bounds read-only editor heights for short and long answers', () => {
    const component = TestBed.createComponent(ExerciseIdeComponent).componentInstance;
    const sizing = component as unknown as {
      calculateEditorHeight(
        code: string,
        minimum: number,
        maximum: number,
        lineHeight: number,
        verticalPadding: number
      ): number;
    };

    expect(sizing.calculateEditorHeight('return true;', 64, 200, 18, 20)).toBe(64);
    expect(sizing.calculateEditorHeight('line\n'.repeat(100), 220, 720, 21, 32)).toBe(720);
  });
});
