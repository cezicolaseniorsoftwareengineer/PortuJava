import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import JSZip from 'jszip';

interface FileRequirement {
  path: string;
  includes?: string[];
}

interface ProjectGate {
  phase: number;
  title: string;
  purpose: string;
  requirements: FileRequirement[];
}

interface GateResult extends ProjectGate {
  passed: boolean;
  missing: string[];
}

const STORAGE_KEY = 'portujava.bank-project-lab.v1';

const PROJECT_GATES: ProjectGate[] = [
  {
    phase: 0,
    title: 'Raiz e fronteiras do produto',
    purpose: 'A raiz descreve o produto; backend e frontend possuem seus próprios src.',
    requirements: [
      { path: 'README.md', includes: ['objetivo'] },
      { path: 'pom.xml', includes: ['modules'] },
      { path: 'mvnw', includes: ['MAVEN_PROJECTBASEDIR'] },
      { path: 'mvnw.cmd', includes: ['MAVEN_PROJECTBASEDIR'] },
      { path: '.mvn/wrapper/maven-wrapper.properties', includes: ['distributionUrl'] },
      { path: 'backend/pom.xml', includes: ['artifactId'] },
      { path: 'backend/src/main/java/com/portujava/bank/domain/package-info.java', includes: ['package'] },
      { path: 'backend/src/main/java/com/portujava/bank/application/package-info.java', includes: ['package'] },
      { path: 'backend/src/main/java/com/portujava/bank/ports/package-info.java', includes: ['package'] },
      { path: 'backend/src/main/java/com/portujava/bank/adapters/package-info.java', includes: ['package'] },
      { path: 'frontend/package.json', includes: ['scripts'] },
      { path: 'frontend/src/app/app.config.ts', includes: ['ApplicationConfig'] },
      { path: 'contracts/openapi/bank-api.yaml', includes: ['openapi'] },
      { path: 'infrastructure/docker/compose.yaml', includes: ['services'] },
      { path: 'docs/adr/0001-architecture.md', includes: ['decisão'] }
    ]
  },
  gate(3, 'Identidade e acesso', 'identity/SessionService.java', ['revoke', 'authorize'], 'identity/SessionServiceTest.java', ['replay']),
  gate(4, 'Autorização de pagamentos', 'authorization/PaymentPinGuard.java', ['attempt', 'lock'], 'authorization/PaymentPinGuardTest.java', ['concurrent']),
  gate(5, 'Passkeys', 'passkey/PasskeyService.java', ['challenge', 'revoke'], 'passkey/PasskeyServiceTest.java', ['replay']),
  gate(6, 'Ledger auditável', 'ledger/Ledger.java', ['BigDecimal', 'append'], 'ledger/LedgerInvariantTest.java', ['balance']),
  gate(7, 'Transferências internas', 'transfer/TransferService.java', ['lock', 'idempot'], 'transfer/TransferConcurrencyTest.java', ['concurrent']),
  gate(8, 'Pix por chave', 'pix/PixIntentService.java', ['idempot', 'receipt'], 'pix/PixIntentServiceTest.java', ['timeout']),
  gate(9, 'Pix QR e EMV', 'pix/qr/EmvParser.java', ['crc', 'hash'], 'pix/qr/EmvGoldenCorpusTest.java', ['tamper']),
  gate(10, 'BaaS e adapters', 'ports/PaymentGateway.java', ['interface'], 'adapters/payment/PaymentGatewayContractTest.java', ['timeout']),
  gate(11, 'Webhooks e settlement', 'webhook/WebhookInbox.java', ['signature', 'dedup'], 'webhook/WebhookInboxTest.java', ['duplicate']),
  gate(12, 'Reconciliação', 'reconciliation/ReconciliationService.java', ['drift', 'repair'], 'reconciliation/ReconciliationServiceTest.java', ['provider']),
  gate(13, 'Boleto e cobrança', 'charge/ChargeService.java', ['version', 'idempot'], 'charge/ChargeServiceTest.java', ['expired']),
  gate(14, 'Tesouraria', 'treasury/TreasuryService.java', ['reserve', 'backing'], 'treasury/TreasuryClosingTest.java', ['balance']),
  gate(15, 'KYC, LGPD e PLD/FT', 'compliance/PrivacyPolicy.java', ['consent', 'retention'], 'compliance/PrivacyPolicyTest.java', ['mask']),
  gate(16, 'Antifraude', 'fraud/FraudPolicy.java', ['reason', 'version'], 'fraud/FraudPolicyTest.java', ['velocity']),
  gate(17, 'Controles distribuídos', 'distributed/DistributedControl.java', ['lease', 'failClosed'], 'distributed/DistributedControlTest.java', ['outage']),
  {
    phase: 18,
    title: 'Operação bancária',
    purpose: 'SLO, telemetria, recuperação e runbook precisam existir antes do incidente.',
    requirements: [
      { path: 'infrastructure/observability/slo.yaml', includes: ['objective', 'reconciliation'] },
      { path: 'docs/runbooks/incident.md', includes: ['rollback', 'reconcile'] },
      { path: 'docs/runbooks/disaster-recovery.md', includes: ['RTO', 'RPO'] }
    ]
  },
  {
    phase: 19,
    title: 'Angular e PWA bancário',
    purpose: 'O cliente previne repetição acidental, mas o backend continua autoritativo.',
    requirements: [
      { path: 'frontend/src/app/features/payments/payment-confirmation.component.ts', includes: ['pending', 'idempotency'] },
      { path: 'frontend/src/app/features/payments/payment-confirmation.component.spec.ts', includes: ['double submit'] }
    ]
  },
  gate(20, 'IA financeira governada', 'assistant/AiAuthorityPolicy.java', ['allowlist', 'userIntent'], 'assistant/AiAuthorityPolicyTest.java', ['injection']),
  {
    phase: 21,
    title: 'Supply chain e entrega',
    purpose: 'A entrega precisa ser reproduzível, rastreável e reversível.',
    requirements: [
      { path: '.github/workflows/ci.yml', includes: ['mvn', 'npm', 'test'] },
      { path: 'docs/supply-chain/sbom.md', includes: ['SBOM'] },
      { path: 'docs/runbooks/rollback.md', includes: ['rollback'] }
    ]
  },
  {
    phase: 22,
    title: 'Defesa final do banco',
    purpose: 'Deploy, incidente, reconciliação e decisões devem deixar evidência verificável.',
    requirements: [
      { path: 'docs/final-defense/deployment.md', includes: ['URL', 'commit'] },
      { path: 'docs/final-defense/incident-report.md', includes: ['timeline', 'root cause'] },
      { path: 'docs/final-defense/reconciliation.md', includes: ['drift', '0.00'] },
      { path: 'docs/final-defense/technical-defense.md', includes: ['invariants', 'trade-offs', 'rollback'] }
    ]
  }
];

