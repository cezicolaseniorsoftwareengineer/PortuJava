import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ExerciseApiService } from '../core/exercise-api.service';
import { ModuleExerciseSummary, ModuleSummary } from '../core/exercise.models';
import {
  BRAZIL_MAP_SOURCE,
  BRAZIL_MAP_STATES,
  BRAZIL_MAP_VIEWBOX,
  BrazilMapState
} from './brazil-map.data';

interface JourneyStop {
  code: string;
  name: string;
  focus: string;
  artifact: string;
}

type BrazilRegion = 'north' | 'northeast' | 'central-west' | 'southeast' | 'south';

interface StateLabelLayout {
  readonly lines: readonly string[];
  readonly x?: number;
  readonly y?: number;
  readonly fontSize?: number;
}

interface MapStateLabel {
  readonly code: string;
  readonly lines: readonly string[];
  readonly x: number;
  readonly y: number;
  readonly fontSize: number;
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

const REGION_BY_STATE: Readonly<Record<string, BrazilRegion>> = {
  AC: 'north', AP: 'north', AM: 'north', PA: 'north', RO: 'north', RR: 'north', TO: 'north',
  AL: 'northeast', BA: 'northeast', CE: 'northeast', MA: 'northeast', PB: 'northeast',
  PE: 'northeast', PI: 'northeast', RN: 'northeast', SE: 'northeast',
  DF: 'central-west', GO: 'central-west', MS: 'central-west', MT: 'central-west',
  ES: 'southeast', MG: 'southeast', RJ: 'southeast', SP: 'southeast',
  PR: 'south', RS: 'south', SC: 'south'
};

const STATE_LABEL_LAYOUT: Readonly<Record<string, StateLabelLayout>> = {
  AC: { lines: ['ACRE - AC'], fontSize: 8 },
  AL: { lines: ['ALAGOAS', 'AL'], x: 596, y: 260, fontSize: 5 },
  AP: { lines: ['AMAPÁ', 'AP'], fontSize: 8 },
  AM: { lines: ['AMAZONAS - AM'], fontSize: 11 },
  BA: { lines: ['BAHIA - BA'], fontSize: 10 },
  CE: { lines: ['CEARÁ', 'CE'], fontSize: 7 },
  DF: { lines: ['DF'], x: 420, y: 355, fontSize: 4.5 },
  ES: { lines: ['ESPÍRITO SANTO', 'ES'], x: 541, y: 410, fontSize: 5.2 },
  GO: { lines: ['GOIÁS', 'GO'], fontSize: 8 },
  MA: { lines: ['MARANHÃO', 'MA'], fontSize: 8 },
  MT: { lines: ['MATO GROSSO - MT'], fontSize: 9 },
  MS: { lines: ['MATO GROSSO', 'DO SUL - MS'], fontSize: 8 },
  MG: { lines: ['MINAS GERAIS', 'MG'], fontSize: 9 },
  PA: { lines: ['PARÁ - PA'], fontSize: 11 },
  PB: { lines: ['PARAÍBA - PB'], x: 592, y: 224, fontSize: 5.2 },
  PR: { lines: ['PARANÁ', 'PR'], fontSize: 8 },
  PE: { lines: ['PERNAMBUCO - PE'], x: 571, y: 242, fontSize: 5.2 },
  PI: { lines: ['PIAUÍ - PI'], fontSize: 7.5 },
  RJ: { lines: ['RIO DE JANEIRO', 'RJ'], x: 512, y: 450, fontSize: 5.2 },
  RN: { lines: ['RIO GRANDE DO NORTE', 'RN'], x: 588, y: 201, fontSize: 4.8 },
  RS: { lines: ['RIO GRANDE', 'DO SUL - RS'], fontSize: 8 },
  RO: { lines: ['RONDÔNIA', 'RO'], fontSize: 8 },
  RR: { lines: ['RORAIMA', 'RR'], fontSize: 8 },
  SC: { lines: ['SANTA CATARINA', 'SC'], fontSize: 6.2 },
  SP: { lines: ['SÃO PAULO', 'SP'], fontSize: 7 },
  SE: { lines: ['SERGIPE', 'SE'], x: 586, y: 278, fontSize: 5 },
  TO: { lines: ['TOCANTINS', 'TO'], fontSize: 8 }
};

@Component({
  selector: 'app-treasure-map',
  imports: [RouterLink],
  templateUrl: './treasure-map.component.html',
  styleUrl: './treasure-map.component.scss'
})
export class TreasureMapComponent implements OnInit {
  readonly mapStates = BRAZIL_MAP_STATES;
  readonly mapViewBox = BRAZIL_MAP_VIEWBOX;
  readonly mapSource = BRAZIL_MAP_SOURCE;
  readonly stateLabels: readonly MapStateLabel[] = BRAZIL_MAP_STATES.map((state) => {
    const layout = STATE_LABEL_LAYOUT[state.code];
    if (!layout) {
      throw new Error(`Missing map label layout for ${state.code}`);
    }
    return {
      code: state.code,
      lines: layout.lines,
      x: layout.x ?? state.anchorX,
      y: layout.y ?? state.anchorY,
      fontSize: layout.fontSize ?? 8
    };
  });
  readonly stops: JourneyStop[] = [
    { code: 'AC', name: 'Acre', focus: 'Enquadrar o problema antes de escrever código', artifact: 'Problema, restrições e critério de sucesso' },
    { code: 'RO', name: 'Rondônia', focus: 'Encapsulamento e objetos com invariantes', artifact: 'Value object validado por testes' },
    { code: 'AM', name: 'Amazonas', focus: 'Orientação a objetos aplicada ao domínio', artifact: 'Modelo de domínio coeso' },
    { code: 'RR', name: 'Roraima', focus: 'Lógica, estado e transições explícitas', artifact: 'Máquina de estados mínima' },
    { code: 'AP', name: 'Amapá', focus: 'Recursão, busca e análise de complexidade', artifact: 'Algoritmo explicado e medido' },
    { code: 'PA', name: 'Pará', focus: 'Estruturas de dados escolhidas por contrato', artifact: 'Coleção adequada e casos de borda' },
    { code: 'TO', name: 'Tocantins', focus: 'Eventos, observadores e baixo acoplamento', artifact: 'Contrato de evento testável' },
    { code: 'MA', name: 'Maranhão', focus: 'Auditoria transversal sem contaminar o domínio', artifact: 'Decorator de auditoria' },
    { code: 'PI', name: 'Piauí', focus: 'Decisões determinísticas e tabelas de regras', artifact: 'Política com fallback seguro' },
    { code: 'CE', name: 'Ceará', focus: 'Falhas explícitas e exceções de domínio', artifact: 'Catálogo de erros recuperáveis' },
    { code: 'RN', name: 'Rio Grande do Norte', focus: 'Testes que provam comportamento', artifact: 'Regressão para happy path e bordas' },
    { code: 'PB', name: 'Paraíba', focus: 'Mudanças pequenas e verificáveis', artifact: 'Incremento revisável' },
    { code: 'PE', name: 'Pernambuco', focus: 'Contratos HTTP e versionamento', artifact: 'Contrato de API documentado' },
    { code: 'AL', name: 'Alagoas', focus: 'Persistência e evolução de esquema', artifact: 'Migração com rollback' },
    { code: 'SE', name: 'Sergipe', focus: 'Threat modeling e abuso previsível', artifact: 'Ameaças e controles objetivos' },
    { code: 'BA', name: 'Bahia', focus: 'Domínio isolado por portas e adaptadores', artifact: 'Fronteiras hexagonais' },
    { code: 'MT', name: 'Mato Grosso', focus: 'Dinheiro com precisão e moeda explícitas', artifact: 'Money sem ponto flutuante' },
    { code: 'MS', name: 'Mato Grosso do Sul', focus: 'Ledger balanceado e histórico imutável', artifact: 'Posting de dupla entrada' },
    { code: 'GO', name: 'Goiás', focus: 'Idempotência e concorrência', artifact: 'Operação repetível sem duplicidade' },
    { code: 'DF', name: 'Distrito Federal', focus: 'Decisões arquiteturais contestáveis', artifact: 'ADR com alternativas e evidência' },
    { code: 'MG', name: 'Minas Gerais', focus: 'Identidade, autorização e privacidade', artifact: 'Boundary de acesso e PII mascarada' },
    { code: 'ES', name: 'Espírito Santo', focus: 'Pagamentos como máquinas de estado', artifact: 'Fluxo financeiro idempotente' },
    { code: 'RJ', name: 'Rio de Janeiro', focus: 'Falha distribuída, retry e recuperação', artifact: 'Outbox, timeout e compensação' },
    { code: 'PR', name: 'Paraná', focus: 'SRE, deploy e observabilidade', artifact: 'SLO, runbook e rollback' },
    { code: 'SC', name: 'Santa Catarina', focus: 'Entrevistas por padrões, não memorização', artifact: 'Solução defendida por complexidade' },
    { code: 'RS', name: 'Rio Grande do Sul', focus: 'Defesa técnica e narrativa de produto', artifact: 'Demo baseada em evidências' },
    { code: 'SP', name: 'São Paulo', focus: 'Construir, publicar e defender outro banco', artifact: 'Banco, entrevistas e pitch final' }
  ];

  readonly confettiPieces = Array.from({ length: 16 });
  private readonly stopIndexByCode = new Map(this.stops.map((stop, index) => [stop.code, index]));
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

  selectMapState(state: BrazilMapState): void {
    const stopIndex = this.stopIndexByCode.get(state.code);
    if (stopIndex !== undefined) {
      this.selectStop(this.stops[stopIndex]);
    }
  }

  isMapStateUnlocked(state: BrazilMapState): boolean {
    const stopIndex = this.stopIndexByCode.get(state.code);
    return stopIndex !== undefined && this.isStopUnlocked(stopIndex);
  }

  isMapStateCompleted(state: BrazilMapState): boolean {
    const stopIndex = this.stopIndexByCode.get(state.code);
    return stopIndex !== undefined && this.isStopCompleted(stopIndex);
  }

  isMapStateCurrent(state: BrazilMapState): boolean {
    return state.code === this.currentStop.code;
  }

  regionFor(stateCode: string): BrazilRegion {
    const region = REGION_BY_STATE[stateCode];
    if (!region) {
      throw new Error(`Missing region for ${stateCode}`);
    }
    return region;
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
