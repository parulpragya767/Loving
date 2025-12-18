import os
from pathlib import Path
from typing import Dict

# Define all prompt file paths relative to the scripts directory
_PROMPT_FILES = {
    'populate_ritual_details': '../prompts/populate_ritual_details_prompt_temp.txt',
    'prompt_v1': '../prompts/prompt_v1.txt',
    'prompt_v2': '../prompts/prompt_v2.txt',
    # Add more prompt files here as needed
}

def _get_script_dir() -> Path:
    """Get the directory containing this script."""
    return Path(__file__).parent

def load_prompt(prompt_name: str) -> str:
    """Load a prompt template by name."""
    if prompt_name not in _PROMPT_FILES:
        raise ValueError(f"Unknown prompt name: {prompt_name}")
    
    script_dir = _get_script_dir()
    prompt_path = script_dir / _PROMPT_FILES[prompt_name]
    
    try:
        return prompt_path.read_text(encoding='utf-8')
    except FileNotFoundError as e:
        raise FileNotFoundError(f"Prompt file not found: {prompt_path}") from e
    except Exception as e:
        raise Exception(f"Error loading prompt '{prompt_name}': {str(e)}") from e

def get_ritual_details_prompt() -> str:
    """Get the ritual details prompt template."""
    return load_prompt('populate_ritual_details')
