import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeIntroComponent } from './code-intro.component';

describe('CodeIntroComponent', () => {
  let component: CodeIntroComponent;
  let fixture: ComponentFixture<CodeIntroComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CodeIntroComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeIntroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
