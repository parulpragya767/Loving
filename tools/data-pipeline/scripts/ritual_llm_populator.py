import json
from typing import Dict, List, Any
from LLMUtils import call_llm_json
from PromptUtils import get_ritual_details_prompt
from data_models import BatchRitualDetailsResponse
from airtable_utils import AirtableFields

def generate_ritual_data_prompt(rituals: List[Dict[str, Any]]) -> str:
    """
    Generate a prompt string with ritual data, including only populated fields for each ritual.
    """
    ritual_data_prompt = "Ritual Data:\n"
    
    for i, ritual in enumerate(rituals):
        ritual_data_prompt += f"\nRitual {i+1}:\n"
        
        # Add title (always required)
        ritual_data_prompt += f"  title: {ritual.get(AirtableFields.TITLE)}\n"
        
        # Add other fields only if they have values
        if ritual.get(AirtableFields.DESCRIPTION):
            ritual_data_prompt += f"  description: {ritual.get(AirtableFields.DESCRIPTION)}\n"
        if ritual.get(AirtableFields.LOVE_TYPES):
            ritual_data_prompt += f"  loveTypes: {ritual.get(AirtableFields.LOVE_TYPES)}\n"
        if ritual.get(AirtableFields.RITUAL_MODE):
            ritual_data_prompt += f"  ritualMode: {ritual.get(AirtableFields.RITUAL_MODE)}\n"
        if ritual.get(AirtableFields.RELATIONAL_NEEDS):
            ritual_data_prompt += f"  relationalNeeds: {ritual.get(AirtableFields.RELATIONAL_NEEDS)}\n"
        if ritual.get(AirtableFields.TIME_TAKEN):
            ritual_data_prompt += f"  timeTaken: {ritual.get(AirtableFields.TIME_TAKEN)}\n"
    
    return ritual_data_prompt

def populate_missing_ritual_fields_batch(batch: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Populate missing ritual fields for a batch of rituals using LLM in one call.
    """
    print(f"  > Populating ritual details for {len(batch)} rituals using LLM...")
    system_prompt = get_ritual_details_prompt()

    try:
        # Extract titles from the batch
        titles = []
        valid_rituals = []
        
        for i, ritual in enumerate(batch):
            title = ritual.get(AirtableFields.TITLE, "")
            if not title:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title")
                continue
            titles.append(title)
            valid_rituals.append(ritual)
        
        if not titles:
            print("  > No valid rituals to process")
            return batch
        
        # Create prompt with ritual data including all available fields
        ritual_data_prompt = generate_ritual_data_prompt(valid_rituals)
        
        # Generate ritual details for all rituals in one batch
        batch_details = call_llm_json(
            model_class=BatchRitualDetailsResponse,
            system=system_prompt,
            prompt=ritual_data_prompt,
        )
        
        print(json.dumps([ritual.dict() for ritual in batch_details.rituals], indent=2, ensure_ascii=False))
        
        # Update each ritual with its corresponding details
        for i, (ritual, details) in enumerate(zip(valid_rituals, batch_details.rituals)):
            ritual[AirtableFields.TAGLINE] = details.tagLine
            ritual[AirtableFields.DESCRIPTION] = details.description
            ritual[AirtableFields.STEPS] = "\n".join(details.steps)
            ritual[AirtableFields.HOW_IT_HELPS] = details.howItHelps
            ritual[AirtableFields.LOVE_TYPES] = ", ".join([l.value for l in details.loveTypes])
            ritual[AirtableFields.RELATIONAL_NEEDS] = ", ".join([r.value for r in details.relationalNeeds])
            ritual[AirtableFields.RITUAL_TONES] = ", ".join([t.value for t in details.ritualTones])
            ritual[AirtableFields.RITUAL_MODE] = details.ritualMode.value
            ritual[AirtableFields.TIME_TAKEN] = details.timeTaken.value
            ritual[AirtableFields.SEMANTIC_SUMMARY] = details.semanticSummary
            ritual[AirtableFields.SYNC_STATUS] = 'REVIEW'
            
            print(f"  > Successfully populated ritual {i+1}: {ritual.get(AirtableFields.TITLE, 'Unknown')}")
        
    except Exception as e:
        print(f"  > Error populating batch: {str(e)}")
        # Set status to indicate failure for all valid rituals
        for ritual in valid_rituals:
            ritual[AirtableFields.SYNC_STATUS] = 'ERROR'
    
    return batch
