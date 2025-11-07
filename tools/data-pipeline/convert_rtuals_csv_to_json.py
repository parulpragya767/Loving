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
        reader = csv.DictReader(f)
        return list(reader)

def compute_file_hash(csv_path: Path) -> str:
    sha = hashlib.sha256()
    with csv_path.open("rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            sha.update(chunk)
    return sha.hexdigest()

@dataclass
class RitualDTO:
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    title: Optional[str] = None
    shortDescription: Optional[str] = None
    fullDescription: Optional[str] = None
    ritualTypes: List[str] = field(default_factory=list)
    ritualMode: Optional[str] = None
    ritualTones: List[str] = field(default_factory=list)
    sensitivityLevel: Optional[str] = None
    effortLevel: Optional[str] = None
    timeTaken: Optional[str] = None
    estimatedDurationMinutes: Optional[int] = None
    ritualSteps: List[Dict[str, Any]] = field(default_factory=list)
    mediaAssets: List[Dict[str, Any]] = field(default_factory=list)
    loveTypesSupported: List[str] = field(default_factory=list)
    emotionalStatesSupported: List[str] = field(default_factory=list)
    relationalNeedsServed: List[str] = field(default_factory=list)
    lifeContextsRelevant: List[str] = field(default_factory=list)
    rhythm: Optional[str] = None
    preparationRequirements: List[str] = field(default_factory=list)
    semanticSummary: Optional[str] = None
    status: Optional[str] = None
    createdBy: Optional[str] = None
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
    dto = RitualDTO(
        title=row.get("title") or None,
        shortDescription=row.get("shortDescription") or None,
        fullDescription=row.get("longDescription") or None,
        loveTypesSupported=parse_list(row.get("loveTypes", "")),
        relationalNeedsServed=parse_list(row.get("relationalNeeds", "")),
        lifeContextsRelevant=parse_list(row.get("lifeContexts", "")),
        emotionalStatesSupported=parse_list(row.get("emotionalStates", "")),
        ritualMode=row.get("ritualMode") or "PAIR",
        rhythm=row.get("rhythm") or None,
        timeTaken=row.get("timeTaken") or None,
        semanticSummary=row.get("semanticSummary") or None
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

