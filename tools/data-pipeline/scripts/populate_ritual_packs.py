
from __future__ import annotations

import argparse
from datetime import datetime
import json
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

from model.ritual_pack_models import Journey, RitualPackDetailResponse, RitualPackInput
from utils.json_utils import (
    append_to_json_array_file,
    load_json_array,
    write_json_file,
    RITUAL_PACKS_PATH,
    RITUALS_PATH,
)
from utils.llm_utils import (PromptType, call_llm_json_with_usage,)
from utils.ritual_utils import RitualFields, RitualPackFields

LLM_RITUAL_PACK_OUTPUT_PATH = "data/llm_ritual_pack_output_changelog.json"

def build_ritual_pack_input(ritual_pack_id: str) -> RitualPackInput:
    ritual_packs = load_json_array(RITUAL_PACKS_PATH)
    rituals = load_json_array(RITUALS_PATH)

    ritual_pack: Optional[Dict[str, Any]] = next(
        (p for p in ritual_packs if p.get(RitualPackFields.ID) == ritual_pack_id),
        None,
    )
    if ritual_pack is None:
        raise ValueError(f"Ritual pack with id '{ritual_pack_id}' not found")

    rituals_by_id: Dict[str, Dict[str, Any]] = {
        str(r.get(RitualFields.ID)): r for r in rituals if r.get(RitualFields.ID)
    }

    ritual_summaries: List[Dict[str, str]] = []
    for ritual_id in ritual_pack.get(RitualPackFields.RITUAL_IDS, []) or []:
        ritual = rituals_by_id.get(str(ritual_id))
        if ritual is None:
            continue
        ritual_summaries.append(
            {
                RitualFields.TITLE: str(ritual.get(RitualFields.TITLE, "")),
                RitualFields.SHORT_DESCRIPTION: str(ritual.get(RitualFields.SHORT_DESCRIPTION, "")),
            }
        )

    journey_code = ritual_pack.get(RitualPackFields.JOURNEY)
    journey_description = ""
    if isinstance(journey_code, str):
        journey_description = Journey[journey_code].value if journey_code in Journey.__members__ else ""

    return RitualPackInput(
        title=str(ritual_pack.get(RitualPackFields.TITLE, "")),
        shortDescription=str(ritual_pack.get(RitualPackFields.SHORT_DESCRIPTION, "")),
        journey=journey_description,
        loveTypes=[str(v) for v in (ritual_pack.get(RitualPackFields.LOVE_TYPES, []) or [])],
        relationalNeeds=[str(v) for v in (ritual_pack.get(RitualPackFields.RELATIONAL_NEEDS, []) or [])],
        rituals=ritual_summaries,
    )

def populate_ritual_pack_details_with_llm(
    ritual_pack_input: RitualPackInput,
) -> Tuple[RitualPackDetailResponse, Dict[str, Any]]:
    user_input = json.dumps(ritual_pack_input.model_dump(), indent=2, ensure_ascii=False)
    return call_llm_json_with_usage(
        model_class=RitualPackDetailResponse,
        user_input=user_input,
        prompt_type=PromptType.RITUAL_PACK_CREATION,
    )

def dump_llm_output(ritual_pack_id: str, ritual_pack_title: str, details: RitualPackDetailResponse, usage_info: Dict[str, Any]) -> None:
    timestamp = datetime.now().isoformat()

    entry = {
        "timestamp": timestamp,
        "ritual_pack_id": ritual_pack_id,
        "title": ritual_pack_title,
        "usage": usage_info,
        "ritualPackDetails": details.model_dump(),
    }

    append_to_json_array_file(LLM_RITUAL_PACK_OUTPUT_PATH, entry)
    print(f"  > Dumped LLM output for ritual pack {ritual_pack_id} to changelog at {timestamp}")

def update_ritual_pack_fields_from_llm_output(ritual_pack_id: str, details: RitualPackDetailResponse) -> None:
    ritual_packs = load_json_array(RITUAL_PACKS_PATH)

    ritual_pack: Optional[Dict[str, Any]] = next(
        (p for p in ritual_packs if p.get(RitualPackFields.ID) == ritual_pack_id),
        None,
    )
    if ritual_pack is None:
        raise ValueError(f"Ritual pack with id '{ritual_pack_id}' not found")

    llm_fields = details.model_dump()
    updatable_fields = (
        RitualPackFields.TAGLINE,
        RitualPackFields.DESCRIPTION,
        RitualPackFields.HOW_IT_HELPS,
        RitualPackFields.SEMANTIC_SUMMARY,
    )
    for key in updatable_fields:
        if key in llm_fields and key in ritual_pack:
            ritual_pack[key] = llm_fields[key]

    write_json_file(RITUAL_PACKS_PATH, ritual_packs)

def main() -> None:
    parser = argparse.ArgumentParser(description="Build the ritual pack details by calling LLM")
    parser.add_argument(
        "ritual_pack_id",
        type=str,
        help="Ritual pack id (UUID) from src/main/resources/data/ritualPacks.json",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Write the LLM-generated fields back into src/main/resources/data/ritualPacks.json",
    )

    args = parser.parse_args()

    ritual_pack_input = build_ritual_pack_input(args.ritual_pack_id)
    print(json.dumps(ritual_pack_input.model_dump(), indent=2, ensure_ascii=False))

    details, usage_info = populate_ritual_pack_details_with_llm(ritual_pack_input)
    dump_llm_output(args.ritual_pack_id, ritual_pack_input.title, details, usage_info)

    if args.apply:
        update_ritual_pack_fields_from_llm_output(args.ritual_pack_id, details)
        print(f"  > Updated ritual pack fields in {RITUAL_PACKS_PATH}")

if __name__ == "__main__":
    main()
