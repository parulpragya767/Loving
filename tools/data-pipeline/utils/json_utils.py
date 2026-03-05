from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict, List

PROJECT_ROOT = Path(__file__).resolve().parents[3]
RITUALS_PATH = PROJECT_ROOT / "src/main/resources/data/rituals.json"
RITUAL_PACKS_PATH = PROJECT_ROOT / "src/main/resources/data/ritualPacks.json"

def load_json_data(file_path: Path) -> Any:
    if not file_path.exists():
        raise FileNotFoundError(f"JSON file not found at {file_path}")

    with open(file_path, "r", encoding="utf-8") as f:
        return json.load(f)

def load_json_array(file_path: Path) -> List[Dict[str, Any]]:
    data = load_json_data(file_path)
    if not isinstance(data, list):
        raise ValueError(f"JSON content must be a list of objects: {file_path}")
    return data