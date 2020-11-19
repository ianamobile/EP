import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EpTemplateComponent } from './ep-template.component';

describe('EpTemplateComponent', () => {
  let component: EpTemplateComponent;
  let fixture: ComponentFixture<EpTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EpTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EpTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
