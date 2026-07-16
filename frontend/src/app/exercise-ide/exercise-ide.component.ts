
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';

import { ExerciseApiService } from '../core/exercise-api.service';
import { SubmissionApiService } from '../core/submission-api.service';
import {
  ExerciseDetail,
  HintView,
  ScratchRunResult,
  SolutionAnnotation,
  SolutionView,
  SubmissionResult
} from '../core/exercise.models';

interface DisplayCodeLine {
  text: string;
  indent: number;
}

interface DisplaySolutionAnnotation extends SolutionAnnotation {
  codeLines: DisplayCodeLine[];
}

@Component({
    selector: 'app-exercise-ide',
    imports: [FormsModule, RouterLink, MonacoEditorModule],
    templateUrl: './exercise-ide.component.html',
    styleUrl: './exercise-ide.component.scss'
})
export class ExerciseIdeComponent implements OnInit {
  exercise: ExerciseDetail | null = null;
  code = '';
  loading = true;
  loadError = '';

  running = false;
  submitting = false;

  runResult: ScratchRunResult | null = null;
  submissionResult: SubmissionResult | null = null;

  hints: HintView[] = [];
  loadingHint = false;

  solution: SolutionView | null = null;
  solutionCodeLines: DisplayCodeLine[] = [];
  solutionAnnotations: DisplaySolutionAnnotation[] = [];
  loadingSolution = false;
  solutionError = '';

  editorOptions = {
    theme: 'vs-dark',
    language: 'java',
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 14,
    wordWrap: 'on',
    wrappingIndent: 'same',
    wordWrapColumn: 88,
    scrollBeyondLastColumn: 0,
    lineNumbersMinChars: 3,
    // Monaco defaults to zero top padding, which glues line 1 to the container edge;
    // VS Code itself renders with breathing room above the first line.
    padding: { top: 16, bottom: 12 }
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly exerciseApi: ExerciseApiService,
    private readonly submissionApi: SubmissionApiService
  ) {}

  ngOnInit(): void {
    const exerciseId = this.route.snapshot.paramMap.get('exerciseId');
    if (!exerciseId) {
      this.loadError = 'Nenhum exercício informado na rota.';
      this.loading = false;
      return;
    }
    this.exerciseApi.getExercise(exerciseId).subscribe({
      next: (exercise) => {
        this.exercise = exercise;
        this.code = exercise.editorCode;
        this.loading = false;
      },
      error: () => {
        this.loadError = 'Não foi possível carregar o exercício.';
        this.loading = false;
      }
    });
  }

  runCode(): void {
    if (this.running) {
      return;
    }
    this.running = true;
    this.runResult = null;
    this.submissionApi.scratchRun(this.code).subscribe({
      next: (result) => {
        this.runResult = result;
        this.running = false;
      },
      error: () => {
        this.running = false;
      }
    });
  }

  submitCode(): void {
    if (!this.exercise || this.submitting) {
      return;
    }
    this.submitting = true;
    this.submissionResult = null;
    this.submissionApi.submit(this.exercise.exerciseId, this.code).subscribe({
      next: (result) => {
        this.submissionResult = result;
        this.submitting = false;
        if (this.exercise) {
          this.exercise.status = result.exerciseSolved ? 'SOLVED' : 'IN_PROGRESS';
        }
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  revealNextHint(): void {
    if (!this.exercise || this.loadingHint || this.hints.length >= this.exercise.hintCount) {
      return;
    }
    this.loadingHint = true;
    this.exerciseApi.getHint(this.exercise.exerciseId, this.hints.length).subscribe({
      next: (hint) => {
        this.hints = [...this.hints, hint];
        this.loadingHint = false;
      },
      error: () => {
        this.loadingHint = false;
      }
    });
  }

  get hasMoreHints(): boolean {
    return !!this.exercise && this.hints.length < this.exercise.hintCount;
  }

  revealSolution(): void {
    if (!this.exercise || this.loadingSolution || this.solution) {
      return;
    }
    this.loadingSolution = true;
    this.solutionError = '';
    this.exerciseApi.getSolution(this.exercise.exerciseId).subscribe({
      next: (solution) => {
        this.solution = solution;
        this.solutionCodeLines = this.toDisplayCodeLines(solution.solutionCode);
        this.solutionAnnotations = solution.annotations.map((annotation) => ({
          ...annotation,
          codeLines: this.toDisplayCodeLines(annotation.codeExcerpt)
        }));
        this.loadingSolution = false;
      },
      error: () => {
        this.solutionError = 'Não foi possível carregar a resposta deste exercício.';
        this.loadingSolution = false;
      }
    });
  }

  hideSolution(): void {
    this.solution = null;
    this.solutionCodeLines = [];
    this.solutionAnnotations = [];
    this.solutionError = '';
  }

  private toDisplayCodeLines(code: string): DisplayCodeLine[] {
    return code.replace(/\r\n?/g, '\n').split('\n').map((rawLine) => {
      const text = rawLine.replace(/\t/g, '    ');
      return {
        text,
        indent: text.length - text.trimStart().length
      };
    });
  }
}
