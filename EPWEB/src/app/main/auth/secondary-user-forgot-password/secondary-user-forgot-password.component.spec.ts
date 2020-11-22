import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecondaryUserForgotPasswordComponent } from './secondary-user-forgot-password.component';

describe('SecondaryUserForgotPasswordComponent', () => {
  let component: SecondaryUserForgotPasswordComponent;
  let fixture: ComponentFixture<SecondaryUserForgotPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SecondaryUserForgotPasswordComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecondaryUserForgotPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
