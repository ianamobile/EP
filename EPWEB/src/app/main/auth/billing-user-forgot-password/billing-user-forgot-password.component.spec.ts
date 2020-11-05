import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BillingUserForgotPasswordComponent } from './billing-user-forgot-password.component';

describe('BillingUserForgotPasswordComponent', () => {
  let component: BillingUserForgotPasswordComponent;
  let fixture: ComponentFixture<BillingUserForgotPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BillingUserForgotPasswordComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BillingUserForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
