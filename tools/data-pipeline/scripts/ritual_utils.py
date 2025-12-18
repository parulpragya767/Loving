import json
from typing import List

def sanitize_steps_text_to_array(raw_steps: str | None) -> List[str]:
    """Sanitizes steps from Airtable (JSON string) to array format for JSON."""
    if not raw_steps:
        return []
    try:
        parsed = json.loads(raw_steps)
        if isinstance(parsed, list):
            return [step.strip() for step in parsed if step and step.strip()]
        return []
    except json.JSONDecodeError:
        print("Invalid steps JSON:", raw_steps)
    return []

def steps_array_to_text(steps: List[str]) -> str:
    """Converts steps array from JSON to JSON string format for Airtable."""
    if not isinstance(steps, list):
        return "[]"

    cleaned = [step.strip() for step in steps if step and step.strip()]
    return json.dumps(cleaned, indent=2, ensure_ascii=False)
