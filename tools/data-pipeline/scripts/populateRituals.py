import requests
import json
from time import sleep
import argparse
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Initial parameters
START_ROW = 1
END_ROW = 500
PAGE_SIZE = 100 # Airtable limits to 100

# Airtable params
BASE_ID = 'appt8THxSUPVnWkJk'
TABLE_NAME = 'Rituals Experiment'
TABLE_VIEW_NAME = 'g'

AIRTABLE_TOKEN = os.getenv('AIRTABLE_TOKEN')

# Constants
STEPS_FIELD = "Steps"
STATUS_FIELD = "Status"

def transform_airtable_record(record):
    flat_data = record.get("fields", {}).copy()
    flat_data['airtable_id'] = record.get('id')
    return flat_data

def read_from_airtable(start_row, end_row):
    if start_row < 1 or end_row < start_row:
        print("ERROR: Invalid row range. start_row must be >= 1 and <= end_row.")
        return []
    url = f"https://api.airtable.com/v0/{BASE_ID}/{TABLE_NAME}"
    headers = {
        "Authorization": f"Bearer {AIRTABLE_TOKEN}",
        "Content-Type": "application/json"
    }
    params = {
        "pageSize": PAGE_SIZE,
        "maxRecords": end_row,
        "view": f"{TABLE_VIEW_NAME}"
    }
    offset = None
    all_records = []
    while True:
        try:
            if offset:
                params["offset"] = offset
                print(f"  > Fetching next page using offset: {offset}")
            else:
                print("  > Fetching first page...")
    
            response = requests.get(url, headers=headers, params=params)
            response.raise_for_status() # Raise an HTTPError for bad responses (4xx or 5xx)
    
            data = response.json()
    
            # Add the records from the current page to the total list
            current_records = data.get("records", [])
            all_records.extend(current_records)
            print(f"  > Fetched {len(current_records)} records. Total records so far: {len(all_records)}")
    
            # Check for the offset to determine if there are more pages
            offset = data.get("offset")
    
            # Break if no more offset or if we have reached maxRecords (which is end_row)
            if not offset or len(all_records) >= end_row:
                break
    
            # Rate limit guard: Airtable limits requests to 5 per second, per base.
            # We add a small delay to be safe.
            sleep(0.2)
        except requests.exceptions.RequestException as e:
            print(f"\n--- An error occurred during the API request ---")
            print(f"Error: {e}")
            # Check if response object exists before accessing its attributes
            response_code = response.status_code if 'response' in locals() else 'N/A'
            response_text = response.text if 'response' in locals() else 'N/A'
            print(f"Response status code: {response_code}")
            print(f"Response body: {response_text}")
            # Return whatever was fetched before the error
            return all_records[start_row - 1 : end_row]
    final_records = all_records[start_row - 1 : end_row]

    flattened_records = [transform_airtable_record(record) for record in final_records]

    print(f"--- Finished fetching. Returning {len(flattened_records)} records. ---")
    return flattened_records

def filter_actionable_rituals(airtable_records):
    return [
        record for record in airtable_records 
        if not record.get(STEPS_FIELD)
    ]

def write_batch_to_airtable(batch):
    if not batch:
        return True

    url = f"https://api.airtable.com/v0/{BASE_ID}/{TABLE_NAME}"
    headers = {
        "Authorization": f"Bearer {AIRTABLE_TOKEN}",
        "Content-Type": "application/json"
    }

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
        
    payload = {"records": airtable_records}

    print(f"  > Attempting to update {len(airtable_records)} records...")
    
    try:
        # Use PATCH method for updating existing records
        response = requests.patch(url, headers=headers, data=json.dumps(payload))
        response.raise_for_status()
        
        print(f"  > Batch update successful.")
        return True

    except requests.exceptions.RequestException as e:
        print(f"\n--- ERROR during batch update to Airtable ---")
        print(f"Error: {e}")
        response_code = response.status_code if 'response' in locals() else 'N/A'
        response_text = response.text if 'response' in locals() else 'N/A'
        print(f"Response status code: {response_code}")
        print(f"Response body: {response_text}")
        return False

def populate_missing_ritual_batch(batch):
    print(f"  > Populating steps for {len(batch)} rituals...")
    for ritual in batch:
        if not ritual.get(STEPS_FIELD):
            ritual[STEPS_FIELD] = f'Sample Step: Generate steps for "{ritual.get("Title", "Description")}"'
            ritual[STATUS_FIELD] = 'NEED_REVIEW'

def populate_rituals(rituals):
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
        populate_missing_ritual_batch(batch)
        
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
    parser.add_argument(
        '--batch-size', 
        type=int, 
        default=5, 
        dest='update_batch_size',
        help=f'The maximum number of records to update in one batch (default: 5, max 10).'
    )
    
    args = parser.parse_args()
    global UPDATE_BATCH_SIZE
    UPDATE_BATCH_SIZE =  args.update_batch_size
    # -------------------------------------
    
    print(f"Using update batch size: {UPDATE_BATCH_SIZE}")
    # -------------------------------------
        
    # 1. Fetch records
    all_records = read_from_airtable(start_row=args.start_row, end_row=args.end_row)

    # 2. Filter records
    actionable_rituals = filter_actionable_rituals(all_records)
    
    # 3. Populate missing fields and write back to Airtable
    populate_rituals(actionable_rituals)
    
    # 4. Output results 
    if actionable_rituals:
        print(f"\n*** Processed {len(actionable_rituals)} Actionable Rituals ***")
    else:
        print("No actionable rituals were found to process.")