import { Component, EventEmitter, Input, Optional, Output, Self } from '@angular/core';
import { ControlValueAccessor, FormsModule, NgControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

type InputValue = string | number | null;

@Component({
  selector: 'app-form-input',
  imports: [FormsModule, MatFormFieldModule, MatInputModule],
  templateUrl: './form-input.component.html',
  styleUrls: ['./form-input.component.css'],
})
export class FormInputComponent implements ControlValueAccessor {
  @Input({ required: true }) label = '';
  @Input() type = 'text';
  @Input() placeholder = '';
  @Input() autocomplete = '';
  @Input() error = '';
  @Input() hint = '';
  @Input() rows: number | null = null;
  @Input() min: string | number | null = null;
  @Input() max: string | number | null = null;
  @Input() maxlength: string | number | null = null;
  @Input() fullWidth = true;

  @Output() fieldInput = new EventEmitter<Event>();
  @Output() fieldBlur = new EventEmitter<FocusEvent>();

  value: InputValue = '';
  disabled = false;

  private onChange: (value: InputValue) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor(@Optional() @Self() public ngControl: NgControl | null) {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }
  }

  get showError(): boolean {
    const control = this.ngControl?.control;
    return !!control && control.invalid && (control.touched || control.dirty) && !!this.error;
  }

  writeValue(value: InputValue): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: InputValue) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  updateValue(value: InputValue): void {
    this.value = value;
    this.onChange(value);
  }

  handleInput(event: Event): void {
    this.fieldInput.emit(event);
  }

  handleBlur(event: FocusEvent): void {
    this.onTouched();
    this.fieldBlur.emit(event);
  }
}
