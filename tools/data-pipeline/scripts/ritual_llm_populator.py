import json
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from pydantic import BaseModel
from LLMUtils import call_llm_json
from PromptUtils import get_ritual_details_prompt

class RitualDetailsResponse(BaseModel):
    """Pydantic model for structured ritual details from LLM"""
    tagline: str
    description: str
    steps: List[str]
    howItHelps: str
    loveType: List[str]
    ritualMode: str
    timeTaken: str
    relationalNeed: List[str]
    tone: str

def populate_missing_ritual_fields_batch(batch: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Populate missing ritual fields for a batch of rituals using LLM.
    """
    print(f"  > Populating ritual details for {len(batch)} rituals using LLM...")
    system_prompt = get_ritual_details_prompt()

    for i, ritual in enumerate(batch):
        try:
            title = ritual.get("Title", "")
            if not title:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title")
                continue
            
            # Generate ritual details using LLM
            details = call_llm_json(
                model_class=RitualDetailsResponse,
                system=system_prompt,
                prompt=f"Title: {title}",
            )
            print(details)
            
            # Update ritual with generated details
            ritual["Tagline"] = details.tagline
            ritual["Description"] = details.description
            ritual["Steps"] = "\n".join(details.steps)
            ritual["How It Helps"] = details.howItHelps
            ritual["Love Type"] = ", ".join(details.loveType)
            ritual["Ritual Mode"] = details.ritualMode
            ritual["Time Taken"] = details.timeTaken
            ritual["Relational Need"] = ", ".join(details.relationalNeed)
            ritual["Tone"] = details.tone
            ritual["Sync Status"] = 'REVIEW'
            
            print(f"  > Successfully populated ritual {i+1}: {title}")
            
        except Exception as e:
            print(f"  > Error populating ritual {i+1}: {str(e)}")
            # Set status to indicate failure
            ritual["Sync Status"] = 'ERROR'
            continue
    
    return batch