function gate(
  phase: number,
  title: string,
  source: string,
  sourceTokens: string[],
  test: string,
  testTokens: string[]
): ProjectGate {
  const javaRoot = 'backend/src/main/java/com/portujava/bank/';
  const testRoot = 'backend/src/test/java/com/portujava/bank/';
  return {
    phase,
    title,
    purpose: 'Implemente o incremento, o caso adversarial e conecte a evidência ao gate da fase.',
    requirements: [
      { path: javaRoot + source, includes: sourceTokens },
      { path: testRoot + test, includes: testTokens }
    ]
  };
}

@Component({
  selector: 'app-bank-project-lab',
  imports: [CommonModule, FormsModule],
  templateUrl: './bank-project-lab.component.html',
  styleUrl: './bank-project-lab.component.scss'
})
export class BankProjectLabComponent {
  files: Record<string, string> = this.loadFiles();
  selectedPath = '';
  newPath = '';
  pathError = '';
  gateResults: GateResult[] = this.evaluateGates();
  lastGateRun = '';

  get paths(): string[] {
    return Object.keys(this.files).sort((left, right) => left.localeCompare(right));
  }

  get selectedContent(): string {
    return this.selectedPath ? this.files[this.selectedPath] ?? '' : '';
  }

  set selectedContent(content: string) {
    if (!this.selectedPath) return;
    this.files = { ...this.files, [this.selectedPath]: content };
    this.persist();
  }

