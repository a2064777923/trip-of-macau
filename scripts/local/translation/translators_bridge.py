import json
import subprocess
import sys
from pathlib import Path

if hasattr(sys.stdin, "reconfigure"):
    sys.stdin.reconfigure(encoding="utf-8")
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

DEFAULT_LOCALE_MAP = {
    "zh-Hans": "zh-CN",
    "zh-Hant": "zh-TW",
    "en": "en",
    "pt": "pt",
}

ENGINE_LOCALE_MAP = {
    "bing": {
        "zh-Hans": "zh-Hans",
        "zh-Hant": "zh-Hant",
        "en": "en",
        "pt": "pt",
    },
    "google": DEFAULT_LOCALE_MAP,
    "deepl": DEFAULT_LOCALE_MAP,
    "yandex": DEFAULT_LOCALE_MAP,
    "baidu": DEFAULT_LOCALE_MAP,
    "alibaba": DEFAULT_LOCALE_MAP,
    "sogou": DEFAULT_LOCALE_MAP,
    "iciba": DEFAULT_LOCALE_MAP,
    "tencent": DEFAULT_LOCALE_MAP,
}


def resolve_locale(engine: str, locale: str) -> str:
    return ENGINE_LOCALE_MAP.get(engine, DEFAULT_LOCALE_MAP).get(locale, locale)


def resolve_requirements_path() -> Path:
    return Path(__file__).resolve().with_name("requirements.txt")


def read_payload() -> dict:
    raw = sys.stdin.buffer.read()
    if not raw:
        return {}

    for encoding in ("utf-8", "utf-8-sig", "gb18030", "cp936"):
        try:
            return json.loads(raw.decode(encoding))
        except UnicodeDecodeError:
            continue
        except json.JSONDecodeError as exc:
            raise ValueError(f"invalid JSON payload ({encoding}): {exc}") from exc

    raise ValueError("stdin payload could not be decoded as UTF-8 or GB18030/CP936")


def ensure_translators_installed():
    try:
        import translators as ts  # type: ignore
        return ts
    except Exception:
        requirements_path = resolve_requirements_path()
        install_command = [
            sys.executable,
            "-m",
            "pip",
            "install",
            "-r",
            str(requirements_path),
        ]
        install = subprocess.run(
            install_command,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
        )
        if install.returncode != 0:
            error_message = install.stderr.strip() or install.stdout.strip() or "unknown install error"
            raise RuntimeError(
                "translators install failed: "
                f"{error_message}. Tried: {' '.join(install_command)}"
            )

        try:
            import translators as ts  # type: ignore
            return ts
        except Exception as exc:
            raise RuntimeError(f"translators import failed after install: {exc}") from exc


def main() -> int:
    try:
        payload = read_payload()
    except Exception as exc:
        print(json.dumps({
            "ok": False,
            "error": f"invalid bridge payload: {exc}",
        }, ensure_ascii=False))
        return 0

    engine = str(payload.get("engine", "")).strip().lower()
    source_locale = str(payload.get("sourceLocale", "")).strip()
    target_locale = str(payload.get("targetLocale", "")).strip()
    text = str(payload.get("text", ""))
    timeout_ms = int(payload.get("timeoutMs", 8000) or 8000)

    if not engine or not source_locale or not target_locale or not text.strip():
        print(json.dumps({"ok": False, "error": "engine, locales, and text are required"}, ensure_ascii=False))
        return 0

    try:
        ts = ensure_translators_installed()
    except Exception as exc:
        print(json.dumps({
            "ok": False,
            "error": str(exc),
        }, ensure_ascii=False))
        return 0

    try:
        translated = ts.translate_text(
            query_text=text,
            translator=engine,
            from_language=resolve_locale(engine, source_locale),
            to_language=resolve_locale(engine, target_locale),
            timeout=max(timeout_ms / 1000.0, 1.0),
        )
        print(json.dumps({
            "ok": True,
            "engine": engine,
            "text": translated,
        }, ensure_ascii=False))
        return 0
    except Exception as exc:
        print(json.dumps({
            "ok": False,
            "engine": engine,
            "error": f"{exc.__class__.__name__}: {exc}",
        }, ensure_ascii=False))
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
