import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecondaryUserFormDialogComponent } from './secondary-user-form-dialog.component';

describe('SecondaryUserFormDialogComponent', () => {
  let component: SecondaryUserFormDialogComponent;
  let fixture: ComponentFixture<SecondaryUserFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SecondaryUserFormDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SecondaryUserFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
