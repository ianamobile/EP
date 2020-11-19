import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MCLookupComponent } from './mclookup.component';

describe('MCLookupComponent', () => {
  let component: MCLookupComponent;
  let fixture: ComponentFixture<MCLookupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MCLookupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MCLookupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
