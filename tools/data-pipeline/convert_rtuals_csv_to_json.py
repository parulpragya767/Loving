import argparse
import csv
import hashlib
import json
import sys
import uuid
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from enum import Enum
from pathlib import Path
from typing import Any, Dict, List, Optional, Type, TypeVar

# Type variable for enum classes
E = TypeVar('E', bound=Enum)

def parse_enum_list(cell: str, enum_cls: Type[E]) -> List[E]:
    if not cell or not cell.strip():
        return []
    result = []
    for item in cell.split(","):
        item = item.strip()
        if not item:
            continue
        try:
            result.append(enum_cls[item.upper()])
        except KeyError:
            print(f"Warning: Unknown {enum_cls.__name__} value: {item}")
    return result

def parse_list(cell: str) -> List[str]:
    if not cell or not cell.strip():
        return []
    return [item.strip() for item in cell.split(",") if item.strip()]

def load_csv_rows(csv_path: Path) -> List[Dict[str, str]]:
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f, delimiter=';')
        return list(reader)

@dataclass
class RitualDTO:
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    title: Optional[str] = None
    tagLine: Optional[str] = None
    description: Optional[str] = None
    howItHelps: Optional[str] = None
    steps: List[str] = field(default_factory=list)
    loveTypes: List[str] = field(default_factory=list)
    relationalNeeds: List[str] = field(default_factory=list)
    ritualMode: Optional[str] = None
    ritualTones: List[str] = field(default_factory=list)
    timeTaken: Optional[str] = None
    mediaAssets: List[Dict[str, Any]] = field(default_factory=list) 
    semanticSummary: Optional[str] = None
    status: Optional[str] = None
    contentHash: Optional[str] = None
    createdAt: Optional[str] = None
    updatedAt: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dict with proper enum handling."""
        result = {}
        for k, v in self.__dict__.items():
            if v is None:
                continue
            if isinstance(v, list):
                if v and isinstance(v[0], Enum):
                    result[k] = [item.value for item in v]
                else:
                    result[k] = v
            elif isinstance(v, Enum):
                result[k] = v.value
            else:
                result[k] = v
        return result

def row_to_ritual_dto(row: Dict[str, str]) -> Dict[str, Any]:
    """Convert a CSV row to a RitualDTO dictionary."""
    # Helper to safely get and strip a cell
    def cell(name: str) -> Optional[str]:
        v = row.get(name)
        if v is None:
            return None
        v = v.strip()
        return v if v else None

    # Steps may be separated by '|' or '\n' or '->'; support several delimiters
    raw_steps = cell("Steps") or ""
    steps: List[str] = []
    if raw_steps:
        if "|" in raw_steps:
            steps = [s.strip() for s in raw_steps.split("|") if s.strip()]
        elif "->" in raw_steps:
            steps = [s.strip() for s in raw_steps.split("->") if s.strip()]
        else:
            steps = [s.strip() for s in raw_steps.split(";") if s.strip()]

    # Build DTO with fields matching Java RitualDTO
    dto = RitualDTO(
        id=cell("id") or str(uuid.uuid4()),
        title=cell("Title"),
        tagLine=cell("Tagline"),
        description=cell("Description"),
        howItHelps=cell("How It Helps"),
        steps=steps,
        loveTypes=parse_list(cell("Love Types") or ""),
        relationalNeeds=parse_list(cell("Relational Needs") or ""),
        ritualMode=cell("Ritual Mode") or "TOGETHER",
        ritualTones=parse_list(cell("Ritual Tones") or ""),
        timeTaken=cell("Time Taken"),
        mediaAssets=[],
        semanticSummary=cell("Semantic Summary"),
        status="PUBLISHED",
        contentHash=None,
        createdAt=None,
        updatedAt=None,
    )

    return dto.to_dict()


def convert(csv_path: Path, out_path: Path) -> None:
    rows = load_csv_rows(csv_path)
    data = [row_to_ritual_dto(r) for r in rows]

    out_path.parent.mkdir(parents=True, exist_ok=True)
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2, default=str)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description="Convert rituals.csv to RitualDTO-like JSON")
    parser.add_argument(
        "--input",
        "-i",
        type=Path,
        default=Path(__file__).parent / "rituals.csv",
        help="Path to rituals CSV file",
    )
    parser.add_argument(
        "--output",
        "-o",
        type=Path,
        default=Path(__file__).parent / "rituals.json",
        help="Path to output JSON file",
    )
    args = parser.parse_args(argv)

    convert(args.input, args.output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))


