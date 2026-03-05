import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RelatoriosComponent } from './relatorios.component';

describe('RelatoriosComponent', () => {
  let component: RelatoriosComponent;
  let fixture: ComponentFixture<RelatoriosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RelatoriosComponent, HttpClientTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(RelatoriosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default to trust-score tab', () => {
    expect(component.activeTab).toBe('trust-score');
  });

  it('should switch tabs', () => {
    component.setTab('blockchain');
    expect(component.activeTab).toBe('blockchain');
  });
});
