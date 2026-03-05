import json
import os
from dataclasses import dataclass
from enum import Enum
from typing import Any, Dict, TypeVar, Type
from openai import OpenAI
from pydantic import BaseModel

# Configuration from environment variables with defaults
OPENAI_API_KEY = os.getenv("AI_OPENAI_API_KEY")
OPENAI_API_MODEL = "gpt-4.1"

RITUAL_PROMPT_ID = "pmpt_6979d318ddbc8196be589cc7f1e3b6010cfc7a973b8ec130"
RITUAL_PROMPT_VERSION = "6"
RITUAL_PROMPT_VARIABLE = "ritual_data"

RITUAL_PACK_PROMPT_ID = "pmpt_69a839b52a288196aceb049c87d4e71703b9a81bf58f11b4"
RITUAL_PACK_PROMPT_VERSION = "3"
RITUAL_PACK_PROMPT_VARIABLE = "ritual_pack_data"

# Initialize OpenAI client
client = OpenAI(api_key=OPENAI_API_KEY)

T = TypeVar('T', bound=BaseModel)


class PromptType(str, Enum):
    RITUAL_CREATION = "ritual_creation"
    RITUAL_PACK_CREATION = "ritual_pack_creation"


@dataclass(frozen=True)
class PromptConfig:
    prompt_id: str
    prompt_version: str
    prompt_variable: str


PROMPT_CONFIGS: Dict[PromptType, PromptConfig] = {
    PromptType.RITUAL_CREATION: PromptConfig(
        prompt_id=RITUAL_PROMPT_ID,
        prompt_version=RITUAL_PROMPT_VERSION,
        prompt_variable=RITUAL_PROMPT_VARIABLE,
    ),
    PromptType.RITUAL_PACK_CREATION: PromptConfig(
        prompt_id=RITUAL_PACK_PROMPT_ID,
        prompt_version=RITUAL_PACK_PROMPT_VERSION,
        prompt_variable=RITUAL_PACK_PROMPT_VARIABLE,
    ),
}

def get_prompt_config(prompt_type: PromptType) -> PromptConfig:
    return PROMPT_CONFIGS[prompt_type]

def call_llm_json_with_usage(
    model_class: Type[T],
    user_input: str,
    prompt_type: PromptType = PromptType.RITUAL_CREATION,
) -> tuple[T, Dict[str, Any]]:
    """Call OpenAI responses API and return parsed model instance with usage information."""
    if not OPENAI_API_KEY:
        raise ValueError("AI_OPENAI_API_KEY environment variable is not set")

    prompt_config = get_prompt_config(prompt_type)

    if not prompt_config.prompt_id:
        raise ValueError("Prompt id is not set")

    if not prompt_config.prompt_version:
        raise ValueError("Prompt version is not set")

    try:
        response = client.responses.parse(
            model=OPENAI_API_MODEL,
            prompt={
                "id": prompt_config.prompt_id,
                "version": prompt_config.prompt_version,
                "variables": {
                    prompt_config.prompt_variable: user_input
                }
            },
            text_format=model_class,
        )
        
        # Extract usage information
        usage_info = {
            "model": OPENAI_API_MODEL,
            "prompt_id": prompt_config.prompt_id,
            "prompt_version": prompt_config.prompt_version,
            "input_tokens": getattr(response.usage, 'input_tokens', None) if hasattr(response, 'usage') else None,
            "output_tokens": getattr(response.usage, 'output_tokens', None) if hasattr(response, 'usage') else None,
            "total_tokens": getattr(response.usage, 'total_tokens', None) if hasattr(response, 'usage') else None,
        }
        
        return response.output_parsed, usage_info
    except Exception as e:
        raise Exception(f"API request failed: {str(e)}")