  get passedGateCount(): number {
    return this.gateResults.filter((result) => result.passed).length;
  }

  createFile(): void {
    const path = this.normalizePath(this.newPath);
    if (!path) {
      this.pathError = 'Informe um caminho relativo válido.';
      return;
    }
    if (!this.isSafePath(path)) {
      this.pathError = 'Use somente caminhos relativos dentro do projeto.';
      return;
    }
    if (Object.hasOwn(this.files, path)) {
      this.pathError = 'Esse arquivo já existe.';
      return;
    }
    this.files = { ...this.files, [path]: '' };
    this.selectedPath = path;
    this.newPath = '';
    this.pathError = '';
    this.persist();
  }

  select(path: string): void {
    this.selectedPath = path;
  }

  deleteSelected(): void {
    if (!this.selectedPath || !window.confirm(`Excluir ${this.selectedPath} do laboratório?`)) return;
    const next = { ...this.files };
    delete next[this.selectedPath];
    this.files = next;
    this.selectedPath = '';
    this.persist();
    this.runGates();
  }

  reset(): void {
    if (!window.confirm('Voltar ao repositório vazio e excluir todos os arquivos do laboratório?')) return;
    this.files = {};
    this.selectedPath = '';
    this.persist();
    this.runGates();
  }

  runGates(): void {
    this.gateResults = this.evaluateGates();
    this.lastGateRun = new Date().toLocaleString('pt-BR');
  }

  exportEvidence(): void {
    const snapshot = {
      exportedAt: new Date().toISOString(),
      format: 'portujava-bank-project-v1',
      gates: this.gateResults.map(({ phase, title, passed, missing }) => ({ phase, title, passed, missing })),
      files: this.files
    };
    const url = URL.createObjectURL(new Blob([JSON.stringify(snapshot, null, 2)], { type: 'application/json' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'portujava-bank-project-evidence.json';
    link.click();
    URL.revokeObjectURL(url);
  }

  async exportRepositoryZip(): Promise<void> {
    const zip = new JSZip();
    for (const [path, content] of Object.entries(this.files)) {
      if (this.isSafePath(path)) {
        zip.file(path, content);
      }
    }
    zip.file('.portujava/gate-results.json', JSON.stringify({
      exportedAt: new Date().toISOString(),
      gates: this.gateResults.map(({ phase, title, passed, missing }) => ({ phase, title, passed, missing }))
    }, null, 2));
    const archive = await zip.generateAsync({ type: 'blob', platform: 'UNIX' });
    const url = URL.createObjectURL(archive);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'bank-platform.zip';
    link.click();
    URL.revokeObjectURL(url);
  }

  private evaluateGates(): GateResult[] {
    return PROJECT_GATES.map((projectGate) => {
      const missing = projectGate.requirements.flatMap((requirement) => {
        const content = this.files[requirement.path];
        if (content === undefined) return [requirement.path];
        const missingTokens = (requirement.includes ?? []).filter((token) =>
          !content.toLocaleLowerCase('pt-BR').includes(token.toLocaleLowerCase('pt-BR'))
        );
        return missingTokens.map((token) => `${requirement.path} contém "${token}"`);
      });
      return { ...projectGate, passed: missing.length === 0, missing };
    });
  }

  private persist(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.files));
  }

  private loadFiles(): Record<string, string> {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (!stored) return {};
      const parsed: unknown = JSON.parse(stored);
      if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) return {};
      return Object.fromEntries(Object.entries(parsed).filter(([path, content]) =>
        typeof content === 'string' && this.isSafePath(path) && this.normalizePath(path) === path
      ));
    } catch {
      return {};
    }
  }

  private normalizePath(path: string): string {
    return path.trim().replaceAll('\\', '/').replace(/^\.\//, '').replace(/\/{2,}/g, '/');
  }

  private isSafePath(path: string): boolean {
    return path.length > 0
      && path.length <= 240
      && !path.startsWith('/')
      && !path.endsWith('/')
      && !path.split('/').includes('..')
      && !/[\u0000-\u001f<>:"|?*]/.test(path);
  }
}
