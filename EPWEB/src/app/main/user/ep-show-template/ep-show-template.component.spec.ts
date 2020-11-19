import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EpShowTemplateComponent } from './ep-show-template.component';

describe('EpShowTemplateComponent', () => {
  let component: EpShowTemplateComponent;
  let fixture: ComponentFixture<EpShowTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EpShowTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EpShowTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
