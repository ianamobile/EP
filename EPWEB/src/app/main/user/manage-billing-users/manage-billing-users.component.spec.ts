import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageBillingUsersComponent } from './manage-billing-users.component';

describe('ManageBillingUsersComponent', () => {
  let component: ManageBillingUsersComponent;
  let fixture: ComponentFixture<ManageBillingUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManageBillingUsersComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageBillingUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
