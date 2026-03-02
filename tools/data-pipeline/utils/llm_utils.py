import json
import os
from typing import Any, Dict, Optional, TypeVar, Type
from openai import OpenAI
from pydantic import BaseModel

# Configuration from environment variables with defaults
OPENAI_API_MODEL = os.getenv("AI_OPENAI_API_MODEL")
OPENAI_API_KEY = os.getenv("AI_OPENAI_API_KEY")
PROMPT_ID = "pmpt_6979d318ddbc8196be589cc7f1e3b6010cfc7a973b8ec130"
PROMPT_VERSION = "6"

# Initialize OpenAI client
client = OpenAI(api_key=OPENAI_API_KEY)

T = TypeVar('T', bound=BaseModel)

def call_llm_json_with_usage(
    model_class: Type[T],
    user_input: str,
    prompt_version: str = PROMPT_VERSION,
    prompt_id: str = PROMPT_ID,
) -> tuple[T, Dict[str, Any]]:
    """Call OpenAI responses API and return parsed model instance with usage information."""
    if not OPENAI_API_KEY:
        raise ValueError("AI_OPENAI_API_KEY environment variable is not set")

    try:
        response = client.responses.parse(
            model=OPENAI_API_MODEL,
            prompt={
                "id": prompt_id,
                "version": prompt_version,
                "variables": {
                    "ritual_data": user_input
                }
            },
            text_format=model_class,
        )
        
        # Extract usage information
        usage_info = {
            "model": OPENAI_API_MODEL,
            "prompt_id": prompt_id,
            "prompt_version": prompt_version,
            "input_tokens": getattr(response.usage, 'input_tokens', None) if hasattr(response, 'usage') else None,
            "output_tokens": getattr(response.usage, 'output_tokens', None) if hasattr(response, 'usage') else None,
            "total_tokens": getattr(response.usage, 'total_tokens', None) if hasattr(response, 'usage') else None,
        }
        
        return response.output_parsed, usage_info
    except Exception as e:
        raise Exception(f"API request failed: {str(e)}")