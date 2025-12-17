import json
import os
import requests
from typing import Any, Dict, Optional

# Configuration from environment variables with defaults
OPENAI_API_BASE_URL = os.getenv("OPENAI_API_BASE_URL")
OPENAI_API_MODEL = os.getenv("OPENAI_API_MODEL")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
DEFAULT_TEMPERATURE = 0.7
DEFAULT_TIMEOUT = 30

def call_llm_json(prompt: str, system: Optional[str] = None) -> Dict[str, Any]:
    """Call OpenAI-compatible chat completions API and return parsed JSON content."""
    if not OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY environment variable is not set")
        
    headers = {
        "Authorization": f"Bearer {OPENAI_API_KEY}",
        "Content-Type": "application/json",
    }

    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": prompt})

    payload = {
        "model": OPENAI_API_MODEL,
        "messages": messages,
        "temperature": DEFAULT_TEMPERATURE,
        "response_format": {"type": "json_object"},
    }

    try:
        response = requests.post(
            OPENAI_API_BASE_URL,
            headers=headers,
            json=payload,
            timeout=DEFAULT_TIMEOUT
        )
        response.raise_for_status()
        result = response.json()
        content = result["choices"][0]["message"]["content"]
        return json.loads(content)
    except requests.exceptions.RequestException as e:
        raise Exception(f"API request failed: {str(e)}")
    except (json.JSONDecodeError, KeyError) as e:
        raise Exception(f"Failed to parse JSON response: {str(e)}")

def call_llm_text(prompt: str, system: Optional[str] = None) -> str:
    """Call OpenAI-compatible chat completions API and return plain text content."""
    if not OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY environment variable is not set")
        
    headers = {
        "Authorization": f"Bearer {OPENAI_API_KEY}",
        "Content-Type": "application/json",
    }

    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": prompt})

    payload = {
        "model": OPENAI_API_MODEL,
        "messages": messages,
        "temperature": DEFAULT_TEMPERATURE,
    }

    try:
        response = requests.post(
            OPENAI_API_BASE_URL,
            headers=headers,
            json=payload,
            timeout=DEFAULT_TIMEOUT
        )
        response.raise_for_status()
        result = response.json()
        return result["choices"][0]["message"]["content"]
    except requests.exceptions.RequestException as e:
        raise Exception(f"API request failed: {str(e)}")
    except KeyError as e:
        raise Exception(f"Unexpected response format: {str(e)}")
