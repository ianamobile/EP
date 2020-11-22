import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddendumDetailsComponent } from './addendum-details.component';

describe('AddendumDetailsComponent', () => {
  let component: AddendumDetailsComponent;
  let fixture: ComponentFixture<AddendumDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddendumDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddendumDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
