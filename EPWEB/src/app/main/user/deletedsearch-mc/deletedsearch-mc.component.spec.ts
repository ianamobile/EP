import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeletedsearchMCComponent } from './deletedsearch-mc.component';

describe('DeletedsearchMCComponent', () => {
  let component: DeletedsearchMCComponent;
  let fixture: ComponentFixture<DeletedsearchMCComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeletedsearchMCComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeletedsearchMCComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
