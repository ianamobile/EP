import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UiiaInvoicesComponent } from './uiia-invoices.component';

describe('UiiaInvoicesComponent', () => {
  let component: UiiaInvoicesComponent;
  let fixture: ComponentFixture<UiiaInvoicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UiiaInvoicesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UiiaInvoicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
