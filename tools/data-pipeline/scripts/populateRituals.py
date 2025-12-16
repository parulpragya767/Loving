import requests
import json
from time import sleep
import argparse
from airtable_utils import read_from_airtable, update_airtable

# Initial parameters
START_ROW = 1
END_ROW = 500

# Constants
STEPS_FIELD = "Steps"
SYNC_STATUS_FIELD = "Sync Status"
UPDATE_BATCH_SIZE = 10

def transform_airtable_record(record: dict) -> dict:
    """Flatten Airtable record by merging fields with record ID."""
    return {**record.get("fields", {}), 'airtable_id': record.get('id')}

def fetch_rituals_from_airtable(start_row: int, end_row: int) -> list[dict]:
    """
    Fetch and transform rituals from Airtable within the specified row range.
    Only fetches records where Sync Status is 'GENERATE'.
    """
    if start_row < 1 or end_row < start_row:
        raise ValueError("start_row must be >= 1 and <= end_row")
    
    print(f"  > Fetching actionable records from rows {start_row} to {end_row}...")
    filter_formula = f"{{{SYNC_STATUS_FIELD}}} = 'GENERATE'"
    all_records = read_from_airtable(start=start_row - 1, end=end_row, filter=filter_formula)
    print(f"  > Fetched {len(all_records)} actionable records.")
    
    return [transform_airtable_record(record) for record in all_records]

def write_batch_to_airtable(batch: list[dict]) -> bool:
    """Write a batch of records to Airtable with minimal fields."""
    if not batch:
        return True
        
    airtable_records = []
    for ritual in batch:
        if not (record_id := ritual.get('airtable_id')):
            print(f"Warning: Skipping record due to missing 'airtable_id'")
            continue
            
        fields = {
            field: ritual[field] 
            for field in [STEPS_FIELD, SYNC_STATUS_FIELD] 
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

def populate_missing_ritual_fields_batch(batch):
    print(f"  > Populating steps for {len(batch)} rituals...")
    for ritual in batch:
        if not ritual.get(STEPS_FIELD):
            ritual[STEPS_FIELD] = f'Sample Step: Generate steps for "{ritual.get("Title", "Description")}"'
            ritual[SYNC_STATUS_FIELD] = 'REVIEW'

def populate_and_update_rituals_to_airtable(rituals):
    """
    Processes a list of actionable rituals, populates the missing fields in batches,
    and writes the updated records back to Airtable.
    """
    if not rituals:
        print("No rituals to populate. Skipping write process.")
        return
    
    # Iterate over the list in chunks of UPDATE_BATCH_SIZE (10)
    for i in range(0, len(rituals), UPDATE_BATCH_SIZE):
        batch = rituals[i:i + UPDATE_BATCH_SIZE]
        
        print(f"\nProcessing batch {i // UPDATE_BATCH_SIZE + 1} of {len(rituals) // UPDATE_BATCH_SIZE + 1} ({len(batch)} records)")
        
        # 1. Populate the missing steps (in-place modification of the 'batch' list)
        populate_missing_ritual_fields_batch(batch)
        
        # 2. Write the updated batch back to Airtable
        success = write_batch_to_airtable(batch)
        
        if not success:
            print(f"Stopping ritual population due to write error on batch starting at index {i}.")
            break

        # Rate limit guard: Wait between write batches to respect the 5 req/sec limit.
        sleep(0.2)
    return rituals

# --- MAIN EXECUTION ---
if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description="Airtable ritual processor. Reads, filters for missing steps, and writes generated steps back to Airtable.")
    
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
    
    # 2. Populate missing fields and write back to Airtable
    populate_and_update_rituals_to_airtable(actionable_rituals)
    
    # 3. Output results 
    if actionable_rituals:
        print(f"\n*** Processed {len(actionable_rituals)} Actionable Rituals ***")
    else:
        print("No actionable rituals were found to process.")