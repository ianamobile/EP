import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EpRequirementsDialogComponent } from './ep-requirements-dialog.component';

describe('EpRequirementsDialogComponent', () => {
  let component: EpRequirementsDialogComponent;
  let fixture: ComponentFixture<EpRequirementsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EpRequirementsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EpRequirementsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
