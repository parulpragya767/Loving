import json
from typing import Dict, List, Any
from datetime import datetime
from utils.llm_utils import call_llm_json_with_usage
from utils.prompt_utils import load_prompt
from model.ritual_models import BatchRitualDetailsResponse
from utils.airtable_utils import AirtableFields, SyncStatus
from utils.ritual_utils import steps_array_to_text

LLM_OUTPUT_PATH = "data/llm_output_changelog.json"

def dump_llm_batch_output(batch_details: BatchRitualDetailsResponse, usage_info: Dict[str, Any], 
                         prompt_name: str, ritual_count: int):
    """Dump the LLM output for a batch to the changelog file."""
    timestamp = datetime.now().isoformat()
    
    # Prepare batch entry with metadata
    batch_entry = {
        "timestamp": timestamp,
        "prompt_name": prompt_name,
        "ritual_count": ritual_count,
        "usage": usage_info,
        "rituals": [ritual.dict() for ritual in batch_details.rituals]
    }
    
    # Read existing changelog if it exists
    try:
        with open(LLM_OUTPUT_PATH, 'r', encoding='utf-8') as f:
            changelog_data = json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        changelog_data = []
    
    # Append new batch entry
    changelog_data.append(batch_entry)
    
    # Write back to file
    with open(LLM_OUTPUT_PATH, 'w', encoding='utf-8') as f:
        json.dump(changelog_data, f, indent=2, ensure_ascii=False)
    
    print(f"  > Dumped LLM output for {ritual_count} rituals to changelog at {timestamp}")

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

def populate_missing_ritual_fields_batch(batch: List[Dict[str, Any]], prompt_name: str = "prompt_v1") -> List[Dict[str, Any]]:
    """
    Populate missing ritual fields for a batch of rituals using LLM in one call.
    
    Args:
        batch: List of ritual dictionaries to process
        prompt_name: Name of the prompt being used for processing
    """
    print(f"  > Populating ritual details for {len(batch)} rituals using LLM with {prompt_name}...")
    system_prompt = load_prompt(prompt_name)

    try:
        valid_rituals = []
        
        for i, ritual in enumerate(batch):
            title = ritual.get(AirtableFields.TITLE, "")
            if not title:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title")
                continue
            valid_rituals.append(ritual)
        
        if not valid_rituals:
            print("  > No valid rituals to process")
            return batch
        
        # Create prompt with ritual data including all available fields
        ritual_data_prompt = generate_ritual_data_prompt(valid_rituals)
        
        # Generate ritual details for all rituals in one batch
        batch_details, usage_info = call_llm_json_with_usage(
            model_class=BatchRitualDetailsResponse,
            system=system_prompt,
            prompt=ritual_data_prompt,
        )
        
        # Dump LLM output to changelog
        dump_llm_batch_output(batch_details, usage_info, prompt_name, len(valid_rituals))
        
        # Update each ritual with its corresponding details
        for i, (ritual, details) in enumerate(zip(valid_rituals, batch_details.rituals)):
            ritual[AirtableFields.TAGLINE] = details.tagLine
            ritual[AirtableFields.DESCRIPTION] = details.description
            ritual[AirtableFields.STEPS] = steps_array_to_text(details.steps)
            ritual[AirtableFields.HOW_IT_HELPS] = details.howItHelps
            ritual[AirtableFields.LOVE_TYPES] = [l.value for l in details.loveTypes]
            ritual[AirtableFields.RELATIONAL_NEEDS] = [r.value for r in details.relationalNeeds]
            ritual[AirtableFields.RITUAL_TONES] = [t.value for t in details.ritualTones]
            ritual[AirtableFields.TIME_TAKEN] = details.timeTaken.value
            ritual[AirtableFields.SEMANTIC_SUMMARY] = details.semanticSummary
            ritual[AirtableFields.SYNC_STATUS] = SyncStatus.REVIEW.value
            
            print(f"  > Successfully populated ritual {i+1}: {ritual.get(AirtableFields.TITLE, 'Unknown')}")
        
    except Exception as e:
        print(f"  > Error populating batch: {str(e)}")
    
    return batch
