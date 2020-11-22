import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthlyBookWeeklySupplementComponent } from './monthly-book-weekly-supplement.component';

describe('MonthlyBookWeeklySupplementComponent', () => {
  let component: MonthlyBookWeeklySupplementComponent;
  let fixture: ComponentFixture<MonthlyBookWeeklySupplementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonthlyBookWeeklySupplementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthlyBookWeeklySupplementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
