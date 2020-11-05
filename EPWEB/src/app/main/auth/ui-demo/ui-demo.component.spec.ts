import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UiDemoComponent } from './ui-demo.component';

describe('UiDemoComponent', () => {
  let component: UiDemoComponent;
  let fixture: ComponentFixture<UiDemoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UiDemoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UiDemoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
