import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleExerciseSummary, ModuleSummary } from '../core/exercise.models';

interface JourneyStop {
  code: string;
  name: string;
  x: number;
  y: number;
  focus: string;
  artifact: string;
}

const BANK_MODULE_CODE = 'real-bank-from-scratch';
const INTERVIEW_MODULE_CODE = 'technical-interview-preparation';
const FOUNDATION_MODULE_ORDER = [
  'distinguished-engineering-method',
  'oop-coffee-machine',
  'logic-shapes',
  'dsa-foundations',
  'event-driven-robot',
  'aop-decorator-simulation',
  'rule-engine-daily-routine'
];

@Component({
  selector: 'app-treasure-map',
  imports: [RouterLink],
  templateUrl: './treasure-map.component.html',
  styleUrl: './treasure-map.component.scss'
})
export class TreasureMapComponent implements OnInit {
  readonly stops: JourneyStop[] = [
    { code: 'AC', name: 'Acre', x: 108, y: 342, focus: 'Enquadrar o problema antes de escrever código', artifact: 'Problema, restrições e critério de sucesso' },
    { code: 'RO', name: 'Rondônia', x: 177, y: 326, focus: 'Encapsulamento e objetos com invariantes', artifact: 'Value object validado por testes' },
    { code: 'AM', name: 'Amazonas', x: 195, y: 205, focus: 'Orientação a objetos aplicada ao domínio', artifact: 'Modelo de domínio coeso' },
    { code: 'RR', name: 'Roraima', x: 235, y: 93, focus: 'Lógica, estado e transições explícitas', artifact: 'Máquina de estados mínima' },
    { code: 'AP', name: 'Amapá', x: 392, y: 112, focus: 'Recursão, busca e análise de complexidade', artifact: 'Algoritmo explicado e medido' },
    { code: 'PA', name: 'Pará', x: 340, y: 214, focus: 'Estruturas de dados escolhidas por contrato', artifact: 'Coleção adequada e casos de borda' },
    { code: 'TO', name: 'Tocantins', x: 371, y: 314, focus: 'Eventos, observadores e baixo acoplamento', artifact: 'Contrato de evento testável' },
    { code: 'MA', name: 'Maranhão', x: 455, y: 246, focus: 'Auditoria transversal sem contaminar o domínio', artifact: 'Decorator de auditoria' },
    { code: 'PI', name: 'Piauí', x: 465, y: 302, focus: 'Decisões determinísticas e tabelas de regras', artifact: 'Política com fallback seguro' },
    { code: 'CE', name: 'Ceará', x: 528, y: 292, focus: 'Falhas explícitas e exceções de domínio', artifact: 'Catálogo de erros recuperáveis' },
    { code: 'RN', name: 'Rio Grande do Norte', x: 563, y: 318, focus: 'Testes que provam comportamento', artifact: 'Regressão para happy path e bordas' },
    { code: 'PB', name: 'Paraíba', x: 548, y: 344, focus: 'Mudanças pequenas e verificáveis', artifact: 'Incremento revisável' },
    { code: 'PE', name: 'Pernambuco', x: 530, y: 365, focus: 'Contratos HTTP e versionamento', artifact: 'Contrato de API documentado' },
    { code: 'AL', name: 'Alagoas', x: 518, y: 390, focus: 'Persistência e evolução de esquema', artifact: 'Migração com rollback' },
    { code: 'SE', name: 'Sergipe', x: 508, y: 414, focus: 'Threat modeling e abuso previsível', artifact: 'Ameaças e controles objetivos' },
    { code: 'BA', name: 'Bahia', x: 452, y: 402, focus: 'Domínio isolado por portas e adaptadores', artifact: 'Fronteiras hexagonais' },
    { code: 'MT', name: 'Mato Grosso', x: 258, y: 352, focus: 'Dinheiro com precisão e moeda explícitas', artifact: 'Money sem ponto flutuante' },
    { code: 'MS', name: 'Mato Grosso do Sul', x: 287, y: 466, focus: 'Ledger balanceado e histórico imutável', artifact: 'Posting de dupla entrada' },
    { code: 'GO', name: 'Goiás', x: 342, y: 404, focus: 'Idempotência e concorrência', artifact: 'Operação repetível sem duplicidade' },
    { code: 'DF', name: 'Distrito Federal', x: 365, y: 390, focus: 'Decisões arquiteturais contestáveis', artifact: 'ADR com alternativas e evidência' },
    { code: 'MG', name: 'Minas Gerais', x: 397, y: 478, focus: 'Identidade, autorização e privacidade', artifact: 'Boundary de acesso e PII mascarada' },
    { code: 'ES', name: 'Espírito Santo', x: 477, y: 488, focus: 'Pagamentos como máquinas de estado', artifact: 'Fluxo financeiro idempotente' },
    { code: 'RJ', name: 'Rio de Janeiro', x: 438, y: 529, focus: 'Falha distribuída, retry e recuperação', artifact: 'Outbox, timeout e compensação' },
    { code: 'PR', name: 'Paraná', x: 350, y: 570, focus: 'SRE, deploy e observabilidade', artifact: 'SLO, runbook e rollback' },
    { code: 'SC', name: 'Santa Catarina', x: 365, y: 615, focus: 'Entrevistas por padrões, não memorização', artifact: 'Solução defendida por complexidade' },
    { code: 'RS', name: 'Rio Grande do Sul', x: 342, y: 672, focus: 'Defesa técnica e narrativa de produto', artifact: 'Demo baseada em evidências' },
    { code: 'SP', name: 'São Paulo', x: 382, y: 528, focus: 'Construir, publicar e defender outro banco', artifact: 'Banco, entrevistas e pitch final' }
  ];

