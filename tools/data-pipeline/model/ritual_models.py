from typing import List
from enum import Enum
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
        description="Short, clear one-line phrase capturing the ritual’s essence. Warm, grounded, and inviting."
    )

    description: str = Field(
        description="Brief overview of what the ritual involves and its emotional intention, in plain language."
    )

    steps: List[str] = Field(
        description="Chronological, actionable steps. Each step is concise, practical, and gently invitational."
    )

    howItHelps: str = Field(
        description="Explains the emotional or relational shift this ritual supports, in natural, empathetic language."
    )

    loveTypes: List[LoveType] = Field(
        description="Primary love types this ritual expresses or strengthens (e.g., CARE, FIRE, BELONG). Select 1–3."
    )

    relationalNeeds: List[RelationalNeed] = Field(
        description="Core relational needs this ritual supports (select 1–3)."
    )

    ritualTones: List[RitualTone] = Field(
        description="The felt emotional tone of the ritual experience (select 1–3)."
    )

    timeTaken: TimeTaken = Field(
        description="Estimated duration category reflecting typical real-world use."
    )

    semanticSummary: str = Field(
        description="A brief, compassionate summary (2–4 sentences) describing when this ritual is most helpful, what emotional state it supports, and what kind of shift it offers."
    )


class BatchRitualDetailsResponse(BaseModel):
    """Batch response for multiple rituals."""

    rituals: List[RitualDetailsResponse] = Field(
        description="One completed ritual object per input, in the same order."
    )
