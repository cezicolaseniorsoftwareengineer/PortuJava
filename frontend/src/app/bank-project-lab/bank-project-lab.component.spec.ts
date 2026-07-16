import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BankProjectLabComponent } from './bank-project-lab.component';

describe('BankProjectLabComponent', () => {
  let fixture: ComponentFixture<BankProjectLabComponent>;
  let component: BankProjectLabComponent;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({ imports: [BankProjectLabComponent] }).compileComponents();
    fixture = TestBed.createComponent(BankProjectLabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('starts as an empty virtual repository', () => {
    expect(component.paths).toEqual([]);
    expect(component.passedGateCount).toBe(0);
  });

  it('normalizes a PowerShell path and persists its content', () => {
    component.newPath = '.\\README.md';
    component.createFile();
    component.selectedContent = '# Objetivo';

    expect(component.paths).toEqual(['README.md']);
    expect(JSON.parse(localStorage.getItem('portujava.bank-project-lab.v1') ?? '{}')['README.md'])
      .toBe('# Objetivo');
  });

  it('rejects paths that escape the virtual repository', () => {
    component.newPath = '../secret.txt';
    component.createFile();

    expect(component.paths).toEqual([]);
    expect(component.pathError).toContain('relativos');
  });

  it('discards a path-traversal entry injected into local storage', () => {
    localStorage.setItem('portujava.bank-project-lab.v1', JSON.stringify({
      '../../outside.txt': 'unsafe',
      'README.md': '# Objetivo'
    }));
    fixture.destroy();
    fixture = TestBed.createComponent(BankProjectLabComponent);
    component = fixture.componentInstance;

    expect(component.paths).toEqual(['README.md']);
  });
});
