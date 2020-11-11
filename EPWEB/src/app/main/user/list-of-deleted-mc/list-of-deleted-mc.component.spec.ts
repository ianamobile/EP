import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListOfDeletedMCComponent } from './list-of-deleted-mc.component';

describe('ListOfDeletedMCComponent', () => {
  let component: ListOfDeletedMCComponent;
  let fixture: ComponentFixture<ListOfDeletedMCComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListOfDeletedMCComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListOfDeletedMCComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
