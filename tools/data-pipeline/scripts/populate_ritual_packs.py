
from __future__ import annotations

import argparse
import json
from typing import Any, Dict, List, Optional, Tuple

from model.ritual_pack_models import Journey, RitualPackDetailResponse, RitualPackInput
from utils.json_utils import load_json_array, RITUAL_PACKS_PATH, RITUALS_PATH
from utils.llm_utils import (
    PromptType,
    call_llm_json_with_usage,
)
from utils.ritual_utils import RitualFields, RitualPackFields


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


def main() -> None:
    parser = argparse.ArgumentParser(description="Build the ritual pack details by calling LLM")
    parser.add_argument(
        "ritual_pack_id",
        type=str,
        help="Ritual pack id (UUID) from src/main/resources/data/ritualPacks.json",
    )

    args = parser.parse_args()

    ritual_pack_input = build_ritual_pack_input(args.ritual_pack_id)
    print(json.dumps(ritual_pack_input.model_dump(), indent=2, ensure_ascii=False))

    details, usage_info = populate_ritual_pack_details_with_llm(ritual_pack_input)
    print(
        json.dumps(
            {
                "ritualPackInput": ritual_pack_input.model_dump(),
                "ritualPackDetails": details.model_dump(),
                "usage": usage_info,
            },
            indent=2,
            ensure_ascii=False,
        )
    )

if __name__ == "__main__":
    main()
