import json
import os
import argparse
from datetime import datetime, timezone
from typing import List, Dict, Any, Literal
from airtable_utils import read_from_airtable, update_airtable, create_airtable_records, AirtableFields, SyncStatus
from ritual_utils import sanitize_steps_text_to_array, steps_array_to_text

# The file path for the local JSON array.
JSON_FILE_PATH: str = "../data/rituals.json"

# The unique field name used to link records across Airtable and JSON.
ID_FIELD_NAME: str = "id"

# Mapping of field names: {JSON_KEY: AIRTABLE_FIELD_NAME}
# Only fields present in this map will be synced.
FIELD_MAP: Dict[str, str] = {
    "id": ID_FIELD_NAME,
    "title": AirtableFields.TITLE,
    "tagLine": AirtableFields.TAGLINE,
    "description": AirtableFields.DESCRIPTION,
    "howItHelps": AirtableFields.HOW_IT_HELPS,
    "steps": AirtableFields.STEPS,
    "loveTypes": AirtableFields.LOVE_TYPES,
    "relationalNeeds": AirtableFields.RELATIONAL_NEEDS,
    "ritualMode": AirtableFields.RITUAL_MODE,
    "ritualTones": AirtableFields.RITUAL_TONES,
    "timeTaken": AirtableFields.TIME_TAKEN,
    "semanticSummary": AirtableFields.SEMANTIC_SUMMARY,
    "status": AirtableFields.STATUS,
}

