import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TerminalFeedLocationsComponent } from './terminal-feed-locations.component';

describe('TerminalFeedLocationsComponent', () => {
  let component: TerminalFeedLocationsComponent;
  let fixture: ComponentFixture<TerminalFeedLocationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TerminalFeedLocationsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TerminalFeedLocationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
