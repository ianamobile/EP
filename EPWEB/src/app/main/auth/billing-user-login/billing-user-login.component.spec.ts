import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BillingUserLoginComponent } from './billing-user-login.component';

describe('BillingUserLoginComponent', () => {
  let component: BillingUserLoginComponent;
  let fixture: ComponentFixture<BillingUserLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BillingUserLoginComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BillingUserLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
