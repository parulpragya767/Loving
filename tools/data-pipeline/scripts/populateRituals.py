import requests
import json
from time import sleep
import argparse
from datetime import datetime
from airtable_utils import read_from_airtable, update_airtable, AirtableFields, SyncStatus
from ritual_llm_populator import populate_missing_ritual_fields_batch

# Initial parameters
START_ROW = 1
END_ROW = 500

# Constants
UPDATE_BATCH_SIZE = 10
CHANGELOG_PATH = "../data/rituals_changelog.json"

def transform_airtable_record(record: dict) -> dict:
    """Flatten Airtable record by merging fields with record ID."""
    return {**record.get("fields", {}), 'airtable_id': record.get('id')}

def fetch_rituals_from_airtable(start_row: int, end_row: int) -> list[dict]:
    """
    Fetch and transform rituals from Airtable within the specified row range.
    Only fetches records where Sync Status is GENERATE.
    """
    if start_row < 1 or end_row < start_row:
        raise ValueError("start_row must be >= 1 and <= end_row")
    
    print(f"  > Fetching actionable records from rows {start_row} to {end_row}...")
    filter_formula = f"{{{AirtableFields.SYNC_STATUS}}} = '{SyncStatus.GENERATE.value}'"
    all_records = read_from_airtable(start=start_row - 1, end=end_row, filter=filter_formula)
    print(f"  > Fetched {len(all_records)} actionable records.")
    
    return [transform_airtable_record(record) for record in all_records]

def write_batch_to_airtable(batch: list[dict]) -> bool:
    """Write a batch of records to Airtable with ritual fields."""
    if not batch:
        return True
        
    airtable_records = []
    for ritual in batch:
        if not (record_id := ritual.get('airtable_id')):
            print(f"Warning: Skipping record due to missing 'airtable_id'")
            continue
            
        # Include all ritual fields plus sync status
        fields = {
            field: ritual[field] 
            for field in AirtableFields.RITUAL_FIELDS + [AirtableFields.SYNC_STATUS] 
            if field in ritual and ritual[field] is not None
        }
        
        if fields:
            airtable_records.append({"id": record_id, "fields": fields})
    
    if not airtable_records:
        print("Warning: No valid records to update in this batch")
        return False
    
    print(f"  > Updating {len(airtable_records)} records...")
    success = update_airtable(airtable_records)
    
    if success:
        print("  > Batch update successful")
    else:
        print("  > Batch update failed")
        
    return success

def dump_batch_to_changelog(batch: list[dict]):
    """Dump the current rituals batch to the changelog JSON file."""
    if not batch:
        return
    
    # Create timestamp for this batch
    timestamp = datetime.now().isoformat()
    
    # Prepare batch entry with metadata
    batch_entry = {
        "timestamp": timestamp,
        "batch_size": len(batch),
        "rituals": batch
    }
    
    # Read existing changelog if it exists
    try:
        with open(CHANGELOG_PATH, 'r', encoding='utf-8') as f:
            changelog_data = json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        changelog_data = []
    
    # Append new batch entry
    changelog_data.append(batch_entry)
    
    # Write back to file
    with open(CHANGELOG_PATH, 'w', encoding='utf-8') as f:
        json.dump(changelog_data, f, indent=2, ensure_ascii=False)
    
    print(f"  > Dumped {len(batch)} rituals to changelog at {timestamp}")

def populate_and_update_rituals_to_airtable(rituals):
    """
    Processes a list of actionable rituals, populates the missing fields in batches,
    and writes the updated records back to Airtable.
    
    Args:
        rituals: List of ritual dictionaries from Airtable
    """
    if not rituals:
        print("No rituals to populate. Skipping write process.")
        return
    
    # Iterate over the list in chunks of UPDATE_BATCH_SIZE (10)
    for i in range(0, len(rituals), UPDATE_BATCH_SIZE):
        batch = rituals[i:i + UPDATE_BATCH_SIZE]
        
        print(f"\nProcessing batch {i // UPDATE_BATCH_SIZE + 1} of {len(rituals) // UPDATE_BATCH_SIZE + 1} ({len(batch)} records)")
        
        # 1. Dump the current batch to changelog before processing
        dump_batch_to_changelog(batch)
        
        # 2. Populate the missing fields using LLM (in-place modification of the 'batch' list)
        populate_missing_ritual_fields_batch(batch)
        
        # # 3. Write the updated batch back to Airtable
        # success = write_batch_to_airtable(batch)
        
        # if not success:
        #     print(f"Stopping ritual population due to write error on batch starting at index {i}.")
        #     break

        # Rate limit guard: Wait between write batches to respect the 5 req/sec limit.
        sleep(0.2)
    return rituals

# --- MAIN EXECUTION ---
if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description="Airtable ritual processor. Reads the actionable rituals, populates the missing fields using LLM, and updates the records back to Airtable.")
    
    # start_row and end_row are now mandatory
    parser.add_argument(
        '--start', 
        type=int, 
        required=True, 
        dest='start_row',
        help='The 1-based starting row number to read (e.g., 1).'
    )
    parser.add_argument(
        '--end', 
        type=int, 
        required=True, 
        dest='end_row',
        help='The ending row number to read (e.g., 50).'
    )

    args = parser.parse_args()
        
    # 1. Fetch records which are actionable rituals with Sync Status == GENERATE
    actionable_rituals = fetch_rituals_from_airtable(start_row=args.start_row, end_row=args.end_row)
    
    # 2. Populate missing fields using LLM and write back to Airtable
    populate_and_update_rituals_to_airtable(actionable_rituals)
    
    # 3. Output results 
    if actionable_rituals:
        print(f"\n*** Processed {len(actionable_rituals)} Actionable Rituals ***")
    else:
        print("No actionable rituals were found to process.")