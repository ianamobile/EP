import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationSearchDialogComponent } from './notification-search-dialog.component';

describe('NotificationSearchDialogComponent', () => {
  let component: NotificationSearchDialogComponent;
  let fixture: ComponentFixture<NotificationSearchDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NotificationSearchDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotificationSearchDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
