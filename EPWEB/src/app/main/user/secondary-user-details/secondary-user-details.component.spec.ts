import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecondaryUserDetailsComponent } from './secondary-user-details.component';

describe('SecondaryUserDetailsComponent', () => {
  let component: SecondaryUserDetailsComponent;
  let fixture: ComponentFixture<SecondaryUserDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SecondaryUserDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecondaryUserDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