  readonly confettiPieces = Array.from({ length: 16 });
  modules: ModuleSummary[] = [];
  loading = true;
  loadError = '';
  selectedStopCode = this.stops[0].code;

  constructor(private readonly exerciseApi: ExerciseApiService) {}

  ngOnInit(): void {
    this.exerciseApi.getModules().subscribe({
      next: (modules) => {
        this.modules = modules;
        this.selectedStopCode = this.currentStop.code;
        this.loading = false;
      },
      error: () => {
        this.loadError = 'Não foi possível carregar o progresso da expedição.';
        this.loading = false;
      }
    });
  }

  get routePoints(): string {
    return this.stops.map((stop) => `${stop.x},${stop.y}`).join(' ');
  }

  get selectedStop(): JourneyStop {
    return this.stops.find((stop) => stop.code === this.selectedStopCode) ?? this.stops[0];
  }

  get selectedStopIndex(): number {
    return this.stops.findIndex((stop) => stop.code === this.selectedStopCode);
  }

  get foundationModules(): ModuleSummary[] {
    return this.modules
      .filter((module) => module.moduleCode !== BANK_MODULE_CODE && module.moduleCode !== INTERVIEW_MODULE_CODE)
      .sort((left, right) => {
        const leftOrder = FOUNDATION_MODULE_ORDER.indexOf(left.moduleCode);
        const rightOrder = FOUNDATION_MODULE_ORDER.indexOf(right.moduleCode);
        return (leftOrder < 0 ? Number.MAX_SAFE_INTEGER : leftOrder) -
          (rightOrder < 0 ? Number.MAX_SAFE_INTEGER : rightOrder);
      });
  }

  get foundationTotal(): number {
    return this.foundationModules.reduce((total, module) => total + module.totalCount, 0);
  }

  get foundationSolved(): number {
    return this.foundationModules.reduce((total, module) => total + module.solvedCount, 0);
  }

  get foundationProgress(): number {
    return this.percentage(this.foundationSolved, this.foundationTotal);
  }

  get currentStopIndex(): number {
    let current = 0;
    for (let index = 1; index < this.stops.length; index += 1) {
      if (this.isStopUnlocked(index)) {
        current = index;
      }
    }
    return current;
  }

  get currentStop(): JourneyStop {
    return this.stops[this.currentStopIndex];
  }

  get completedRoutePercent(): number {
    return Math.round((this.currentStopIndex / (this.stops.length - 1)) * 100);
  }

  get exercisesToSaoPaulo(): number {
    return Math.max(0, this.foundationTotal - this.foundationSolved);
  }

  get bankModule(): ModuleSummary | undefined {
    return this.modules.find((module) => module.moduleCode === BANK_MODULE_CODE);
  }

  get interviewModule(): ModuleSummary | undefined {
    return this.modules.find((module) => module.moduleCode === INTERVIEW_MODULE_CODE);
  }

  get bankProgress(): number {
    return this.percentage(this.bankModule?.solvedCount ?? 0, this.bankModule?.totalCount ?? 0);
  }

  get interviewProgress(): number {
    return this.percentage(this.interviewModule?.solvedCount ?? 0, this.interviewModule?.totalCount ?? 0);
  }

  get totalExercises(): number {
    return this.modules.reduce((total, module) => total + module.totalCount, 0);
  }

  get solvedExercises(): number {
    return this.modules.reduce((total, module) => total + module.solvedCount, 0);
  }

  get finalReady(): boolean {
    return this.totalExercises > 0 && this.solvedExercises === this.totalExercises;
  }

  get reachedSaoPaulo(): boolean {
    return this.foundationTotal > 0 && this.foundationSolved === this.foundationTotal;
  }

  get selectedRecommendedModule(): ModuleSummary | undefined {
    if (this.foundationModules.length === 0) {
      return undefined;
    }
    const position = Math.min(
      this.foundationModules.length - 1,
      Math.floor((this.selectedStopIndex / (this.stops.length - 1)) * this.foundationModules.length)
    );
    return this.foundationModules[position];
  }

  get selectedRecommendedExercise(): ModuleExerciseSummary | undefined {
    const module = this.selectedRecommendedModule;
    return module?.exercises.find((exercise) => exercise.status !== 'SOLVED') ?? module?.exercises[0];
  }

  get nextInterviewExercise(): ModuleExerciseSummary | undefined {
    return this.interviewModule?.exercises.find((exercise) => exercise.status !== 'SOLVED');
  }

  selectStop(stop: JourneyStop): void {
    this.selectedStopCode = stop.code;
  }

  isStopUnlocked(index: number): boolean {
    return index === 0 || this.foundationSolved >= this.requiredExercisesForStop(index);
  }

  isStopCompleted(index: number): boolean {
    return index < this.currentStopIndex;
  }

  isCurrentStop(index: number): boolean {
    return index === this.currentStopIndex;
  }

  requiredExercisesForStop(index: number): number {
    if (this.foundationTotal === 0) {
      return index === 0 ? 0 : 1;
    }
    return Math.ceil((index / (this.stops.length - 1)) * this.foundationTotal);
  }

  remainingForStop(index: number): number {
    return Math.max(0, this.requiredExercisesForStop(index) - this.foundationSolved);
  }

  private percentage(solved: number, total: number): number {
    return total > 0 ? Math.round((solved / total) * 100) : 0;
  }
}
