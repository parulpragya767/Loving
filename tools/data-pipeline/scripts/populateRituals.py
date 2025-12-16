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
STATUS_FIELD = "Status"
UPDATE_BATCH_SIZE = 10

def transform_airtable_record(record):
    flat_data = record.get("fields", {}).copy()
    flat_data['airtable_id'] = record.get('id')
    return flat_data

def fetch_rituals_from_airtable(start_row, end_row):
    if start_row < 1 or end_row < start_row:
        print("ERROR: Invalid row range. start_row must be >= 1 and <= end_row.")
        return []
    
    try:
        print(f"  > Fetching records from rows {start_row} to {end_row}...")
        
        # Use the utility function to fetch records
        all_records = read_from_airtable(
            start=start_row - 1,  # Convert to 0-based index
            end=end_row,
        )
        
        print(f"  > Fetched {len(all_records)} records.")
        
        # Transform records to match expected format
        flattened_records = [transform_airtable_record(record) for record in all_records]
        
        print(f"--- Finished fetching. Returning {len(flattened_records)} records. ---")
        return flattened_records
        
    except Exception as e:
        print(f"\n--- An error occurred during the API request ---")
        print(f"Error: {e}")
        return []

def filter_actionable_rituals(airtable_records):
    return [
        record for record in airtable_records 
        if not record.get(STEPS_FIELD)
    ]

def write_batch_to_airtable(batch):
    if not batch:
        return True

    # Restructure the flat Python dictionary back into the Airtable API format
    airtable_records = []
    for ritual in batch:
        record_id = ritual.get('airtable_id')
        fields_to_update = [STEPS_FIELD, STATUS_FIELD]
        fields = {}
        
        if not record_id:
            print(f"Warning: Skipping record due to missing 'airtable_id'.")
            continue

        # Dynamically build the 'fields' payload
        for field_name in fields_to_update:
            field_value = ritual.get(field_name)
            
            # Only include the field in the update if its value is not None
            if field_value is not None:
                fields[field_name] = field_value
        
        # Only append to the batch if there is at least one field to set
        if fields:
            airtable_records.append({
                "id": record_id,
                "fields": fields
            })
        else:
            print(f"Warning: Skipping record with ID {record_id}. No valid fields found in {fields_to_update} to update.")
            
    if not airtable_records:
        print("Warning: No valid records to update in this batch.")
        return False
        
    print(f"  > Attempting to update {len(airtable_records)} records...")
    
    try:
        # Use the utility function for batch update
        success = update_airtable(airtable_records)
        if success:
            print(f"  > Batch update successful.")
        return success

    except Exception as e:
        print(f"\n--- ERROR during batch update to Airtable ---")
        print(f"Error: {e}")
        return False

def populate_missing_ritual_fields_batch(batch):
    print(f"  > Populating steps for {len(batch)} rituals...")
    for ritual in batch:
        if not ritual.get(STEPS_FIELD):
            ritual[STEPS_FIELD] = f'Sample Step: Generate steps for "{ritual.get("Title", "Description")}"'
            ritual[STATUS_FIELD] = 'NEED_REVIEW'

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
        
    # 1. Fetch records
    all_records = fetch_rituals_from_airtable(start_row=args.start_row, end_row=args.end_row)

    # 2. Filter records
    actionable_rituals = filter_actionable_rituals(all_records)
    
    # 3. Populate missing fields and write back to Airtable
    populate_and_update_rituals_to_airtable(actionable_rituals)
    
    # 4. Output results 
    if actionable_rituals:
        print(f"\n*** Processed {len(actionable_rituals)} Actionable Rituals ***")
    else:
        print("No actionable rituals were found to process.")