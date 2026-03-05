from enum import Enum
from typing import Dict, List

from pydantic import BaseModel, Field

class Journey(str, Enum):
    FEELING_DISTANT = "When emotional connection feels thin and you sense yourselves drifting apart."
    LOVE_FEELS_FLAT = "When passion or excitement has faded and life together feels routine or uninspired."
    LOST_TOUCH = "When physical intimacy or affection has faded, and you want to feel warmth and closeness again."
    CARRYING_TOO_MUCH = "When responsibilities, imbalance, or mental load leave you tired and disconnected."
    WEATHERING_A_STORM = "When external stress or life transitions test the relationship and you need steadiness and support."
    BRIDGING_THE_DIVIDE = "When conflict, misunderstanding, or hurt has created distance and repair is needed."
    LEARNING_TO_HEAR_EACH_OTHER = "When communication feels difficult and you want to listen, understand, and feel heard more deeply."
    MAKING_SPACE_FOR_US = "When busy lives or distractions leave little time for each other and you want to reconnect."
    KEEP_THE_LOVE_ALIVE = "When you want to keep love vibrant through small, everyday gestures of affection and presence."
    GROW_AND_EVOLVE_TOGETHER = "When you want to support each other’s growth and evolve together as partners and individuals."
    RETURN_TO_SELF = "When you need to reconnect with your own inner world, needs, and sense of self."
    CELEBRATE_US = "When you want to honour your relationship, celebrate milestones, or express shared gratitude."


class RitualPackInput(BaseModel):
    title: str
    shortDescription: str
    journey: str
    loveTypes: List[str]
    relationalNeeds: List[str]
    rituals: List[Dict[str, str]]


class RitualPackDetailResponse(BaseModel):
    """Structured ritual pack content for the Loving App."""

    tagLine: str = Field(description="A short one-line phrase capturing the core focus of the ritual pack.")
    description: str = Field(description="A brief overview explaining what the ritual pack is for and its emotional intention.")
    howItHelps: str = Field(description="A short explanation of the emotional or relational shift this ritual pack supports.")
    semanticSummary: str = Field(description="A concise summary describing when this ritual pack is most helpful and the shift it encourages.")
