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

class RitualMode(str, Enum):
    SOLO = "SOLO"
    TOGETHER = "TOGETHER"

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
    """Pydantic model generated from LLMPopulateRitualDetails.java OpenAPI contract"""
    tagLine: str = Field(description="A catchy, memorable phrase or headline for the ritual that captures its essence and purpose.")
    description: str = Field(description="A detailed explanation of what the ritual involves, its purpose, and what participants should expect.")
    howItHelps: str = Field(description="An explanation of the benefits and positive outcomes that participants can expect from performing this ritual.")
    steps: List[str] = Field(description="A step-by-step guide listing the sequence of actions to perform during the ritual, in chronological order.")
    loveTypes: List[LoveType] = Field(description="The primary love types being expressed or sought by the user — e.g., CARE, FIRE, BELONG. Choose 1–3 values that best represent their emotional theme.")
    relationalNeeds: List[RelationalNeed] = Field(description="The relational or emotional needs most present in the user's current experience — e.g., CONNECTION, UNDERSTANDING, TRUST_AND_SAFETY. Choose 1–3 values that fit best.")
    ritualTones: List[RitualTone] = Field(description="The specific tone or atmosphere of the ritual — e.g., PLAYFUL, SERIOUS, INTIMATE. Choose 1–3 values that best describe the ritual's emotional character.")
    ritualMode: RitualMode = Field(description="The mode in which the ritual is performed — SOLO or TOGETHER")
    timeTaken: TimeTaken = Field(description="The estimated duration required to complete the ritual — e.g., MOMENT, SHORT, LONG. Choose the time category that best fits.")
    semanticSummary: str = Field(description="A concise, empathetic 2–4 sentence summary describing the ritual's purpose, emotional impact, and ideal use case.")
