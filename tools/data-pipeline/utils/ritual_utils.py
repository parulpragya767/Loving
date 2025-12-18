import json
from typing import List, Dict
from utils.airtable_utils import AirtableFields

# Centralized ritual field mapping for JSON to Airtable synchronization
class RitualFields:
    """Centralized ritual field names for JSON structure."""
    
    ID = "id"
    TITLE = "title"
    TAGLINE = "tagLine"
    DESCRIPTION = "description"
    STEPS = "steps"
    HOW_IT_HELPS = "howItHelps"
    LOVE_TYPES = "loveTypes"
    RELATIONAL_NEEDS = "relationalNeeds"
    RITUAL_MODE = "ritualMode"
    RITUAL_TONES = "ritualTones"
    TIME_TAKEN = "timeTaken"
    SEMANTIC_SUMMARY = "semanticSummary"
    STATUS = "status"

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
