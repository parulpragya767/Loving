import json
from typing import Dict, List, Any
from LLMUtils import call_llm_json
from PromptUtils import get_ritual_details_prompt
from data_models import BatchRitualDetailsResponse

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
            title = ritual.get("Title", "")
            if not title:
                print(f"  > Warning: Skipping ritual {i+1} due to missing title")
                continue
            titles.append(title)
            valid_rituals.append(ritual)
        
        if not titles:
            print("  > No valid rituals to process")
            return batch
        
        # Create prompt with all titles
        titles_prompt = "Titles:\n" + "\n".join(titles)
        
        # Generate ritual details for all rituals in one batch
        batch_details = call_llm_json(
            model_class=BatchRitualDetailsResponse,
            system=system_prompt,
            prompt=titles_prompt,
        )
        
        print(json.dumps([ritual.dict() for ritual in batch_details.rituals], indent=2, ensure_ascii=False))
        
        # Update each ritual with its corresponding details
        for i, (ritual, details) in enumerate(zip(valid_rituals, batch_details.rituals)):
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
            
            print(f"  > Successfully populated ritual {i+1}: {ritual.get('Title', 'Unknown')}")
        
    except Exception as e:
        print(f"  > Error populating batch: {str(e)}")
        # Set status to indicate failure for all valid rituals
        for ritual in valid_rituals:
            ritual["Sync Status"] = 'ERROR'
    
    return batch
