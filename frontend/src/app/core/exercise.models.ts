export interface ExerciseDetail {
  exerciseId: string;
  title: string;
  statementMarkdown: string;
  codeContract: string;
  editorCode: string;
  difficulty: string;
  estimatedMinutes: number;
  moduleCode: string;
  moduleTitle: string;
  hintCount: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'SOLVED';
}

export interface HintView {
  index: number;
  text: string;
  hasMore: boolean;
}

export interface SolutionView {
  solutionCode: string;
  steps: string[];
}

export interface TestCaseFeedback {
  description: string;
  passed: boolean;
  hidden: boolean;
  actualValueSummary: string | null;
  exceptionSummary: string | null;
}

export interface SubmissionResult {
  compileSuccess: boolean;
  compileErrors: string[];
  executionTimedOut: boolean;
  totalTests: number;
  passedTests: number;
  allPassed: boolean;
  testResults: TestCaseFeedback[];
  exerciseSolved: boolean;
}

export interface ScratchRunResult {
  success: boolean;
  errors: string[];
  warnings: string[];
  output: string;
  timedOut: boolean;
  outputTruncated: boolean;
}

export interface ModuleExerciseSummary {
  exerciseId: string;
  title: string;
  difficulty: string;
  estimatedMinutes: number;
  sortOrder: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'SOLVED';
}

export interface ModuleSummary {
  moduleCode: string;
  title: string;
  paradigm: string;
  description: string;
  sortOrder: number;
  exercises: ModuleExerciseSummary[];
  solvedCount: number;
  totalCount: number;
}
