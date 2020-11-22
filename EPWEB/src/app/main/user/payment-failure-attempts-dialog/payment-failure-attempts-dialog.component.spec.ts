import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentFailureAttemptsDialogComponent } from './payment-failure-attempts-dialog.component';

describe('PaymentFailureAttemptsDialogComponent', () => {
  let component: PaymentFailureAttemptsDialogComponent;
  let fixture: ComponentFixture<PaymentFailureAttemptsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PaymentFailureAttemptsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentFailureAttemptsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
