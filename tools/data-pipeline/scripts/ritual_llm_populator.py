import json
from typing import Dict, List, Any
from datetime import datetime
from utils.llm_utils import call_llm_json_with_usage
from model.ritual_models import RitualDetailsResponse
from utils.airtable_utils import AirtableFields, SyncStatus
from utils.ritual_utils import steps_array_to_text

LLM_OUTPUT_PATH = "data/llm_output_changelog.json"

def dump_llm_batch_output(ritual: RitualDetailsResponse, usage_info: Dict[str, Any], title: str):
    """Dump the LLM output for a batch to the changelog file."""
    timestamp = datetime.now().isoformat()
    
    # Prepare batch entry with metadata
    batch_entry = {
        "timestamp": timestamp,
        "usage": usage_info,
        "title": title,
        "ritual": ritual.dict()
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
    
    print(f"  > Dumped LLM output for {title} to changelog at {timestamp}")

def generate_ritual_data_prompt(ritual: Dict[str, Any]) -> str:
    """
    Generate a prompt string with ritual data, including only populated fields for each ritual.
    """
    # Add title (always required)
    ritual_data_prompt = f"title: {ritual.get(AirtableFields.TITLE)}\n"
    
    # Add other fields only if they have values
    if ritual.get(AirtableFields.SHORT_DESCRIPTION):
        ritual_data_prompt += f"description: {ritual.get(AirtableFields.SHORT_DESCRIPTION)}\n"
    if ritual.get(AirtableFields.LOVE_TYPES):
        ritual_data_prompt += f"loveTypes: {ritual.get(AirtableFields.LOVE_TYPES)}\n"
    if ritual.get(AirtableFields.RITUAL_MODE):
        ritual_data_prompt += f"ritualMode: {ritual.get(AirtableFields.RITUAL_MODE)}\n"
    if ritual.get(AirtableFields.RELATIONAL_NEEDS):
        ritual_data_prompt += f"relationalNeeds: {ritual.get(AirtableFields.RELATIONAL_NEEDS)}\n"
    if ritual.get(AirtableFields.TIME_TAKEN):
        ritual_data_prompt += f"timeTaken: {ritual.get(AirtableFields.TIME_TAKEN)}\n"

    return ritual_data_prompt

def populate_missing_ritual_fields_batch(batch: List[Dict[str, Any]], prompt_version: str) -> List[Dict[str, Any]]:
    """
    Populate missing ritual fields for a batch of rituals using LLM in one call.
    
    Args:
        batch: List of ritual dictionaries to process
        prompt_version: Version of the prompt being used for processing
    """
    print(f"  > Populating ritual details for {len(batch)} rituals using LLM with {prompt_version}...")

    try:
        for i, ritual in enumerate(batch):
            title = ritual.get(AirtableFields.TITLE, "")
            if not title:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title")
                continue

            ritual_data_prompt = generate_ritual_data_prompt(ritual)
            print(ritual_data_prompt)
            details, usage_info = call_llm_json_with_usage(
                model_class=RitualDetailsResponse,
                prompt_version=prompt_version,
                user_input=ritual_data_prompt
            )

            dump_llm_batch_output(details, usage_info, title)

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