class AirtableJsonSyncer:
    """
    Handles bidirectional synchronization between an Airtable table and a local JSON file.
    """

    def __init__(self, id_field_name: str, field_map: Dict[str, str]):
        """Initializes the syncer with mapping details."""
        self.id_field_name = id_field_name
        self.field_map = field_map
        
        # Invert the map for 'to_json' sync direction
        self.inverted_field_map = {v: k for k, v in field_map.items()}
        print(f"Syncer initialized.")

    # --- Utility Methods ---

    def _apply_map(self, source_data: Dict[str, Any], direction: Literal['to_airtable', 'to_json']) -> Dict[str, Any]:
        """Maps fields based on the sync direction."""
        mapped_data = {}
        mapping = self.field_map if direction == 'to_airtable' else self.inverted_field_map

        for source_key, target_key in mapping.items():
            if source_key in source_data:
                mapped_data[target_key] = source_data[source_key]

        return mapped_data


    def load_airtable_data(self) -> List[Dict[str, Any]]:
        """Fetches all records from Airtable and formats them for processing."""   
        try:
            # Use the utility function to fetch only PUBLISHED records
            filter_formula = f"{{{AirtableFields.SYNC_STATUS}}} = '{SyncStatus.PUBLISHED.value}'"
            airtable_records = read_from_airtable(filter=filter_formula)
        except Exception as e:
            print(f"Error fetching Airtable data: {e}")
            return []

        # Map Airtable fields to JSON keys and store the Airtable record ID
        mapped_data = []
        for record in airtable_records:
            fields = record.get('fields', {})
            # Map Airtable keys (values in FIELD_MAP) to JSON keys (keys in FIELD_MAP)
            mapped_record = self._apply_map(fields, 'to_json')
            
            # The Airtable internal ID is crucial for updates
            mapped_record[AirtableFields.AIRTABLE_RECORD_ID] = record['id']
            
            # Ensure the common ID field exists for matching
            if self.id_field_name in mapped_record:
                mapped_data.append(mapped_record)
            else:
                print(f"Skipping Airtable record {record['id']}: Missing common ID field '{self.id_field_name}' after mapping.")

        print(f"Successfully loaded {len(mapped_data)} records from Airtable.")
        return mapped_data

    def load_json_data(self) -> List[Dict[str, Any]]:
        """Loads data from the local JSON file."""
        if not os.path.exists(JSON_FILE_PATH):
            print(f"JSON file not found at {JSON_FILE_PATH}. Returning empty list.")
            return []
        
        print(f"Loading data from {JSON_FILE_PATH}...")
        with open(JSON_FILE_PATH, 'r', encoding='utf-8') as f:
            try:
                data = json.load(f)
                if not isinstance(data, list):
                    print("Error: JSON content must be a list of objects.")
                    return []
                print(f"Successfully loaded {len(data)} records from JSON.")
                return data
            except json.JSONDecodeError as e:
                print(f"Error decoding JSON file: {e}")
                return []

    def save_json_data(self, data: List[Dict[str, Any]]):
        """Saves data back to the local JSON file."""
        print(f"Saving {len(data)} records to {JSON_FILE_PATH}...")
        with open(JSON_FILE_PATH, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        print("JSON file successfully saved.")

    # --- Synchronization Logic ---
    def sync_to_airtable(self, json_data: List[Dict[str, Any]], airtable_data: List[Dict[str, Any]]):
        """
        Syncs data from JSON to Airtable.
        JSON is the source of truth, updating existing Airtable records or creating new ones.
        """
        print("\n--- Starting Sync: JSON (Source) -> Airtable (Target) ---")
        
        # Index existing Airtable records by their common ID field
        airtable_index = {
            record[self.id_field_name]: record[AirtableFields.AIRTABLE_RECORD_ID]
            for record in airtable_data if self.id_field_name in record
        }

        updates: List[Dict[str, Any]] = []
        creations: List[Dict[str, Any]] = []
        utc_timestamp = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.000Z')
        
        for json_record in json_data:
            common_id = json_record.get(self.id_field_name)

            if not common_id:
                print(f"Warning: Skipping JSON record due to missing ID field '{self.id_field_name}'.")
                continue

            # Apply mapping for Airtable structure
            airtable_fields = self._apply_map(json_record, 'to_airtable')
            
            # Sanitize steps field for Airtable (convert array to JSON string)
            if AirtableFields.STEPS in airtable_fields:
                airtable_fields[AirtableFields.STEPS] = steps_array_to_text(airtable_fields[AirtableFields.STEPS])
            
            # Add a timestamp to track the sync time
            airtable_fields[AirtableFields.LAST_UPDATED_TS] = utc_timestamp

            if common_id in airtable_index:
                # Record exists, prepare for update
                record_id = airtable_index[common_id]
                updates.append({
                    'id': record_id,
                    'fields': airtable_fields
                })
            else:
                # Record is new, prepare for creation
                creations.append({
                    'fields': airtable_fields
                })

        # Process updates in batches (Airtable API limit is 10 records per request)
        if updates:
            print(f"Processing {len(updates)} records for update...")
            success = update_airtable(updates)
            if success:
                print(f"Successfully updated {len(updates)} Airtable records.")
            else:
                print(f"Failed to update {len(updates)} Airtable records.")

        # Process creations in batches
        if creations:
            print(f"Processing {len(creations)} records for creation...")
            success = create_airtable_records(creations)
            if success:
                print(f"Successfully created {len(creations)} Airtable records.")
            else:
                print(f"Failed to create {len(creations)} Airtable records.")

        if not updates and not creations:
            print("No changes detected in JSON requiring Airtable modification.")

    def sync_to_json(self, json_data: List[Dict[str, Any]], airtable_data: List[Dict[str, Any]]):
        """
        Syncs data from Airtable to JSON.
        Airtable is the source of truth, updating existing JSON records or adding new ones.
        """
        print("\n--- Starting Sync: Airtable (Source) -> JSON (Target) ---")
        
        # Index existing JSON records by their common ID field
        json_index: Dict[Any, Dict[str, Any]] = {
            record[self.id_field_name]: record
            for record in json_data if self.id_field_name in record
        }
        
        # List of records that will replace the old JSON data
        new_json_data = []
        updates_count = 0
        creations_count = 0

        for airtable_record in airtable_data:
            common_id = airtable_record.get(self.id_field_name)

            # Sanitize steps from Airtable (JSON string to array) for JSON
            if "steps" in airtable_record:
                airtable_record["steps"] = sanitize_steps_text_to_array(airtable_record["steps"])

            if common_id in json_index:
                # Record exists in JSON, update it
                existing_json_record = json_index[common_id]
                
                # Update existing fields based on Airtable data
                for key, value in airtable_record.items():
                    # Only update fields that are defined in the field map (JSON keys)
                    if key in self.field_map or key == self.id_field_name:
                        existing_json_record[key] = value

                new_json_data.append(existing_json_record)
                updates_count += 1
                del json_index[common_id] # Mark as processed
            else:
                # Record is new, add it to JSON
                new_json_data.append(airtable_record)
                creations_count += 1

        # Add any remaining (unmatched) records from the original JSON back to the new list
        # This keeps JSON records that don't exist in Airtable (or were unmatched)
        for record in json_index.values():
             new_json_data.append(record)

        # Remove any fields present in AIRTABLE_SPECIFIC_FIELDS before saving to JSON
        final_data_for_json = []
        for record in new_json_data:
            cleaned_record = record.copy()
            for field in AirtableFields.AIRTABLE_SPECIFIC_FIELDS:
                cleaned_record.pop(field, None)
            final_data_for_json.append(cleaned_record)
        # ------------------------------------

        # Save the updated dataset back to the JSON file
        self.save_json_data(final_data_for_json)
        
        print(f"Completed sync: {updates_count} JSON records updated, {creations_count} new records added.")

    def sync(self, direction: Literal['to_airtable', 'to_json']):
        """The main synchronization method."""
        
        print("Starting Synchronization Process...")
        json_data = self.load_json_data()
        airtable_data = self.load_airtable_data()

        if not json_data and not airtable_data:
            print("Both sources are empty. Nothing to sync.")
            return

        if direction == 'to_airtable':
            self.sync_to_airtable(json_data, airtable_data)
        elif direction == 'to_json':
            self.sync_to_json(json_data, airtable_data)

if __name__ == '__main__':
    
    # Setup argument parsing
    parser = argparse.ArgumentParser(
        description="Bidirectional sync tool for Airtable and local JSON file.",
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument(
        '--sync_direction',
        type=str,
        required=True,
        choices=['to_airtable', 'to_json'],
        help="The direction of synchronization.\n"
             "  'to_airtable': JSON is source of truth (updates Airtable).\n"
             "  'to_json': Airtable is source of truth (updates JSON)."
    )
    args = parser.parse_args()

    try:
        syncer = AirtableJsonSyncer(
            id_field_name=ID_FIELD_NAME,
            field_map=FIELD_MAP,
        )
        # Pass the command-line argument to the sync method
        syncer.sync(args.sync_direction)
        
    except ValueError as e:
        print(f"Configuration Error: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")