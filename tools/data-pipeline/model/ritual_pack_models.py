from enum import Enum
from typing import Dict, List

from pydantic import BaseModel, Field

class Journey(str, Enum):
    FEELING_DISTANT = "When emotional connection feels thin or you’re drifting apart inside."
    LOVE_FEELS_FLAT = "When passion or excitement feels lost — when life together feels routine or uninspired, and the spark has dimmed."
    LOST_TOUCH = "When physical intimacy or affection has faded, and you want to feel warmth and closeness again."
    CARRYING_TOO_MUCH = "When the daily grind, imbalance of responsibility, or mental overload leaves you tired and disconnected."
    WEATHERING_A_STORM = "Facing external stress or life transitions together and needing steadiness and support."
    BRIDGING_THE_DIVIDE = "Learning to repair and reconnect after conflict, misunderstanding, or hurt."
    LEARNING_TO_HEAR_EACH_OTHER = "Strengthening communication, empathy, and the ability to really listen and feel heard."
    MAKING_SPACE_FOR_US = "Prioritising quality time and togetherness amid busy lives or distractions."
    KEEP_THE_LOVE_ALIVE = "Nurturing affection and connection through small, daily gestures and presence."
    GROW_AND_EVOLVE_TOGETHER = "Encouraging mutual growth, learning, and transformation as partners and individuals."
    RETURN_TO_SELF = "Reconnecting with your own inner world, needs, and self-worth — so love can flow from wholeness."
    CELEBRATE_US = "Honouring love, milestones, or shared gratitude — remembering what you’ve built together."


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
