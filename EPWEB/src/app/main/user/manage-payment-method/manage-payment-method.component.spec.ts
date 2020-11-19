import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagePaymentMethodComponent } from './manage-payment-method.component';

describe('ManagePaymentMethodComponent', () => {
  let component: ManagePaymentMethodComponent;
  let fixture: ComponentFixture<ManagePaymentMethodComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManagePaymentMethodComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagePaymentMethodComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
