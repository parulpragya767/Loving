import json
import os
from typing import Any, Dict, Optional, TypeVar, Type
from openai import OpenAI
from pydantic import BaseModel

# Configuration from environment variables with defaults
OPENAI_API_MODEL = os.getenv("OPENAI_API_MODEL")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# Initialize OpenAI client
client = OpenAI(api_key=OPENAI_API_KEY)

T = TypeVar('T', bound=BaseModel)

def call_llm_json(model_class: Type[T], prompt: str, system: Optional[str] = None) -> T:
    """Call OpenAI responses API and return parsed model instance."""
    if not OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY environment variable is not set")
    
    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": prompt})

    try:
        response = client.responses.parse(
            model=OPENAI_API_MODEL,
            input=messages,
            text_format=model_class,
        )
        return response.output_parsed
    except Exception as e:
        raise Exception(f"API request failed: {str(e)}")

def call_llm_text(prompt: str, system: Optional[str] = None) -> str:
    """Call OpenAI responses API and return plain text content."""
    if not OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY environment variable is not set")
    
    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": prompt})

    try:
        response = client.responses.create(
            model=OPENAI_API_MODEL,
            input=messages,
        )
        return response.choices[0].message.content
    except Exception as e:
        raise Exception(f"API request failed: {str(e)}")
