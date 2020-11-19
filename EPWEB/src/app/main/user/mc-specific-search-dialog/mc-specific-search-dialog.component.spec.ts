import { ComponentFixture, TestBed } from '@angular/core/testing';

import { McSpecificSearchDialogComponent } from './mc-specific-search-dialog.component';

describe('McSpecificSearchDialogComponent', () => {
  let component: McSpecificSearchDialogComponent;
  let fixture: ComponentFixture<McSpecificSearchDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ McSpecificSearchDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(McSpecificSearchDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
