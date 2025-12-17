import json
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from LLMUtils import call_llm_json
from PromptUtils import get_ritual_details_prompt

@dataclass
class RitualDetails:
    """Data class representing structured ritual details from LLM"""
    tagline: str
    description: str
    steps: List[str]
    howItHelps: str
    loveType: List[str]
    ritualMode: str
    timeTaken: str
    relationalNeed: List[str]
    tone: str

def generate_ritual_details(title: str, prompt: str) -> RitualDetails:
    """
    Generate complete ritual details using LLM.
    """
    system_prompt = get_ritual_details_prompt()

    # Call LLM
    # response = call_llm_json(
    #     prompt=formatted_prompt,
    #     system="You are a ritual designer for the Loving App. Always respond with valid JSON only.",
    # )

    response={
        "tagline": "",
        "description": "",
        "steps": [],
        "howItHelps": "",
        "loveType": [],
        "ritualMode": "",
        "timeTaken": "",
        "relationalNeed": [],
        "tone": ""
    }
    
    # Convert to RitualDetails object
    return RitualDetails(
        tagline=response.get('tagline', ''),
        description=response.get('description', ''),
        steps=response.get('steps', []),
        howItHelps=response.get('howItHelps', ''),
        loveType=response.get('loveType', []),
        ritualMode=response.get('ritualMode', ''),
        timeTaken=response.get('timeTaken', ''),
        relationalNeed=response.get('relationalNeed', []),
        tone=response.get('tone', '')
    )

def populate_missing_ritual_fields_batch(batch: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Populate missing ritual fields for a batch of rituals using LLM.
    """
    print(f"  > Populating ritual details for {len(batch)} rituals using LLM...")
    
    for i, ritual in enumerate(batch):
        try:
            title = ritual.get("Title", "")
            prompt = ritual.get("Prompt", ritual.get("Description", ""))
            
            if not title or not prompt:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title or prompt")
                continue
            
            # Generate ritual details using LLM
            details = generate_ritual_details(title, prompt)
            
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
