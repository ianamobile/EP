import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EpSearchTemplateDialogComponent } from './ep-search-template-dialog.component';

describe('EpSearchTemplateDialogComponent', () => {
  let component: EpSearchTemplateDialogComponent;
  let fixture: ComponentFixture<EpSearchTemplateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EpSearchTemplateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EpSearchTemplateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
