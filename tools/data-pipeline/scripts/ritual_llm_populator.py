import json
from typing import Dict, List, Any
from LLMUtils import call_llm_json
from PromptUtils import get_ritual_details_prompt
from data_models import RitualDetailsResponse

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
            ritual["Tagline"] = details.tagLine
            ritual["Description"] = details.description
            ritual["Steps"] = "\n".join(details.steps)
            ritual["How It Helps"] = details.howItHelps
            ritual["Love Type"] = ", ".join([l.value for l in details.loveTypes])
            ritual["Ritual Mode"] = details.ritualMode.value
            ritual["Time Taken"] = details.timeTaken.value
            ritual["Relational Need"] = ", ".join([r.value for r in details.relationalNeeds])
            ritual["Tone"] = ", ".join([t.value for t in details.ritualTones])
            ritual["Semantic Summary"] = details.semanticSummary
            ritual["Sync Status"] = 'REVIEW'
            
            print(f"  > Successfully populated ritual {i+1}: {title}")
            
        except Exception as e:
            print(f"  > Error populating ritual {i+1}: {str(e)}")
            # Set status to indicate failure
            ritual["Sync Status"] = 'ERROR'
            continue
    
    return batch
