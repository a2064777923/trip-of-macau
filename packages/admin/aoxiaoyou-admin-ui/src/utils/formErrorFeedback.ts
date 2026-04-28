import type { FormInstance } from 'antd';
import type { NamePath } from 'antd/es/form/interface';
import type { ValidateErrorEntity } from 'rc-field-form/lib/interface';

function toNamePathArray(name: NamePath) {
  return Array.isArray(name) ? name : [name];
}

function buildFieldId(formName: string, name: NamePath) {
  return [formName, ...toNamePathArray(name)].join('_');
}

export function focusFirstInvalidField(
  form: FormInstance,
  formName: string,
  error: unknown,
) {
  const validationError = error as ValidateErrorEntity<unknown> | undefined;
  const firstError = validationError?.errorFields?.[0];

  if (!firstError?.name?.length) {
    return false;
  }

  form.scrollToField(firstError.name, { block: 'center' });

  window.setTimeout(() => {
    const fieldId = buildFieldId(formName, firstError.name);
    const input = document.getElementById(fieldId);
    const formItem = input?.closest('.ant-form-item') as HTMLElement | null;

    if (formItem) {
      formItem.classList.remove('codex-form-item-shake');
      void formItem.offsetWidth;
      formItem.classList.add('codex-form-item-shake');
      window.setTimeout(() => formItem.classList.remove('codex-form-item-shake'), 900);
    }

    if (input && 'focus' in input && typeof input.focus === 'function') {
      input.focus();
    }
  }, 120);

  return true;
}
