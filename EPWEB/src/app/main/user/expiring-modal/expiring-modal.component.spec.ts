import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpiringModalComponent } from './expiring-modal.component';

describe('ExpiringModalComponent', () => {
  let component: ExpiringModalComponent;
  let fixture: ComponentFixture<ExpiringModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExpiringModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExpiringModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
