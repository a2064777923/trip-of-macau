export interface SuspiciousTextIssue {
  path: string;
  reason: string;
  preview: string;
}

const MOJIBAKE_LEADS = 'ÃÂÅÆÇÐÑÕÖØàáâãäåçèéêëìíîïðñòóôõöøùúûüýþÿæ';

function appendPath(path: string, segment: string) {
  if (!path) {
    return segment;
  }
  return `${path}.${segment}`;
}

function preview(value: string) {
  const normalized = value.replace(/\r|\n/g, ' ').trim();
  if (normalized.length <= 24) {
    return normalized;
  }
  return `${normalized.slice(0, 24)}...`;
}

function shouldSkipQuestionMarkHeuristic(path: string) {
  const lower = path.toLowerCase();
  return (
    lower.endsWith('json') ||
    lower.endsWith('html') ||
    lower.endsWith('url') ||
    lower.endsWith('uri') ||
    lower.endsWith('path') ||
    lower.endsWith('token') ||
    lower.endsWith('secret') ||
    lower.endsWith('password') ||
    lower.endsWith('key')
  );
}

function containsSuspiciousControlCharacters(value: string) {
  for (let index = 0; index < value.length; index += 1) {
    const code = value.charCodeAt(index);
    if (
      (code >= 0x00 && code <= 0x08) ||
      code === 0x0b ||
      code === 0x0c ||
      (code >= 0x0e && code <= 0x1f) ||
      (code >= 0x7f && code <= 0x9f)
    ) {
      return true;
    }
  }
  return false;
}

function looksLikeQuestionMarkCorruption(value: string) {
  const trimmed = value.trim();
  if (!trimmed) {
    return false;
  }
  if (/^[?\s]+$/.test(trimmed)) {
    return true;
  }
  return trimmed.includes('???');
}

function containsCjk(value: string) {
  return /[\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF]/u.test(value);
}

function fitsLatin1(value: string) {
  for (const character of value) {
    if (character.charCodeAt(0) > 0xff) {
      return false;
    }
  }
  return true;
}

function containsPotentialMojibakeLead(value: string) {
  for (const character of value) {
    if (MOJIBAKE_LEADS.includes(character)) {
      return true;
    }
  }
  return false;
}

function tryRepairUtf8Mojibake(value: string) {
  if (!fitsLatin1(value) || !containsPotentialMojibakeLead(value)) {
    return null;
  }
  const bytes = Uint8Array.from(Array.from(value, (character) => character.charCodeAt(0) & 0xff));
  try {
    return new TextDecoder('utf-8', { fatal: false }).decode(bytes);
  } catch {
    return null;
  }
}

function inspectString(value: string, path: string): SuspiciousTextIssue | null {
  if (!value || !value.trim()) {
    return null;
  }
  if (value.includes('\uFFFD')) {
    return { path, reason: '包含替换字符', preview: preview(value) };
  }
  if (containsSuspiciousControlCharacters(value)) {
    return { path, reason: '包含异常控制字符', preview: preview(value) };
  }
  if (!shouldSkipQuestionMarkHeuristic(path) && looksLikeQuestionMarkCorruption(value)) {
    return { path, reason: '包含连续问号或仅由问号组成', preview: preview(value) };
  }
  const repaired = tryRepairUtf8Mojibake(value);
  if (repaired && repaired !== value && containsCjk(repaired) && !containsCjk(value)) {
    return { path, reason: '疑似 UTF-8 被错误按 ANSI/Latin-1 解码', preview: preview(value) };
  }
  return null;
}

function isPlainObject(value: unknown): value is Record<string, unknown> {
  return Object.prototype.toString.call(value) === '[object Object]';
}

function isBinaryLike(value: unknown) {
  return (
    typeof FormData !== 'undefined' && value instanceof FormData ||
    typeof Blob !== 'undefined' && value instanceof Blob ||
    typeof File !== 'undefined' && value instanceof File
  );
}

function inspectValue(value: unknown, path: string, visited: WeakSet<object>): SuspiciousTextIssue | null {
  if (value == null) {
    return null;
  }
  if (typeof value === 'string') {
    return inspectString(value, path);
  }
  if (
    typeof value === 'number' ||
    typeof value === 'boolean' ||
    typeof value === 'bigint' ||
    typeof value === 'symbol' ||
    value instanceof Date ||
    isBinaryLike(value)
  ) {
    return null;
  }
  if (Array.isArray(value)) {
    for (let index = 0; index < value.length; index += 1) {
      const issue = inspectValue(value[index], `${path}[${index}]`, visited);
      if (issue) {
        return issue;
      }
    }
    return null;
  }
  if (typeof value === 'object') {
    if (visited.has(value as object)) {
      return null;
    }
    visited.add(value as object);
    if (value instanceof Map) {
      for (const [key, nested] of value.entries()) {
        const issue = inspectValue(nested, appendPath(path, String(key)), visited);
        if (issue) {
          return issue;
        }
      }
      return null;
    }
    if (!isPlainObject(value)) {
      return null;
    }
    for (const [key, nested] of Object.entries(value)) {
      const issue = inspectValue(nested, appendPath(path, key), visited);
      if (issue) {
        return issue;
      }
    }
  }
  return null;
}

export function findSuspiciousTextIssue(payload: unknown) {
  return inspectValue(payload, '', new WeakSet<object>());
}
