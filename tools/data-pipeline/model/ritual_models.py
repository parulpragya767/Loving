from typing import List
from enum import Enum
import json
from pydantic import BaseModel, Field

class LoveType(str, Enum):
    BELONG = "BELONG"
    FIRE = "FIRE"
    SPARK = "SPARK"
    CARE = "CARE"
    SELF = "SELF"
    BUILD = "BUILD"
    GROW = "GROW"
    BEYOND = "BEYOND"
    GRACE = "GRACE"

class RelationalNeed(str, Enum):
    CONNECTION = "CONNECTION"
    INTIMACY = "INTIMACY"
    UNDERSTANDING = "UNDERSTANDING"
    ACCEPTANCE_AND_FORGIVENESS = "ACCEPTANCE_AND_FORGIVENESS"
    TRUST_AND_SAFETY = "TRUST_AND_SAFETY"
    SUPPORT = "SUPPORT"
    BALANCE_AND_FAIRNESS = "BALANCE_AND_FAIRNESS"
    COMMUNICATION = "COMMUNICATION"
    PLAY_AND_JOY = "PLAY_AND_JOY"
    GROWTH = "GROWTH"
    GRATITUDE_AND_APPRECIATION = "GRATITUDE_AND_APPRECIATION"
    PRESENCE_AND_QUALITY_TIME = "PRESENCE_AND_QUALITY_TIME"
    SPACE = "SPACE"

class RitualTone(str, Enum):
    WARM = "WARM"
    PLAYFUL = "PLAYFUL"
    INTIMATE = "INTIMATE"
    REFLECTIVE = "REFLECTIVE"
    CALM = "CALM"
    ADVENTUROUS = "ADVENTUROUS"
    ENERGETIC = "ENERGETIC"
    HEALING = "HEALING"
    SACRED = "SACRED"

class TimeTaken(str, Enum):
    MOMENT = "MOMENT"
    SHORT = "SHORT"
    MEDIUM = "MEDIUM"
    LONG = "LONG"
    EXTENDED = "EXTENDED"
    FLEXIBLE = "FLEXIBLE"

class RitualDetailsResponse(BaseModel):
    """Structured ritual content for the Loving App."""
    tagLine: str = Field(
        description="A short one-line phrase capturing the core focus of the ritual."
    )

    description: str = Field(
        description="A brief overview explaining what the ritual involves and its emotional intention."
    )

    steps: List[str] = Field(
        description="An ordered list of clear, actionable steps describing how to practice the ritual."
    )

    howItHelps: str = Field(
        description="A short explanation of the emotional or relational shift the ritual supports."
    )

    loveTypes: List[LoveType] = Field(
        description="1–3 LoveType values representing the primary dimensions this ritual strengthens."
    )

    relationalNeeds: List[RelationalNeed] = Field(
        description="1–3 RelationalNeed values this ritual is designed to support."
    )

    ritualTones: List[RitualTone] = Field(
        description="1–3 RitualTone values describing the dominant emotional atmosphere."
    )

    timeTaken: TimeTaken = Field(
        description="Estimated duration category for completing the ritual."
    )

    semanticSummary: str = Field(
        description="A concise summary describing when this ritual is most helpful and the shift it encourages."
    )

schema = RitualDetailsResponse.model_json_schema()
# print(json.dumps(schema, indent=2))