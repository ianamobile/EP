import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchSecondaryDialogComponent } from './search-secondary-dialog.component';

describe('SearchSecondaryDialogComponent', () => {
  let component: SearchSecondaryDialogComponent;
  let fixture: ComponentFixture<SearchSecondaryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchSecondaryDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchSecondaryDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
