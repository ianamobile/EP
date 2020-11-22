import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecondaryUserLoginComponent } from './secondary-user-login.component';

describe('SecondaryUserLoginComponent', () => {
  let component: SecondaryUserLoginComponent;
  let fixture: ComponentFixture<SecondaryUserLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SecondaryUserLoginComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecondaryUserLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
